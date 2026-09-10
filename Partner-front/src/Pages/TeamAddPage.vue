<template>
  <main class="team-add-page">
    <h2>创建队伍</h2>
    <van-form @submit="onSubmit" :disabled="submitting">
      <van-cell-group>
        <van-field v-model="form.name" name="name" label="队伍名" placeholder="请输入队伍名"
          maxlength="20" :rules="[{ validator: hasText, message: '请输入队伍名' }]" />
        <van-field v-model="form.description" name="description" label="队伍描述" type="textarea"
          rows="3" maxlength="512" show-word-limit placeholder="请输入队伍描述"
          :rules="[{ validator: hasText, message: '请输入队伍描述' }]" />
        <van-field label="设置过期时间">
          <template #input><van-switch v-model="hasExpiry" size="22" :disabled="submitting" aria-label="设置过期时间" /></template>
        </van-field>
        <van-field v-if="hasExpiry" label="过期时间" name="expireTime"
          :rules="[{ validator: validExpiry, message: '请选择未来的时间' }]">
          <template #input>
            <input v-model="expiry" class="date-input" type="datetime-local" aria-label="过期时间" :disabled="submitting" />
          </template>
        </van-field>
        <van-field label="最大人数">
          <template #input><van-stepper v-model="form.maxNum" :min="2" :max="20" integer :disabled="submitting" /></template>
        </van-field>
        <van-field label="队伍状态">
          <template #input>
            <van-radio-group v-model="form.status" direction="horizontal" :disabled="submitting">
              <van-radio :name="0">公开</van-radio>
              <van-radio :name="1">私有</van-radio>
              <van-radio :name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-if="form.status === 2" v-model="form.password" name="password" type="password"
          label="队伍密码" maxlength="32" placeholder="请输入队伍密码"
          :rules="[{ validator: hasText, message: '请填写队伍密码' }]" />
      </van-cell-group>
      <div class="submit-row">
        <van-button block type="primary" native-type="submit" :loading="submitting" :disabled="submitting">创建队伍</van-button>
      </div>
    </van-form>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import myAxios from '../Axios/myAxios'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const hasExpiry = ref(false)
const expiry = ref('')
const form = reactive({ name: '', description: '', maxNum: 5, status: 0, password: '' })
const hasText = (value: string) => Boolean(value?.trim())
const validExpiry = () => !hasExpiry.value || (Boolean(expiry.value) && new Date(expiry.value).getTime() > Date.now())

const onSubmit = async () => {
  if (submitting.value) return
  if (!hasText(form.name) || !hasText(form.description) || !validExpiry()
    || !Number.isInteger(Number(form.maxNum)) || form.maxNum < 2 || form.maxNum > 20
    || ![0, 1, 2].includes(form.status) || (form.status === 2 && !hasText(form.password))) {
    showFailToast('请检查队伍信息和过期时间')
    return
  }
  submitting.value = true
  try {
    // 当前后端依赖请求中的 userId 创建队伍及成员关系。
    const currentUser = await userStore.fetchCurrentUser(true)
    if (!currentUser?.id) {
      showFailToast('无法确认登录状态，请重新登录')
      return
    }
    const response = await myAxios.post('/team/add', {
      name: form.name.trim(), description: form.description.trim(),
      maxNum: Number(form.maxNum), status: form.status, userId: currentUser.id,
      ...(hasExpiry.value ? { expireTime: new Date(expiry.value).toISOString() } : {}),
      ...(form.status === 2 ? { password: form.password } : {}),
    })
    const body = response.data
    if (body.code !== 0 || !body.data) {
      if (body.code !== 40100) showFailToast(body.description || body.message || '创建失败')
      return
    }
    showSuccessToast('创建成功')
    await router.replace('/team')
  } catch {
    showFailToast('请求失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.team-add-page { max-width: 720px; margin: auto; padding: 16px 0 24px; text-align: left; }
h2 { margin: 0 16px 16px; font-size: 20px; letter-spacing: 0; }
.submit-row { margin: 24px 16px; }
.date-input { width: 100%; min-width: 0; box-sizing: border-box; border: 0; background: transparent; font: inherit; color: inherit; }
:deep(.van-radio-group) { flex-wrap: wrap; gap: 8px 0; }
</style>
