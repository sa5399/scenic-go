<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LoginForm } from '@/types/api'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 预填测试账号方便联调
const form = ref<LoginForm>({ username: 'linxiaoyu', password: '123456' })
const submitting = ref(false)
const error = ref('')

async function onSubmit() {
  if (!form.value.username || !form.value.password) {
    error.value = '请输入用户名和密码'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const result = await login(form.value)
    auth.setLogin(result.token, result.user)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <!-- 纸感极简：浅湖绿纸底 + 居中白卡，排版即视觉 -->
  <div class="login">
    <div class="login-card">
      <div class="login-mark" aria-hidden="true"></div>
      <h1 class="login-title">登 录</h1>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field v-model="form.username" label="用户名" placeholder="请输入用户名" />
          <van-field
            v-model="form.password"
            type="password"
            label="密码"
            placeholder="请输入密码"
          />
        </van-cell-group>
        <p v-if="error" class="login-error">{{ error }}</p>
        <div class="login-actions">
          <van-button type="primary" round block native-type="submit" :loading="submitting">
            登录
          </van-button>
          <van-button type="default" round block plain to="/register" class="login-register">
            注册新账号
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--sg-bg);
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  padding: 30px 8px 20px;
  box-shadow: var(--sg-shadow-pop);
}

.login-mark {
  width: 34px;
  height: 34px;
  margin: 0 auto 14px;
  border-radius: 10px;
  background: var(--sg-cover-gradient);
}

.login-title {
  font-family: var(--sg-font-display);
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0.3em;
  text-indent: 0.3em; // 平衡字距造成的偏移
  text-align: center;
  margin: 0;
}

.login-sub {
  margin: 6px 0 18px;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.22em;
  color: var(--sg-text-secondary);
}

.login-error {
  color: var(--sg-danger);
  font-size: 13px;
  text-align: center;
  margin: 12px 0 0;
}

.login-actions {
  margin: 20px 16px 0;
}

.login-register {
  margin-top: 12px;
  color: var(--sg-primary);
  border-color: var(--sg-primary);
}
</style>
