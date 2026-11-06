<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from '@/utils/message'
import type { LoginForm } from '@/types/api'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 预填管理员账号方便联调
const form = reactive<LoginForm>({ username: 'admin', password: '123456' })
const submitting = ref(false)

async function onSubmit() {
  submitting.value = true
  try {
    const result = await login(form)
    auth.setLogin(result.token, result.user)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin'
    router.push(redirect)
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <!-- 纸感极简：浅灰绿底 + 居中白卡 -->
  <div class="admin-login">
    <el-card class="al-card">
      <div class="al-mark" aria-hidden="true"></div>
      <h1 class="al-title">管理端 · 登 录</h1>
      <el-form label-width="64px" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin / checkin01" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button class="al-submit" type="primary" native-type="submit" :loading="submitting">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--sg-bg);
}

.al-card {
  width: 380px;
  border: none;
  border-radius: var(--sg-radius);
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow-pop);
  padding: 10px;
}

.al-mark {
  width: 34px;
  height: 34px;
  margin: 0 auto 14px;
  border-radius: 10px;
  background: var(--sg-cover-gradient);
}

.al-title {
  font-family: var(--sg-font-display);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-align: center;
  margin: 0 0 24px;
}

.al-submit {
  width: 100%;
}
</style>
