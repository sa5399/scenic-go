<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from '@/utils/message'
import { useAuthStore } from '@/stores/auth'
import { roleText } from '@/utils/format'
import { logout as logoutApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

async function onLogout() {
  try {
    await logoutApi() // 401 时拦截器已清态跳登录，这里不会再走
  } finally {
    auth.logoutLocal()
  }
  ElMessage.success('已退出登录')
  router.push('/admin/login')
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-brand">
        <span class="admin-logo" aria-hidden="true"></span>
        <span>管理端</span>
      </div>
      <el-menu router :default-active="route.path" class="admin-menu">
        <el-menu-item index="/admin/checkin">核销工作台</el-menu-item>
        <el-menu-item v-if="auth.hasRole([3])" index="/admin/checkin/logs">核销流水</el-menu-item>
        <el-menu-item v-if="auth.hasRole([3])" index="/admin/spots">景点管理</el-menu-item>
        <el-menu-item v-if="auth.hasRole([3])" index="/admin/slots">时段与库存</el-menu-item>
        <el-menu-item v-if="auth.hasRole([3])" index="/admin/orders">订单查询</el-menu-item>
        <el-menu-item v-if="auth.hasRole([3])" index="/admin/announcements">公告维护</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <span>{{ route.meta.title }}</span>
        <div v-if="auth.user" class="admin-user">
          <span>{{ auth.user.nickName }}（{{ roleText[auth.user.role] }}）</span>
          <el-button size="small" @click="onLogout">登出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.admin-shell {
  height: 100vh;
}

.admin-aside {
  border-right: 1px solid var(--el-border-color-light);
}

.admin-brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: var(--sg-font-display);
  font-weight: 700;
  font-size: 16px;
}

.admin-logo {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: linear-gradient(160deg, #e9f1ef 0 30%, #6fa093 30% 55%, #2e7d6d 55% 80%, #235e52 80%);
}

.admin-menu {
  border-right: none;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
  font-weight: 600;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 400;
  font-size: 14px;
}

.admin-main {
  background: var(--sg-bg);
}
</style>
