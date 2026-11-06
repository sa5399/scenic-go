/**
 * 生成 public/img 下的占位图（无第三方依赖，Node 内置 zlib 手写 PNG 编码）。
 *
 * 风格与 styles/tokens.css 同源：
 * - spot  12 张 800×600：海报化湖区小景（暮色天带 + 日落橘圆 + 远山剪影 + 层叠水色）
 * - exh   20 张 640×480：青瓷器形（两种青瓷色，器身 + 高光 + 台座阴影）
 * - avatar 10 张 240×240：几何头像（湖青系 10 色 + 环形母题）
 *
 * 均为纯色块海报风（无渐变带状噪声），PNG 体积小、风格统一。
 * 用法：node scripts/gen-placeholders.mjs
 * 真实照片到位后，按同名路径覆盖即可。
 */
import { mkdirSync, writeFileSync } from 'node:fs'
import { deflateSync } from 'node:zlib'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', 'public', 'img')

/* ---------- PNG 编码（RGBA8，filter 0） ---------- */

const CRC_TABLE = (() => {
  const t = new Uint32Array(256)
  for (let n = 0; n < 256; n++) {
    let c = n
    for (let k = 0; k < 8; k++) {
      c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1
    }
    t[n] = c >>> 0
  }
  return t
})()

function crc32(buf) {
  let c = 0xffffffff
  for (const b of buf) {
    c = CRC_TABLE[(c ^ b) & 0xff] ^ (c >>> 8)
  }
  return (c ^ 0xffffffff) >>> 0
}

function chunk(type, data) {
  const out = Buffer.alloc(8 + data.length + 4)
  out.writeUInt32BE(data.length, 0)
  out.write(type, 4, 'ascii')
  data.copy(out, 8)
  out.writeUInt32BE(crc32(out.subarray(4, 8 + data.length)), 8 + data.length)
  return out
}

function encodePng(width, height, rgba) {
  const stride = width * 4
  const raw = Buffer.alloc((stride + 1) * height)
  for (let y = 0; y < height; y++) {
    raw[y * (stride + 1)] = 0 // filter: none（海报色块，不需要预测压缩）
    rgba.copy(raw, y * (stride + 1) + 1, y * stride, (y + 1) * stride)
  }
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(width, 0)
  ihdr.writeUInt32BE(height, 4)
  ihdr[8] = 8 // bit depth
  ihdr[9] = 6 // RGBA
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ])
}

/* ---------- 画布 ---------- */

class Canvas {
  constructor(width, height) {
    this.width = width
    this.height = height
    this.data = Buffer.alloc(width * height * 4)
  }

  set(x, y, [r, g, b, a = 255]) {
    if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
      return
    }
    const i = (y * this.width + x) * 4
    if (a >= 255) {
      this.data[i] = r
      this.data[i + 1] = g
      this.data[i + 2] = b
      this.data[i + 3] = 255
      return
    }
    // 简单 alpha 合成
    const na = a / 255
    this.data[i] = Math.round(r * na + this.data[i] * (1 - na))
    this.data[i + 1] = Math.round(g * na + this.data[i + 1] * (1 - na))
    this.data[i + 2] = Math.round(b * na + this.data[i + 2] * (1 - na))
    this.data[i + 3] = 255
  }

  fill(color) {
    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {
        this.set(x, y, color)
      }
    }
  }

  band(y0, y1, color) {
    for (let y = Math.max(0, y0); y < Math.min(this.height, y1); y++) {
      for (let x = 0; x < this.width; x++) {
        this.set(x, y, color)
      }
    }
  }

  /** 正弦波上缘的色带（水面/山脊） */
  waveBand(y0, amp, phase, color) {
    for (let x = 0; x < this.width; x++) {
      const top = y0 + Math.round(amp * Math.sin(x / 90 + phase) + amp * 0.5 * Math.sin(x / 37 + phase * 2))
      for (let y = Math.max(0, top); y < this.height; y++) {
        this.set(x, y, color)
      }
    }
  }

  disc(cx, cy, r, color) {
    for (let y = Math.ceil(cy - r); y <= cy + r; y++) {
      for (let x = Math.ceil(cx - r); x <= cx + r; x++) {
        if ((x - cx) ** 2 + (y - cy) ** 2 <= r * r) {
          this.set(x, y, color)
        }
      }
    }
  }

  rect(x0, y0, x1, y1, color) {
    for (let y = y0; y < y1; y++) {
      for (let x = x0; x < x1; x++) {
        this.set(x, y, color)
      }
    }
  }

  ellipse(cx, cy, rx, ry, color) {
    for (let y = Math.ceil(cy - ry); y <= cy + ry; y++) {
      for (let x = Math.ceil(cx - rx); x <= cx + rx; x++) {
        const nx = (x - cx) / rx
        const ny = (y - cy) / ry
        if (nx * nx + ny * ny <= 1) {
          this.set(x, y, color)
        }
      }
    }
  }

  line(x0, y0, x1, y1, color, thickness = 1) {
    const steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0))
    for (let i = 0; i <= steps; i++) {
      const x = Math.round(x0 + ((x1 - x0) * i) / steps)
      const y = Math.round(y0 + ((y1 - y0) * i) / steps)
      if (thickness <= 1) {
        this.set(x, y, color)
      } else {
        this.disc(x, y, Math.floor(thickness / 2), color)
      }
    }
  }

  /** 半圆拱带（桥洞）：圆心 (cx,cy)，半径 r，厚 t，只画 y<=cy 的上半 */
  arch(cx, cy, r, t, color) {
    for (let y = cy - r - t; y <= cy; y++) {
      for (let x = cx - r - t; x <= cx + r + t; x++) {
        const d = Math.hypot(x - cx, y - cy)
        if (d <= r + t && d >= r) {
          this.set(x, y, color)
        }
      }
    }
  }

  /** 上缘/下缘两条正弦围合的实心形（船身、山体可用） */
  lens(x0, x1, yMid, ampTop, ampBottom, color, phase = 0) {
    for (let x = x0; x <= x1; x++) {
      const t = (x - x0) / (x1 - x0)
      const s = Math.sin(Math.PI * t)
      const yTop = yMid - Math.round(ampTop * s + Math.sin(x / 23 + phase) * 2)
      const yBottom = yMid + Math.round(ampBottom * s)
      for (let y = yTop; y <= yBottom; y++) {
        this.set(x, y, color)
      }
    }
  }

  save(path) {
    mkdirSync(dirname(path), { recursive: true })
    writeFileSync(path, encodePng(this.width, this.height, this.data))
  }
}

const hex = (h) => [
  parseInt(h.slice(1, 3), 16),
  parseInt(h.slice(3, 5), 16),
  parseInt(h.slice(5, 7), 16),
]

/* ---------- 湖青色板（与 tokens.css 一致） ---------- */

const C = {
  skyTop: hex('#e9f1ef'),
  skyDusk: hex('#f3e9d2'),
  skyCool: hex('#dfe9e4'),
  sun: hex('#e0863c'),
  sunPale: hex('#ecb27a'),
  ridgeFar: hex('#9dbfb6'),
  ridgeNear: hex('#6fa093'),
  water1: hex('#4e9285'),
  water2: hex('#3d8677'),
  water3: hex('#2e7d6d'),
  waterDeep: hex('#235e52'),
  celadonBg: hex('#edf3f1'),
  frame: hex('#d8e5e1'),
  shadow: hex('#dce8e4'),
}

/* ---------- spot：湖区小景 ---------- */

function drawSpot(index) {
  const w = 800
  const h = 600
  const cv = new Canvas(w, h)
  const warm = index % 2 === 0 // 奇偶交替：暮色 / 冷晨

  // 天空两条色带 + 日轮
  cv.band(0, 110, warm ? C.skyDusk : C.skyCool)
  cv.band(110, 300, warm ? C.skyTop : C.skyTop)
  const sunX = 130 + ((index * 137) % 540)
  cv.disc(sunX, 150, 46, warm ? C.sun : C.sunPale)

  // 远山两层
  cv.waveBand(212, 14, index * 1.3, C.ridgeFar)
  cv.waveBand(248, 18, index * 2.1 + 1, C.ridgeNear)

  // 层叠水色（由浅到深，波缘）
  cv.waveBand(304, 10, index * 0.7, C.water1)
  cv.waveBand(384, 12, index * 1.1 + 2, C.water2)
  cv.waveBand(464, 10, index * 0.5 + 4, C.water3)
  cv.waveBand(540, 12, index * 0.9 + 1, C.waterDeep)

  // 日光倒影：日轮正下方的水面碎光（柱状虚段）
  for (let y = 310; y < 590; y += 34) {
    const widthSeg = 10 + Math.round(14 * Math.sin((y + index) / 60))
    cv.rect(sunX - Math.round(widthSeg / 2), y, sunX + Math.round(widthSeg / 2), y + 16, C.sun)
  }
  cv.save(join(ROOT, 'spot', `${String(index + 1).padStart(2, '0')}.jpg`))
}

/* ---------- exh：青瓷器形 ---------- */

function drawExh(index) {
  const w = 640
  const h = 480
  const cv = new Canvas(w, h)
  const body = index % 2 === 0 ? C.water3 : hex('#3a7d8c') // 青瓷两色
  const highlight = index % 2 === 0 ? C.ridgeFar : hex('#8fb8c2')
  cv.fill(C.celadonBg)

  // 内框
  cv.rect(14, 14, w - 14, 22, C.frame)
  cv.rect(14, h - 22, w - 14, h - 14, C.frame)
  cv.rect(14, 14, 22, h - 14, C.frame)
  cv.rect(w - 22, 14, w - 14, h - 14, C.frame)

  const cx = w / 2
  const s = 1 - ((index % 3) * 0.06) // 尺寸微变

  // 台座阴影
  cv.disc(cx, 428, 92 * s, C.shadow)

  // 器身（椭圆）+ 颈 + 沿 + 足
  for (let y = 190; y <= 410; y++) {
    const t = (y - 190) / 220 // 0..1
    const rx = Math.round((30 + 66 * Math.sin(Math.PI * (0.18 + 0.64 * t))) * s)
    for (let x = cx - rx; x < cx + rx; x++) {
      const isHighlight = (x - cx) ** 2 / ((rx * 0.55) ** 2) + (y - 300) ** 2 / (90 ** 2) < 1 && x < cx
      cv.set(Math.round(x), y, isHighlight ? highlight : body)
    }
  }
  cv.rect(Math.round(cx - 26 * s), 128, Math.round(cx + 26 * s), 195, body)
  cv.rect(Math.round(cx - 42 * s), 118, Math.round(cx + 42 * s), 136, body)
  cv.rect(Math.round(cx - 34 * s), 410, Math.round(cx + 34 * s), 432, body)
  cv.save(join(ROOT, 'exh', `${String(index + 1).padStart(2, '0')}.jpg`))
}

/* ---------- avatar：几何头像 ---------- */

const AVATAR_COLORS = [
  '#2e7d6d', '#5a9a8e', '#8ab5ac', '#e0863c', '#b96a3b',
  '#235e52', '#7c8781', '#3a7d8c', '#a8b5b0', '#4e9285',
]

function drawAvatar(index) {
  const size = 240
  const cv = new Canvas(size, size)
  const bg = hex(AVATAR_COLORS[index % AVATAR_COLORS.length])
  const white = [255, 255, 255, 140]
  const dark = [0, 30, 24, 46]
  cv.fill(bg)

  const cx = size / 2
  // 环形母题：大白环 + 对角小圆，随 index 平移
  cv.disc(cx, cx, 74, white)
  cv.disc(cx, cx, 44, bg)
  cv.disc(64 + (index % 5) * 12, 66 + (index % 7) * 8, 22, dark)
  cv.disc(size - 70, size - 60, 14, white)
  cv.save(join(ROOT, 'avatar', `${String(index + 1).padStart(2, '0')}.png`))
}

/* ---------- 登录背景：富元素湖景（暮色 / 冷晨两版） ---------- */

function verticalBands(cv, y0, y1, stops) {
  // stops: [[t,color],...] 按比例铺色带（近渐变的分带画法，压缩友好）
  for (let i = 0; i < stops.length - 1; i++) {
    const [ta, ca] = stops[i]
    const [tb] = stops[i + 1]
    cv.band(Math.round(y0 + (y1 - y0) * ta), Math.round(y0 + (y1 - y0) * tb), ca)
  }
}

function drawLoginBg(name, cool) {
  const w = 1200
  const h = 800
  const waterY = 520
  const cv = new Canvas(w, h)
  const ink = hex('#24534a')

  // 天空（分带近似渐变）
  verticalBands(cv, 0, waterY, cool
    ? [[0, hex('#eef4f0')], [0.3, hex('#e4ede7')], [0.5, hex('#d5e3dc')], [0.72, hex('#c8d9d0')], [1, hex('#bcd3c8')]]
    : [[0, hex('#f6ead3')], [0.26, hex('#f1e4cc')], [0.48, hex('#ece7d5')], [0.7, hex('#dcdcc9')], [1, hex('#cfd8c9')]])

  // 日轮 + 晕
  const sun = { x: cool ? 860 : 740, y: 290, r: cool ? 40 : 52 }
  const sunColor = cool ? hex('#eed9ab') : hex('#e8a95f')
  cv.disc(sun.x, sun.y, sun.r + 62, [...sunColor, 28])
  cv.disc(sun.x, sun.y, sun.r + 30, [...sunColor, 55])
  cv.disc(sun.x, sun.y, sun.r, sunColor)

  // 云（横向长椭圆，半透明）
  const cloud = [255, 252, 244, 74]
  cv.ellipse(230, 150, 150, 15, cloud)
  cv.ellipse(520, 108, 190, 13, cloud)
  cv.ellipse(950, 205, 160, 17, [255, 250, 238, 60])
  cv.ellipse(120, 250, 120, 12, [255, 250, 238, 48])

  // 飞鸟（“人”字两笔）
  const bird = hex('#5a6b64')
  for (const [bx, by, s] of [[330, 180, 9], [395, 155, 7], [610, 215, 8], [660, 190, 6], [905, 130, 9], [985, 165, 6]]) {
    cv.line(bx - s, by + Math.round(s * 0.55), bx, by, bird)
    cv.line(bx, by, bx + s, by + Math.round(s * 0.55), bird)
  }

  // 三层远山
  cv.waveBand(352, 15, 1.15, [157, 191, 182, 200])
  cv.waveBand(400, 18, 2.35, [126, 168, 154, 225])
  cv.waveBand(452, 15, 0.6, [100, 148, 133])

  // 山脚雾带
  cv.band(470, 505, [248, 250, 246, 66])
  cv.band(495, waterY, [248, 250, 246, 46])

  // 烟雨桥（右侧，跨在水线上）
  cv.rect(700, 508, 1060, 520, ink)
  cv.arch(880, 508, 148, 11, ink)
  for (const px of [726, 880, 1034]) {
    cv.rect(px - 4, 462, px + 4, 508, ink)
  }
  cv.rect(700, 458, 1060, 464, ink)

  // 水面
  verticalBands(cv, waterY, h, [
    [0, hex('#4e9285')], [0.3, hex('#3d8677')], [0.62, hex('#2e7d6d')], [1, hex('#1d5044')],
  ])

  // 摇橹船（左侧水面，画在水面之后）
  const boat = hex('#1e453c')
  cv.lens(240, 372, 592, 16, 9, boat, 1.4)
  cv.rect(286, 566, 320, 588, boat)
  cv.line(330, 538, 330, 566, boat, 2)
  cv.line(356, 570, 404, 590, boat, 2)
  cv.disc(300, 556, 5, boat)

  // 日光倒影（日轮正下方的碎光柱）
  for (let y = waterY + 10; y < h - 14; y += 26) {
    const halfW = Math.round(10 + 14 * Math.abs(Math.sin(y / 52)))
    cv.rect(sun.x - halfW, y, sun.x + halfW, y + 13, [232, 169, 95, 150])
  }
  // 散碎波光
  const shimmer = [255, 255, 255, 40]
  for (let y = waterY + 8; y < h - 10; y += 22) {
    for (let k = 0; k < 5; k++) {
      const x = ((y * 13 + k * 271) % (w - 90)) + 40
      cv.rect(x, y, x + 34 + (k % 3) * 14, y + 3, shimmer)
    }
  }

  cv.save(join(ROOT, 'bg', `${name}.jpg`))
}

/* ---------- 生成 ---------- */

for (let i = 0; i < 12; i++) drawSpot(i)
for (let i = 0; i < 20; i++) drawExh(i)
for (let i = 0; i < 10; i++) drawAvatar(i)
drawLoginBg('login', false) // 暮色
drawLoginBg('register', true) // 冷晨
console.log('done: spot ×12, exh ×20, avatar ×10, bg ×2 →', ROOT)
