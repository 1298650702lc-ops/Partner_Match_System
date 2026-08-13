<template>
  <van-loading v-if="loading" class="loading" vertical>正在搜索伙伴</van-loading>

  <van-empty v-else-if="userList.length === 0" description="没有找到符合条件的伙伴" />

  <van-card
    v-for="user in userList"
    :key="user.id"
    :desc="`个人简介：${user.profile || '暂无简介'}`"
    :title="`${user.username || '未设置昵称'} (${user.planetCode || '未设置星球编号'})`"
  >
    <template #thumb>
      <van-image
        :src="user.avatarUrl"
        fit="cover"
        width="100%"
        height="100%"
        @error="useDefaultAvatar(user)"
      />
    </template>
    <template #tags>
      <van-tag
        v-for="tag in user.tags"
        :key="tag"
        plain
        type="primary"
        style="margin-right: 8px; margin-top: 10px"
      >
        {{ tag }}
      </van-tag>
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import myAxios from '../Axios/myAxios'

type SearchUser = {
  id: number
  username?: string
  avatarUrl: string
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
const defaultAvatarUrl = '/default-avatar.svg'
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

const useDefaultAvatar = (user: SearchUser) => {
  if (user.avatarUrl !== defaultAvatarUrl) {
    user.avatarUrl = defaultAvatarUrl
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
      avatarUrl: user.avatarUrl?.trim() || defaultAvatarUrl,
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
