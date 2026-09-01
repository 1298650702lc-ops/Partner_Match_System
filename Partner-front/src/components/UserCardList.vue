<template>
  <van-loading v-if="loading" class="loading" vertical>正在加载伙伴</van-loading>

  <template v-else>
    <van-card
      v-for="user in userList"
      :key="user.id"
      :desc="`个人简介：${user.profile || '暂无简介'}`"
      :title="`${user.username || '未设置昵称'} (${user.planetCode || '未设置星球编号'})`"
    >
      <template #thumb>
        <van-image
          :src="getAvatarUrl(user)"
          fit="cover"
          width="100%"
          height="100%"
          @error="markAvatarAsFailed(user.id)"
        />
      </template>
      <template #tags>
        <van-tag
          v-for="tag in user.tags || []"
          :key="`${user.id}-${tag}`"
          plain
          type="primary"
          style="margin-right: 8px; margin-top: 10px"
        >
          {{ tag }}
        </van-tag>
        <span v-if="!user.tags?.length" class="no-tags">暂无标签</span>
      </template>
      <template #footer>
        <van-button size="mini" @click="$emit('contact', user)">联系我</van-button>
      </template>
    </van-card>

    <van-empty v-if="userList.length === 0" :description="emptyDescription" />
  </template>
</template>

<script setup lang="ts">
import { ref } from 'vue'

export type UserCardListItem = {
  id: number
  username?: string | null
  avatarUrl?: string | null
  planetCode?: string | null
  profile?: string | null
  tags?: string[] | null
}

withDefaults(defineProps<{
  userList: UserCardListItem[]
  loading?: boolean
  emptyDescription?: string
}>(), {
  loading: false,
  emptyDescription: '暂无伙伴数据',
})

defineEmits<{
  contact: [user: UserCardListItem]
}>()

const defaultAvatarUrl = '/default-avatar.svg'
const failedAvatarIds = ref(new Set<number>())

const cleanAvatarUrl = (avatarUrl?: string | null) => {
  const value = avatarUrl?.trim().replace(/^['"]|['"]$/g, '')
  return value || defaultAvatarUrl
}

const getAvatarUrl = (user: UserCardListItem) => {
  return failedAvatarIds.value.has(user.id) ? defaultAvatarUrl : cleanAvatarUrl(user.avatarUrl)
}

const markAvatarAsFailed = (userId: number) => {
  failedAvatarIds.value.add(userId)
}
</script>

<style scoped>
.loading {
  padding-top: 48px;
}

.no-tags {
  color: #969799;
  font-size: 12px;
  line-height: 24px;
}
</style>
