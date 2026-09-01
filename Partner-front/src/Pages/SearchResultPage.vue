<template>
  <UserCardList
    :user-list="userList"
    :loading="loading"
    empty-description="没有找到符合条件的伙伴"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
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

const route = useRoute()
const loading = ref(false)
const userList = ref<SearchUser[]>([])
const selectedTags = (Array.isArray(route.query.tags) ? route.query.tags : [route.query.tags])
  .filter((tag): tag is string => typeof tag === 'string' && tag.trim().length > 0)

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

onMounted(async () => {
  if (selectedTags.length === 0) {
    showToast('请选择至少一个标签')
    return
  }

  loading.value = true
  try {
    const response = await myAxios.get<{ data: ApiUser[] }>('/user/search/tags', {
      params: { tagNameList: selectedTags },
      paramsSerializer: { indexes: null },
    })
    userList.value = (response.data.data ?? []).map((user) => ({
      ...user,
      avatarUrl: user.avatarUrl?.trim() || undefined,
      tags: parseTags(user.tags),
    }))
  } catch (error) {
    console.error('/user/search/tags error', error)
    showToast('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading {
  padding-top: 48px;
}
</style>
