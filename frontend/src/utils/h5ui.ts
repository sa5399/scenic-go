import { showConfirmDialog, showToast } from 'vant'
import 'vant/es/dialog/style'
import 'vant/es/toast/style'

/**
 * H5 端统一弹层/提示。
 * showToast / showConfirmDialog 是函数式组件，样式需手动引入一次，这里统一收口
 *（与 utils/message.ts 对 Element Plus 的处理一致）。
 */
export { showConfirmDialog, showToast }
