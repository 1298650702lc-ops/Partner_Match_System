<template>
  <main class="register-page">
    <h2 class="register-title">注册账号</h2>
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号（至少 4 位）"
          autocomplete="username"
          :rules="accountRules"
        />
        <van-field
          v-model="form.userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码（至少 8 位）"
          autocomplete="new-password"
          :rules="passwordRules"
        />
        <van-field
          v-model="form.checkPassword"
          type="password"
          name="checkPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          autocomplete="new-password"
          :rules="checkPasswordRules"
        />
        <van-field
          v-model="form.planetCode"
          name="planetCode"
          label="星球编号"
          placeholder="请输入星球编号（1～5 位）"
          maxlength="5"
          :rules="planetCodeRules"
        />
      </van-cell-group>

      <div class="form-actions">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="submitting"
          :disabled="submitting"
        >
          注册
        </van-button>
        <van-button
          round
          block
          plain
          type="primary"
          native-type="button"
          :disabled="submitting"
          @click="router.replace('/user/login')"
        >
          返回登录
        </van-button>
      </div>
    </van-form>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showFailToast, showSuccessToast } from 'vant'
import { useRouter } from 'vue-router'
import myAxios from '../Axios/myAxios'

const router = useRouter()
const submitting = ref(false)
const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  planetCode: '',
})

const accountRules = [
  { required: true, message: '请输入账号' },
  { pattern: /^.{4,}$/, message: '账号至少需要 4 位' },
]

const passwordRules = [
  { required: true, message: '请输入密码' },
  { pattern: /^.{8,}$/, message: '密码至少需要 8 位' },
]

const checkPasswordRules = [
  { required: true, message: '请确认密码' },
  {
    validator: (value: string) => value === form.value.userPassword,
    message: '两次输入的密码不一致',
  },
]

const planetCodeRules = [
  { required: true, message: '请输入星球编号' },
  { pattern: /^.{1,5}$/, message: '星球编号长度为 1～5 位' },
]

const onSubmit = async () => {
  if (submitting.value) {
    return
  }

  submitting.value = true
  try {
    const response = await myAxios.put('/user/register', {
      userAccount: form.value.userAccount.trim(),
      userPassword: form.value.userPassword,
      checkPassword: form.value.checkPassword,
      planetCode: form.value.planetCode.trim(),
    })
    const body = response.data
    if (body.code !== 0 || body.data == null || Number(body.data) <= 0) {
      showFailToast(body.message || '注册失败')
      return
    }

    showSuccessToast('注册成功，请登录')
    await router.replace('/user/login')
  } catch (error) {
    console.error('注册请求失败', error)
    showFailToast('注册失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-page {
  padding-top: 28px;
}

.register-title {
  margin: 0 0 24px;
  color: #323233;
  font-size: 24px;
  text-align: center;
}

.form-actions {
  display: grid;
  gap: 12px;
  margin: 20px 16px 16px;
}
</style>
