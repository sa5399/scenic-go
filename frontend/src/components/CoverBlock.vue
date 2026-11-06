<script setup lang="ts">
/**
 * 封面图统一占位：图片缺失/加载中时显示名称首字色块。
 * 尺寸（width/height/radius/round 等）由使用方以属性透传给 van-image。
 */
defineProps<{ src?: string | null; label?: string }>()
</script>

<template>
  <van-image :src="src || undefined" fit="cover">
    <template #error>
      <div class="cover-fallback">{{ (label ?? '景').slice(0, 1) }}</div>
    </template>
    <template #loading>
      <div class="cover-fallback is-loading" />
    </template>
  </van-image>
</template>

<style scoped lang="scss">
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--sg-cover-gradient);
  color: var(--sg-primary);
  font-size: 24px;
  font-weight: 600;

  &.is-loading {
    background: var(--sg-fill);
  }
}
</style>
