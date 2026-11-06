<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { RegisterForm } from '@/types/api'
import { register } from '@/api/auth'

const router = useRouter()
const form = ref<RegisterForm>({ username: '', password: '', nickName: '', phone: '' })
const submitting = ref(false)
const error = ref('')

async function onSubmit() {
  if (!form.value.username || !form.value.password) {
    error.value = '用户名和密码必填'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    await register({
      ...form.value,
      nickName: form.value.nickName || undefined,
      phone: form.value.phone || undefined,
    })
    router.push('/login')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <!-- 与登录页同构的纸感极简 -->
  <div class="register">
    <div class="register-card">
      <div class="register-mark" aria-hidden="true"></div>
      <h1 class="register-title">注 册</h1>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field v-model="form.username" label="用户名" placeholder="登录用户名" required />
          <van-field v-model="form.password" type="password" label="密码" placeholder="密码" required />
          <van-field v-model="form.nickName" label="昵称" placeholder="不填默认用用户名" />
          <van-field v-model="form.phone" label="手机号" placeholder="选填" />
        </van-cell-group>
        <p v-if="error" class="register-error">{{ error }}</p>
        <div class="register-actions">
          <van-button type="primary" round block native-type="submit" :loading="submitting">
            注册
          </van-button>
        </div>
      </van-form>
      <p class="register-hint">
        已有账号？<router-link to="/login" class="register-link">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.register {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--sg-bg);
}

.register-card {
  width: 100%;
  max-width: 380px;
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  padding: 30px 8px 20px;
  box-shadow: var(--sg-shadow-pop);
}

.register-mark {
  width: 34px;
  height: 34px;
  margin: 0 auto 14px;
  border-radius: 10px;
  background: var(--sg-cover-gradient);
}

.register-title {
  font-family: var(--sg-font-display);
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0.3em;
  text-indent: 0.3em;
  text-align: center;
  margin: 0;
}

.register-sub {
  margin: 6px 0 18px;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.22em;
  color: var(--sg-text-secondary);
}

.register-error {
  color: var(--sg-danger);
  font-size: 13px;
  text-align: center;
  margin: 12px 0 0;
}

.register-actions {
  margin: 20px 16px 0;
}

.register-link {
  color: var(--sg-primary);
  text-decoration: none;
}
</style>
