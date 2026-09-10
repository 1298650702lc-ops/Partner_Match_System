<template>
  <main class="user-team-page">
    <header class="toolbar">
      <h2>我的队伍</h2>
      <van-button icon="plus" type="primary" size="small" @click="router.push('/team/add')">创建队伍</van-button>
    </header>
    <van-loading v-if="loading" class="loading" vertical>加载中...</van-loading>
    <van-empty v-else-if="errorMessage" :description="errorMessage">
      <van-button size="small" @click="loadTeams">重试</van-button>
    </van-empty>
    <van-empty v-else-if="!teams.length" description="暂无加入或创建的队伍">
      <van-button size="small" type="primary" @click="router.push('/team')">去队伍广场看看</van-button>
    </van-empty>
    <template v-else>
      <article v-for="team in teams" :key="team.id" class="team-row">
        <div class="team-heading">
          <h3>{{ team.name || '未命名队伍' }}</h3>
          <van-tag :type="team.status === 2 ? 'warning' : 'primary'">{{ statusNames[team.status] || '未知状态' }}</van-tag>
          <van-tag v-if="isOwner(team)" plain type="success" class="owner-tag">我创建的</van-tag>
        </div>
        <p class="description">{{ team.description || '暂无描述' }}</p>
        <div class="creator">
          <van-image :src="avatar(team)" width="32" height="32" fit="cover" alt="创建者头像" />
          <span>{{ team.createUser?.username || '未设置昵称' }}</span>
        </div>
        <div class="metadata"><span>人数上限 {{ team.maxNum }}</span><span>截止时间：{{ formatDate(team.expireTime) }}</span></div>
        <div class="actions">
          <van-button icon="friends-o" size="small" plain type="primary" :disabled="busyId !== null"
            @click="memberTeam = team">查看成员</van-button>
          <!-- 队长：编辑 / 退出（转让队长）/ 解散 -->
          <template v-if="isOwner(team)">
            <van-button size="small" plain type="primary" :disabled="busyId !== null" @click="toEdit(team)">编辑</van-button>
            <van-button size="small" plain type="danger" :disabled="busyId !== null"
              :loading="busyId === team.id && busyAction === 'quit'" @click="startQuit(team)">退出队伍</van-button>
            <van-button size="small" type="danger" :disabled="busyId !== null"
              :loading="busyId === team.id && busyAction === 'disband'" @click="startDisband(team)">解散队伍</van-button>
          </template>
          <!-- 普通成员：退出队伍 -->
          <van-button v-else size="small" type="danger" :disabled="busyId !== null"
            :loading="busyId === team.id" @click="startQuit(team)">退出队伍</van-button>
        </div>
      </article>
    </template>
    <TeamMembersDialog v-if="memberTeam" :team="memberTeam" @close="memberTeam = null" />
  </main>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import myAxios from '../Axios/myAxios'
import { useUserStore } from '../stores/userStore'
import { TEAM_EDIT_STORAGE_KEY, type Team } from '../models/team'
import TeamMembersDialog from '../components/TeamMembersDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const teams = ref<Team[]>([])
const loading = ref(true)
const errorMessage = ref('')
const busyId = ref<number | null>(null)
// 正在进行的操作类型，用于让同一队伍的多个按钮只有被点击的那个显示 loading
const busyAction = ref<'quit' | 'disband' | null>(null)
const memberTeam = ref<Team | null>(null)
const statusNames: Record<number, string> = { 0: '公开', 1: '私有', 2: '加密' }
let requestNumber = 0
onBeforeUnmount(() => { requestNumber++ })

// 我加入 + 我创建的队伍：addTeam 会把队长写入 user_team，所以这一个接口覆盖两种情况
const loadTeams = async () => {
  const currentRequest = ++requestNumber
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await myAxios.get('/team/list/my/join')
    if (currentRequest !== requestNumber) return
    const body = response.data
    if (body.code !== 0) {
      teams.value = []
      if (body.code !== 40100) errorMessage.value = body.description || body.message || '加载失败'
      return
    }
    if (!Array.isArray(body.data)) throw new Error('Invalid team list')
    teams.value = body.data
  } catch {
    if (currentRequest === requestNumber) {
      teams.value = []
      errorMessage.value = '加载队伍失败，请重试'
    }
  } finally {
    if (currentRequest === requestNumber) loading.value = false
  }
}
const avatar = (team: Team) => team.createUser?.avatarUrl?.trim().replace(/^["']|["']$/g, '') || '/default-avatar.svg'
const formatDate = (value?: string | null) => {
  if (!value) return '长期有效'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '未设置' : date.toLocaleString('zh-CN')
}
const isOwner = (team: Team) => team.userId === userStore.user.value?.id

// 退出队伍：二次确认 -> POST /team/quit -> 重新拉取列表
const quitTeam = async (team: Team) => {
  if (busyId.value !== null) return
  busyId.value = team.id
  busyAction.value = 'quit'
  try {
    const response = await myAxios.post('/team/quit', { teamId: team.id })
    const body = response.data
    if (body.code !== 0 || body.data !== true) {
      if (body.code !== 40100) showFailToast(body.description || body.message || '退出失败')
      return
    }
    showSuccessToast('已退出队伍')
    // 队长退出可能导致队伍解散或队长转让，直接刷新列表保证展示与后端一致
    await loadTeams()
  } catch {
    showFailToast('请求失败，请稍后重试')
  } finally {
    busyId.value = null
    busyAction.value = null
  }
}
const startQuit = async (team: Team) => {
  const owner = isOwner(team)
  try {
    await showConfirmDialog({
      title: '退出队伍',
      message: owner
        ? `你是「${team.name || '未命名队伍'}」的队长，退出后队长将转让给最早加入的成员；若队伍只有你一人则会直接解散。确定退出吗？`
        : `确定退出「${team.name || '未命名队伍'}」吗？`,
      confirmButtonColor: '#ee0a24',
    })
  } catch {
    return // 用户取消
  }
  await quitTeam(team)
}
// 解散队伍（仅队长）：二次确认 -> DELETE /team/delete?id= -> 刷新列表
const disbandTeam = async (team: Team) => {
  if (busyId.value !== null) return
  busyId.value = team.id
  busyAction.value = 'disband'
  try {
    const response = await myAxios.delete('/team/delete', { params: { id: team.id } })
    const body = response.data
    if (body.code !== 0 || body.data !== true) {
      if (body.code !== 40100) showFailToast(body.description || body.message || '解散失败')
      return
    }
    showSuccessToast('队伍已解散')
    await loadTeams()
  } catch {
    showFailToast('请求失败，请稍后重试')
  } finally {
    busyId.value = null
    busyAction.value = null
  }
}
const startDisband = async (team: Team) => {
  try {
    await showConfirmDialog({
      title: '解散队伍',
      message: `解散后「${team.name || '未命名队伍'}」将被删除，所有成员会被移出，且无法恢复。确定解散吗？`,
      confirmButtonText: '确认解散',
      confirmButtonColor: '#ee0a24',
    })
  } catch {
    return // 用户取消
  }
  await disbandTeam(team)
}
// 编辑队伍（仅队长）：把队伍数据暂存到 sessionStorage 带到编辑页，避免调用会暴露密码的 /team/get
const toEdit = (team: Team) => {
  sessionStorage.setItem(TEAM_EDIT_STORAGE_KEY, JSON.stringify(team))
  router.push({ path: '/team/edit', query: { id: String(team.id) } })
}
onMounted(loadTeams)
</script>

<style scoped>
.user-team-page { max-width: 900px; margin: auto; padding: 16px 0 24px; text-align: left; }
.toolbar, .team-heading, .creator, .metadata, .actions { display: flex; align-items: center; gap: 12px; }
.toolbar { padding: 0 16px 8px; justify-content: space-between; }
h2 { margin: 0; font-size: 20px; letter-spacing: 0; }
h3 { margin: 0; font-size: 16px; overflow-wrap: anywhere; }
.team-row { padding: 20px 16px; border-bottom: 1px solid #ebedf0; }
.team-heading { flex-wrap: wrap; }
.owner-tag { margin-left: auto; }
.description { margin: 12px 0; white-space: pre-wrap; overflow-wrap: anywhere; color: #646566; font-size: 14px; }
.creator { font-size: 14px; overflow-wrap: anywhere; }
.metadata { flex-wrap: wrap; margin-top: 12px; font-size: 12px; color: #646566; }
.actions { justify-content: flex-end; flex-wrap: wrap; gap: 8px; margin-top: 12px; min-height: 32px; }
.loading { padding: 40px 0; }
</style>
