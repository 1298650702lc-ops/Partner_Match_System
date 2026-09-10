<template>
  <van-popup :show="true" position="bottom" class="members-popup" :close-on-popstate="true"
    @update:show="onVisibilityChange">
    <section class="members-panel" role="dialog" aria-modal="true" aria-labelledby="members-title">
      <header class="members-header">
        <div class="heading-text">
          <h2 id="members-title">队伍成员</h2>
          <p>{{ team.name || '未命名队伍' }}</p>
        </div>
        <van-button icon="cross" aria-label="关闭成员列表" title="关闭" size="small" @click="emit('close')" />
      </header>
      <div class="members-content">
        <van-form v-if="needsPassword" class="password-form" @submit="submitPassword">
          <van-field v-model="password" type="password" label="队伍密码" maxlength="32"
            placeholder="请输入队伍密码" :disabled="loading" :rules="[{ validator: hasPassword, message: '请输入队伍密码' }]" />
          <van-button block type="primary" native-type="submit" :loading="loading" :disabled="loading">验证密码</van-button>
        </van-form>
        <van-loading v-if="loading" class="state" vertical>加载成员中...</van-loading>
        <van-empty v-else-if="errorMessage" :description="errorMessage">
          <van-button v-if="canRetry && !needsPassword" size="small" @click="loadMembers(retryPage)">重试</van-button>
        </van-empty>
        <van-empty v-else-if="!members.length" description="暂无成员" />
        <ul v-else class="member-list">
          <li v-for="(member, index) in members" :key="`${member.id}-${index}`" class="member-row">
            <van-image :src="member.avatarUrl" width="48" height="48" fit="cover" alt="成员头像" @error="useDefaultAvatar(member)" />
            <div class="member-info">
              <div class="member-name">
                <h3>{{ member.username || '未设置昵称' }}</h3>
                <van-tag v-if="member.id === team.userId" type="success" plain>队长</van-tag>
              </div>
              <p v-if="member.profile" class="profile">{{ member.profile }}</p>
              <div v-if="member.tags.length" class="tags">
                <van-tag v-for="tag in member.tags" :key="tag" plain type="primary">{{ tag }}</van-tag>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <footer v-if="hasResult && !errorMessage && !needsPassword" class="members-footer">
        <p>共 {{ total }} 位成员</p>
        <van-pagination v-if="pages > 1" :model-value="pageNum" :page-count="pages" mode="simple"
          :disabled="loading" @change="loadMembers" />
      </footer>
    </section>
  </van-popup>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import myAxios from '../Axios/myAxios'
import type { Team } from '../models/team'

type Member = { id: number; username?: string | null; avatarUrl: string; profile?: string | null; tags: string[] }
type ApiMember = Omit<Member, 'tags' | 'avatarUrl'> & { tags?: string | string[] | null; avatarUrl?: string | null }
type MemberPage = { records: ApiMember[]; current: number; size: number; total: number; pages: number }
type ResponseBody = { code: number; data: MemberPage | null; description?: string; message?: string }

const props = defineProps<{ team: Team }>()
const emit = defineEmits<{ close: [] }>()
const members = ref<Member[]>([])
const loading = ref(false)
const errorMessage = ref('')
const canRetry = ref(true)
const needsPassword = ref(false)
const password = ref('')
const pageNum = ref(1)
const total = ref(0)
const pages = ref(0)
const retryPage = ref(1)
const hasResult = ref(false)
const pageSize = 8
const defaultAvatar = '/default-avatar.svg'
let requestController: AbortController | undefined
let disposed = false

const parseTags = (raw: ApiMember['tags']): string[] => {
  try {
    const tags: unknown = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(tags) ? [...new Set(tags.filter((tag): tag is string => typeof tag === 'string' && Boolean(tag.trim())))] : []
  } catch { return [] }
}
const useDefaultAvatar = (member: Member) => {
  if (member.avatarUrl !== defaultAvatar) member.avatarUrl = defaultAvatar
}
const hasPassword = () => Boolean(password.value.trim())
const onVisibilityChange = (visible: boolean) => { if (!visible) emit('close') }

const loadMembers = async (requestedPage = 1) => {
  if (loading.value || disposed) return
  loading.value = true
  retryPage.value = requestedPage
  errorMessage.value = ''
  canRetry.value = true
  requestController = new AbortController()
  try {
    const response = await myAxios.get<ResponseBody>('/team/list/user', {
      params: { teamId: props.team.id, pageNum: requestedPage, pageSize,
        ...(password.value ? { password: password.value } : {}) },
      signal: requestController.signal,
    })
    if (disposed) return
    const body = response.data
    if (body.code !== 0) {
      members.value = []
      hasResult.value = false
      if (body.code === 40100) { emit('close'); return }
      errorMessage.value = body.description || body.message || '获取成员失败'
      needsPassword.value = body.code === 40000 && errorMessage.value.includes('密码')
      canRetry.value = body.code !== 40101 && body.code !== 40001
      return
    }
    const page = body.data
    if (!page || !Array.isArray(page.records) || !Number.isInteger(page.current) || page.current < 1
      || !Number.isInteger(page.pages) || page.pages < 0 || !Number.isInteger(page.total) || page.total < 0) {
      throw new Error('Invalid member page')
    }
    members.value = page.records.map(member => ({
      id: member.id, username: member.username, profile: member.profile,
      avatarUrl: member.avatarUrl?.trim().replace(/^["']|["']$/g, '') || defaultAvatar,
      tags: parseTags(member.tags),
    }))
    pageNum.value = page.current
    pages.value = page.pages
    total.value = page.total
    hasResult.value = true
    needsPassword.value = false
  } catch {
    if (!disposed) {
      members.value = []
      errorMessage.value = '加载成员失败，请重试'
    }
  } finally {
    if (!disposed) loading.value = false
  }
}
const submitPassword = () => { if (hasPassword()) void loadMembers(1) }
onMounted(() => loadMembers())
onBeforeUnmount(() => {
  disposed = true
  requestController?.abort()
  password.value = ''
})
</script>

<style scoped>
.members-popup { height: 80dvh; max-height: 760px; width: 100%; }
.members-panel { display: flex; flex-direction: column; height: 100%; max-width: 900px; margin: auto; text-align: left; color: #323233; }
.members-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; padding: 16px; border-bottom: 1px solid #ebedf0; }
.heading-text { min-width: 0; }
h2 { font-size: 18px; margin: 0; letter-spacing: 0; }
.heading-text p { margin: 6px 0 0; font-size: 13px; overflow-wrap: anywhere; color: #646566; }
.members-header .van-button { flex-shrink: 0; width: 32px; }
.members-content { flex: 1; min-height: 0; overflow-y: auto; }
.state { padding: 40px 0; }
.member-list { list-style: none; padding: 0; margin: 0; }
.member-row { display: flex; align-items: flex-start; gap: 12px; padding: 16px; border-bottom: 1px solid #ebedf0; }
.member-row .van-image { flex-shrink: 0; }
.member-info { flex: 1; min-width: 0; overflow-wrap: anywhere; }
.member-name { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
h3 { margin: 0; font-size: 15px; letter-spacing: 0; }
.profile { margin: 8px 0; font-size: 13px; color: #646566; white-space: pre-wrap; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.members-footer { padding: 12px 16px max(16px, env(safe-area-inset-bottom)); border-top: 1px solid #ebedf0; }
.members-footer p { text-align: center; margin: 0 0 8px; font-size: 13px; color: #646566; }
.password-form { padding: 16px; }
</style>
