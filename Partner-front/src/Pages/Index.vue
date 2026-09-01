<template>
  <main class="index-page">
    <UserCardList
      :user-list="userList"
      :loading="loading"
      empty-description="暂无推荐伙伴"
    />

    <section v-if="!loading && total > 0 && pageCount > 1" class="pagination-section">
      <div class="pagination-summary">共 {{ total }} 位伙伴</div>
      <van-pagination
        v-model="pageNum"
        :page-count="pageCount"
        :disabled="loading"
        mode="simple"
        @change="handlePageChange"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showFailToast } from 'vant'
import myAxios from '../Axios/myAxios'
import UserCardList from '../components/UserCardList.vue'

type SearchUser = {
  id: number
  username?: string
  avatarUrl?: string
  planetCode?: string
  profile?: string
  tags: string[]
}

type ApiUser = Omit<SearchUser, 'tags'> & {
  tags?: string | string[] | null
}

type PageResult<T> = {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

type ApiResponse<T> = {
  code: number
  data: T | null
  message: string
  description: string
}

const pageSize = 8
const pageNum = ref(1)
const pageCount = ref(0)
const total = ref(0)
const loading = ref(false)
const userList = ref<SearchUser[]>([])

const parseTags = (tags: ApiUser['tags']): string[] => {
  if (Array.isArray(tags)) {
    return tags.filter((tag): tag is string => typeof tag === 'string')
  }

  if (!tags) {
    return []
  }

  try {
    const parsedTags: unknown = JSON.parse(tags)
    return Array.isArray(parsedTags)
      ? parsedTags.filter((tag): tag is string => typeof tag === 'string')
      : []
  } catch {
    return []
  }
}

const loadRecommendedUsers = async (requestedPage = pageNum.value) => {
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    const response = await myAxios.get<ApiResponse<PageResult<ApiUser>>>('/user/recommend', {
      params: {
        pageSize,
        pageNum: requestedPage,
      },
    })
    const body = response.data

    if (body.code !== 0 || !body.data) {
      userList.value = []
      total.value = 0
      pageCount.value = 0
      showFailToast(body.message || '获取推荐伙伴失败')
      return
    }

    const pageData = body.data
    userList.value = (pageData.records ?? []).map((user) => ({
      id: user.id,
      username: user.username,
      avatarUrl: user.avatarUrl?.trim() || undefined,
      planetCode: user.planetCode,
      profile: user.profile,
      tags: parseTags(user.tags),
    }))
    pageNum.value = pageData.current || requestedPage
    pageCount.value = pageData.pages || 0
    total.value = pageData.total || 0
  } catch (error) {
    console.error('/user/recommend error', error)
    showFailToast('请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (nextPage: number) => {
  await loadRecommendedUsers(nextPage)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => loadRecommendedUsers())
</script>

<style scoped>
.index-page {
  padding-bottom: 84px;
}

.pagination-section {
  padding: 16px;
  background: #fff;
}

.pagination-summary {
  margin-bottom: 10px;
  color: #646566;
  font-size: 13px;
  text-align: center;
}
</style>
