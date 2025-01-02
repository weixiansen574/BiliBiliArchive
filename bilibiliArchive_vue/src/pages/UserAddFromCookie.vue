<template>
    <h3>Cookie</h3>
    <el-input class="cookie-input" v-model="cookieInput" :autosize="{ minRows: 3 }" type="textarea"
        placeholder="请输入Cookie，自动获取用户信息" />
    <el-button type="primary" @click="submit()" :loading="adding">提交</el-button>
    <el-button type="primary" @click="clear()" plain>清空</el-button>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axois from "axios";
import { useUserStore } from '../stores/userStore';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const router = useRouter();
const adding = ref(false);

const cookieInput = ref("");

function submit(){
  adding.value = true;
  let formData = new FormData();
  formData.append("cookie",cookieInput.value);
  axois.post("/api/users",formData)
  .then(resp => {
    adding.value = false;
    let data = resp.data;
    if(data.code == 0){
      let user = data.data;
      console.log(user);
      ElMessage.success(`用户：${user.name} 添加成功`);
      userStore.addUser(user);
      router.push({ name: 'user',params:{uid:user.uid}});
    } else {
      ElMessage.error(data.message)
    }
  })
    //
}

function clear() {
  cookieInput.value = "";
}



</script>

<style scoped>
.cookie-input {
  width: 100%;
  margin-bottom: 15px;
}
</style>