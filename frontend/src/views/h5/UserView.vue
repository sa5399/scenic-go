<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { roleText } from '@/utils/format'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()

async function onLogout() {
  try {
    await logoutApi() // 401 时拦截器已清态并跳登录页
  } finally {
    auth.logoutLocal()
    if (router.currentRoute.value.name !== 'login') {
      router.push('/login')
    }
  }
}
</script>

<template>
  <div class="user-page">
    <template v-if="auth.user">
      <div class="user-card">
        <van-image round width="56" height="56" :src="auth.user.icon || undefined">
          <template #error>
            <div class="user-avatar-fallback">{{ auth.user.nickName.slice(0, 1) }}</div>
          </template>
        </van-image>
        <div class="user-info">
          <div class="user-name">{{ auth.user.nickName }}</div>
          <div class="user-role">{{ roleText[auth.user.role] }} · @{{ auth.user.username }}</div>
        </div>
      </div>
      <van-cell-group inset class="user-cells">
        <van-cell title="我的评价" is-link to="/my-reviews" />
        <van-cell title="景区公告" is-link to="/announcements" />
      </van-cell-group>
      <div class="user-actions">
        <van-button block round type="danger" plain @click="onLogout">退出登录</van-button>
      </div>
    </template>
    <template v-else>
      <van-empty description="未登录">
        <van-button round type="primary" to="/login">去登录</van-button>
      </van-empty>
    </template>
  </div>
</template>

<style scoped lang="scss">
.user-page {
  padding: 24px 0;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0 16px 16px;
  padding: 16px;
  border-radius: var(--sg-radius);
  background: var(--sg-surface);
}

.user-avatar-fallback {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--sg-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.user-name {
  font-size: 17px;
  font-weight: 600;
}

.user-role {
  font-size: 13px;
  color: var(--sg-text-secondary);
  margin-top: 4px;
}

.user-cells {
  margin-bottom: 24px;
}

.user-actions {
  margin: 0 16px;
}
</style>
