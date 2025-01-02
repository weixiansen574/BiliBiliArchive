<template>
  <h3>Cookie</h3>
  <el-input class="cookie-input" v-model="cookieInput" :autosize="{ minRows: 3 }" type="textarea"
    placeholder="请输入Cookie" />
  <el-button type="primary" @click="update()" :loading="updating">更新</el-button>
  <el-button type="primary">扫码登录</el-button>
  <el-button type="primary" @click="clear()" plain>清空输入</el-button>
  <el-button type="danger" @click="promptDeleteUser()" plain>删除用户</el-button>

</template>

<script setup>
import { useRoute, useRouter } from 'vue-router';
import { ref } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '../stores/userStore'
import { closeDrawer } from '../util';

const cookieInput = ref("");
const route = useRoute();
const router = useRouter();
const uid = route.params.uid;
const user = ref(null)
const updating = ref(false);

// axios.get(`/api/users/${uid}`)
//   .then(response => {
//     user.value = response.data.data;
//     cookieInput.value = user.value.cookie;
//   })


const userStore = useUserStore();

userStore.getOrAsyncLoad(uid, u => {
  user.value = u;
  cookieInput.value = user.value.cookie;
})

function clear() {
  cookieInput.value = "";
}

function update() {
  if (updating == true) {
    return;
  }
  updating.value = true;
  var formData = new FormData();
  formData.append("cookie", cookieInput.value);
  axios.put(`/api/users/${uid}`, formData)
    .then(response => {
      var resp = response.data;
      if (resp.code == 0) {
        Object.assign(user.value, resp.data)
        ElMessage.success('cookie更新成功！');
      } else {
        ElMessage.error(resp.message);
      }
      updating.value = false;
    })
}

function promptDeleteUser(){
  closeDrawer();
  ElMessageBox.confirm("删除用户需要先删除其所有关联的备份项（如收藏夹、历史记录、UP主等）。请确认相关备份已被删除，确定继续吗？",
  "警告",
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    .then(() => {
      axios.delete("/api/users/"+uid)
        .then(({data}) => {
          if(data.code == 0){
            userStore.removeUser(uid);
            router.push({name:"home"})
            ElMessage.success("删除成功！");
          } else {
            ElMessage.error(data.message);
          }
        })
    })
}
</script>

<style scoped>
.cookie-input {
  width: 100%;
  margin-bottom: 15px;
}
</style>