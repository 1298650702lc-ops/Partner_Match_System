<template>
  <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>
  <template v-else-if="user">
    <van-cell title="昵称" is-link :value="user.username" @click="toEdit('username','昵称',user.username)" />
    <van-cell title="账号" is-link :value="user.userAccount" @click="toEdit('userAccount', '账号', user.userAccount)" />
    <van-cell title="头像" is-link @click="toEdit('avatarUrl', '头像', user.avatarUrl || '')">
      <img style="height: 48px" :src="user.avatarUrl || defaultAvatarUrl" alt="用户头像"/>
    </van-cell>
    <van-cell title="性别" is-link :value="formatGender(user.gender)" @click="toEdit('gender', '性别', String(user.gender ?? ''))" />
    <van-cell title="电话" is-link :value="user.phone || '未设置'" @click="toEdit('phone', '电话', user.phone || '')" />
    <van-cell title="邮箱" is-link :value="user.email || '未设置'" @click="toEdit('email', '邮箱', user.email || '')" />
    <van-cell title="星球编号" :value="user.planetCode || '未设置'" />
    <van-cell title="注册时间" :value="formatCreateTime(user.createTime)" />
    <van-cell title="我的队伍" is-link icon="friends-o" @click="router.push('/user/team')" />
    <div class="logout-wrapper">
      <van-button
          round
          block
          type="danger"
          :loading="logoutLoading"
          loading-text="退出中..."
          @click="onLogout"
      >
        退出登录
      </van-button>
    </div>
  </template>
  <van-empty v-else description="暂无用户信息" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showConfirmDialog, showSuccessToast, showToast } from 'vant';
import type { CurrentUser } from "../models/user";
import { useUserStore } from "../stores/userStore";

const router = useRouter();
const userStore = useUserStore();
const user = userStore.user;
const loading = userStore.loading;
const logoutLoading = ref(false);
const defaultAvatarUrl = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';

const formatGender = (gender: number | string | null | undefined) => {
  if (gender === 1 || gender === '1') return '男';
  if (gender === 0 || gender === '0') return '女';
  return '未设置';
};

const formatCreateTime = (createTime: CurrentUser['createTime']) => {
  if (!createTime) return '未设置';
  const date = createTime instanceof Date ? createTime : new Date(createTime);
  return Number.isNaN(date.getTime()) ? '未设置' : date.toLocaleString('zh-CN');
};

onMounted(async () => {
  const currentUser = await userStore.fetchCurrentUser();
  if (!currentUser) {
    await router.replace('/user/login');
  }
});
//跳转到编辑页，并带上要编辑的字段和当前值
const toEdit = (editKey: string, editName: string, value: string) => {
  router.push({ path: '/user/edit', query: { editKey, editName,value } });
};

// 退出登录：二次确认 -> 调用后端注销 -> 清理本地状态 -> 回到登录页
const onLogout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出当前账号吗？',
    });
  } catch {
    // 用户点击了取消
    return;
  }

  logoutLoading.value = true;
  try {
    const success = await userStore.logout();
    if (success) {
      showSuccessToast('已退出登录');
    } else {
      showToast('已清除本地登录状态');
    }
    await router.replace('/user/login');
  } finally {
    logoutLoading.value = false;
  }
};
</script>

<style scoped>
.logout-wrapper {
  margin: 24px 16px 16px;
}
</style>
