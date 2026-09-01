<template>
  <van-form @submit="onSubmit">
    <van-field
      v-model="editUser.currentValue"
      :name="editUser.editKey"
      :label="editUser.editName"
      :placeholder="`请输入${editUser.editName}`"
      :type="editUser.editKey === 'email' ? 'email' : 'text'"
      :disabled="loading || submitting"
      :rules="[{ required: true, message: `请输入${editUser.editName}` }]"
    />
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit" :loading="submitting" :disabled="loading">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import myAxios from "../Axios/myAxios";
import { useUserStore } from "../stores/userStore";

type EditableField = 'username' | 'userAccount' | 'avatarUrl' | 'gender' | 'phone' | 'email';

const editableFields: Record<EditableField, string> = {
  username: '昵称',
  userAccount: '账号',
  avatarUrl: '头像',
  gender: '性别',
  phone: '电话',
  email: '邮箱',
};

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const queryEditKey = Array.isArray(route.query.editKey) ? route.query.editKey[0] : route.query.editKey;
const editKey = queryEditKey && queryEditKey in editableFields
  ? queryEditKey as EditableField
  : null;
const queryValue = Array.isArray(route.query.value) ? route.query.value[0] : route.query.value;
const queryEditName = Array.isArray(route.query.editName) ? route.query.editName[0] : route.query.editName;

const editUser = ref({
  id: 0,
  editKey: editKey ?? '',
  currentValue: queryValue ?? '',
  editName: editKey ? editableFields[editKey] : (queryEditName ?? ''),
});
const loading = ref(true);
const submitting = ref(false);

const loadCurrentUser = async () => {
  if (!editKey) {
    showFailToast('编辑字段无效');
    await router.replace('/user');
    return;
  }

  try {
    const currentUser = await userStore.fetchCurrentUser();
    if (!currentUser?.id) {
      showFailToast('请先登录');
      await router.replace('/user');
      return;
    }

    editUser.value.id = currentUser.id;
    // 以服务端数据为准，避免页面携带的旧 query 值覆盖最新内容。
    const currentValue = currentUser[editKey];
    editUser.value.currentValue = currentValue == null ? '' : String(currentValue);
  } catch (error) {
    console.error('获取当前用户失败', error);
    showFailToast('获取用户信息失败');
  } finally {
    loading.value = false;
  }
};

const onSubmit = async () => {
  if (!editKey || !editUser.value.id || !editUser.value.currentValue.trim() || submitting.value) {
    showFailToast('请输入有效内容');
    return;
  }

  submitting.value = true;
  try {
    const response = await myAxios.post('/user/update', {
      id: editUser.value.id,
      [editKey]: editUser.value.currentValue.trim(),
    });
    const body = response.data;
    if (body.code === 0 && body.data > 0) {
      await userStore.fetchCurrentUser(true);
      showSuccessToast('更新成功');
      await router.back();
    } else {
      showFailToast(body.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败', error);
    showFailToast('更新失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(loadCurrentUser);
</script>
