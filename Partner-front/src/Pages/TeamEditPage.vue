<template>
  <main class="team-edit-page">
    <h2>编辑队伍</h2>
    <van-empty v-if="!team" description="未找到队伍信息，请从队伍列表重新进入">
      <van-button size="small" type="primary" @click="router.replace('/team')">返回队伍列表</van-button>
    </van-empty>
    <van-form v-else @submit="onSubmit" :disabled="submitting">
      <van-cell-group>
        <van-field v-model="form.name" name="name" label="队伍名" placeholder="请输入队伍名"
          maxlength="20" :rules="[{ validator: hasText, message: '请输入队伍名' }]" />
        <van-field v-model="form.description" name="description" label="队伍描述" type="textarea"
          rows="3" maxlength="512" show-word-limit placeholder="请输入队伍描述"
          :rules="[{ validator: hasText, message: '请输入队伍描述' }]" />
        <van-field label="设置过期时间">
          <template #input>
            <van-switch v-model="hasExpiry" size="22" :disabled="submitting || originalHasExpiry" aria-label="设置过期时间" />
          </template>
        </van-field>
        <van-field v-if="hasExpiry" label="过期时间" name="expireTime"
          :rules="[{ validator: validExpiry, message: '请选择未来的时间' }]">
          <template #input>
            <input v-model="expiry" class="date-input" type="datetime-local" aria-label="过期时间" :disabled="submitting" />
          </template>
        </van-field>
        <!-- 后端更新接口会忽略空值，已设置的过期时间只能修改、不能清除 -->
        <div v-if="originalHasExpiry" class="hint">已设置的过期时间暂不支持取消，只能修改。</div>
        <van-field label="最大人数">
          <template #input><span class="readonly-value">{{ team.maxNum }} 人（暂不支持修改）</span></template>
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
          label="队伍密码" maxlength="32"
          :placeholder="originallySecret ? '留空则保持原密码' : '请输入队伍密码'"
          :rules="[{ validator: validPassword, message: '请填写队伍密码' }]" />
      </van-cell-group>
      <div class="submit-row">
        <van-button block type="primary" native-type="submit" :loading="submitting" :disabled="submitting">保存修改</van-button>
        <van-button block plain class="cancel-btn" :disabled="submitting" @click="router.back()">取消</van-button>
      </div>
    </van-form>
  </main>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import myAxios from '../Axios/myAxios'
import { TEAM_EDIT_STORAGE_KEY, type Team } from '../models/team'

const router = useRouter()
const route = useRoute()

// 从列表页通过 sessionStorage 带过来的队伍数据；校验 id 与路由一致，防止串数据
const readTeam = (): Team | null => {
  try {
    const raw = sessionStorage.getItem(TEAM_EDIT_STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Team
    const routeId = Number(route.query.id)
    if (!parsed?.id || (routeId && parsed.id !== routeId)) return null
    return parsed
  } catch {
    return null
  }
}
const team = ref<Team | null>(readTeam())

// Date -> <input type="datetime-local"> 需要的本地时间格式 yyyy-MM-ddTHH:mm
const toLocalInput = (value?: string | null) => {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const submitting = ref(false)
const originalHasExpiry = Boolean(team.value?.expireTime)
const originallySecret = team.value?.status === 2
const hasExpiry = ref(originalHasExpiry)
const expiry = ref(toLocalInput(team.value?.expireTime))
const form = reactive({
  name: team.value?.name ?? '',
  description: team.value?.description ?? '',
  status: team.value?.status ?? 0,
  password: '',
})

const hasText = (value: string) => Boolean(value?.trim())
const validExpiry = () => !hasExpiry.value || (Boolean(expiry.value) && new Date(expiry.value).getTime() > Date.now())
// 原本就是加密队伍时允许密码留空（后端沿用旧密码）；从其它状态切到加密则必须填
const passwordRequired = computed(() => form.status === 2 && !originallySecret)
const validPassword = () => !passwordRequired.value || hasText(form.password)

const onSubmit = async () => {
  if (submitting.value || !team.value) return
  if (!hasText(form.name) || !hasText(form.description) || !validExpiry()
    || ![0, 1, 2].includes(form.status) || !validPassword()) {
    showFailToast('请检查队伍信息')
    return
  }
  submitting.value = true
  try {
    const response = await myAxios.put('/team/update', {
      id: team.value.id,
      name: form.name.trim(),
      description: form.description.trim(),
      status: form.status,
      ...(hasExpiry.value ? { expireTime: new Date(expiry.value).toISOString() } : {}),
      ...(form.status === 2 && hasText(form.password) ? { password: form.password } : {}),
    })
    const body = response.data
    if (body.code !== 0 || body.data !== true) {
      if (body.code !== 40100) showFailToast(body.description || body.message || '保存失败')
      return
    }
    sessionStorage.removeItem(TEAM_EDIT_STORAGE_KEY)
    showSuccessToast('保存成功')
    await router.replace('/team')
  } catch {
    showFailToast('请求失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.team-edit-page { max-width: 720px; margin: auto; padding: 16px 0 24px; text-align: left; }
h2 { margin: 0 16px 16px; font-size: 20px; letter-spacing: 0; }
.submit-row { margin: 24px 16px; display: flex; flex-direction: column; gap: 12px; }
.date-input { width: 100%; min-width: 0; box-sizing: border-box; border: 0; background: transparent; font: inherit; color: inherit; }
.readonly-value { color: #969799; }
.hint { padding: 4px 16px 8px; font-size: 12px; color: #969799; }
:deep(.van-radio-group) { flex-wrap: wrap; gap: 8px 0; }
</style>
