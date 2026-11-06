<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="h5-shell">
    <main class="h5-page">
      <RouterView v-slot="{ Component }">
        <keep-alive include="HomeView,GuideView,ReservesView">
          <component :is="Component" />
        </keep-alive>
      </RouterView>
    </main>
    <van-tabbar v-if="route.meta.tabbar" route placeholder>
      <van-tabbar-item to="/home" icon="wap-home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/guide" icon="guide-o">导览</van-tabbar-item>
      <van-tabbar-item to="/reserves" icon="orders-o">预约</van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped lang="scss">
.h5-shell {
  min-height: 100vh;
  background: var(--sg-bg);
}

.h5-page {
  max-width: 640px; // H5 容器上限，桌面浏览器居中模拟手机
  margin: 0 auto;
  min-height: 100vh;
  background: var(--sg-surface);
}
</style>
