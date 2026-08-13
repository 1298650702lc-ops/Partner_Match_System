<template>
  <div class="search-page">
    <van-search
      v-model="searchText"
      show-action
      clearable
      placeholder="搜索标签，例如：Java、研一"
      @search="filterTags"
      @clear="resetTags"
      @cancel="resetTags"
    >
      <template #action>
        <span class="search-action" @click="filterTags">筛选</span>
      </template>
    </van-search>

    <section class="selected-section">
      <div class="section-title">
        <span>已选标签</span>
        <span class="selected-count">{{ activeIds.length }} 个</span>
      </div>
      <div v-if="activeIds.length === 0" class="empty-tip">选择标签，寻找志同道合的伙伴</div>
      <div v-else class="selected-tags">
        <van-tag
          v-for="tag in activeIds"
          :key="tag"
          closeable
          type="primary"
          size="medium"
          @close="removeTag(tag)"
        >
          {{ tag }}
        </van-tag>
      </div>
    </section>

    <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
      height="calc(100vh - 264px)"
    />

    <div class="search-footer">
      <van-button block round type="primary" :disabled="activeIds.length === 0" @click="searchUsers">
        搜索伙伴（{{ activeIds.length }}）
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

type TagItem = {
  text: string
  id: string
}

type TagCategory = {
  text: string
  children: TagItem[]
}

const router = useRouter()
const searchText = ref('')
const activeIds = ref<string[]>([])
const activeIndex = ref(0)

const allTagList: TagCategory[] = [
  {
    text: '性别',
    children: [
      { text: '男', id: '男' },
      { text: '女', id: '女' },
    ],
  },
  {
    text: '年级',
    children: ['大一', '大二', '大三', '大四', '研一', '研二', '研三'].map((grade) => ({ text: grade, id: grade })),
  },
  {
    text: '方向',
    children: ['C', 'C++', '人工智能', 'Java', 'Python', '前端', '后端', '全栈'].map((direction) => ({
      text: direction,
      id: direction,
    })),
  },
]

const tagList = ref<TagCategory[]>(allTagList)

const resetTags = () => {
  searchText.value = ''
  tagList.value = allTagList
}

const filterTags = () => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) {
    tagList.value = allTagList
    return
  }

  tagList.value = allTagList
    .map((category) => ({
      ...category,
      children: category.children.filter((tag) => tag.text.toLowerCase().includes(keyword)),
    }))
    .filter((category) => category.children.length > 0)

  activeIndex.value = 0
}

const removeTag = (tag: string) => {
  activeIds.value = activeIds.value.filter((item) => item !== tag)
}

const searchUsers = () => {
  if (activeIds.value.length === 0) {
    showToast('请至少选择一个标签')
    return
  }

  router.push({
    path: '/user/list',
    query: { tags: activeIds.value },
  })
}
</script>

<style scoped>
.search-page {
  min-height: 100%;
  background: #f7f8fa;
}

.search-action {
  color: var(--van-primary-color);
}

.selected-section {
  margin: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #323233;
  font-size: 15px;
  font-weight: 600;
}

.selected-count {
  color: #969799;
  font-size: 13px;
  font-weight: 400;
}

.empty-tip {
  margin-top: 10px;
  color: #969799;
  font-size: 13px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.search-footer {
  position: fixed;
  right: 0;
  bottom: var(--van-tabbar-height);
  left: 0;
  z-index: 1;
  padding: 12px 16px;
  box-sizing: border-box;
  background: #fff;
  box-shadow: 0 -2px 10px rgb(0 0 0 / 5%);
}
</style>
