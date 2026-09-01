<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<script setup>

import myAxios from "../Axios/myAxios";
import { showFailToast, showSuccessToast } from "vant";
import {ref} from "vue";
import {useRouter} from "vue-router";
import { useUserStore } from "../stores/userStore";

const router = useRouter();
const userStore = useUserStore();

const userAccount = ref('');
const userPassword = ref('');

const onSubmit = async () => {
  try {
    const response = await myAxios.post("/user/login", {
      userAccount: userAccount.value,
      userPassword: userPassword.value
    });
    const body = response.data;
    if (body.code === 0 && body.data != null) {
      userStore.setCurrentUser(body.data);
      showSuccessToast("登录成功");
      await router.replace("/");
    } else {
      showFailToast(body.message || "登录失败");
    }
  } catch (error) {
    console.error("登录请求失败", error);
    showFailToast("登录失败，请稍后重试");
  }
};

</script>

<style scoped>

</style>
