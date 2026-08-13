<template>
  <van-cell title="昵称" is-link :value="user.username" @click="toEdit('username','昵称',user.username)" />
  <van-cell title="账号" is-link :value="user.userAccount" @click="toEdit('userAccount', '账号', user.userAccount)" />
  <van-cell title="头像" is-link @click="toEdit('avatarUrl', '头像', user.avatarUrl)">
    <img style="height: 48px" :src="user.avatarUrl"/>
  </van-cell>
  <van-cell title="性别" is-link :value="user.gender" @click="toEdit('gender', '性别', user.gender)" />
  <van-cell title="电话" is-link :value="user.phone" @click="toEdit('phone', '电话', user.phone)" />
  <van-cell title="邮箱" is-link :value="user.email" @click="toEdit('email', '邮箱', user.email)" />
  <van-cell title="星球编号" :value="user.planetCode" />
  <van-cell title="注册时间" :value="user.createTime.toISOString()" />
</template>

<script setup lang="ts">
const router = useRouter();

const user = {
  id: 1,
  username: '鱼皮',
  userAccount: 'dogYupi',
  avatarUrl: 'https://img1.baidu.com/it/u=1645832847,2375824523&fm=253&fmt=auto&app=138&f=JPEG?w=480&h=480',
  gender: '男',
  phone: '12131133313',
  email: '23432444@qq.com',
  planetCode: '2220',
  createTime: new Date(),
};

//跳转到编辑页，并带上要编辑的字段和当前值
const toEdit = (editKey: string, editName: string, value: string) => {
  router.push({ path: '/user/edit', query: { editKey, editName,value } });
};
</script>

<style scoped>

</style>
