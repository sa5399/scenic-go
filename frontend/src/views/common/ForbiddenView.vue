<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="forbidden">
    <h1>403 · 没有权限</h1>
    <p>当前账号角色不足以访问该页面。</p>
    <van-button round type="primary" plain @click="router.back()">返回</van-button>
  </div>
</template>

<style scoped lang="scss">
.forbidden {
  padding: 96px 16px;
  text-align: center;

  p {
    color: var(--sg-text-secondary);
    margin-bottom: 24px;
  }
}
</style>
