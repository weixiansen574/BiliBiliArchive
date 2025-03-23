<template>
  <div class="mdui-container">
    <div class="container">
      <div class="user-info" v-if="user">
        <img class="avatar" :src="getBCUserAvatarUrl(user.avatarUrl)" alt="User Avatar" />
        <div class="user-details">
          <div class="user-meta">
            <span class="user-name">{{ user.name }}</span>
            <BiliLevel class="user-level" :level="user.pfs.level_info.current_level" />
            <!-- <span class="user-level">LV{{ user.level }}</span> -->
          </div>
          <div class="user-sign">{{ user.pfs.profile.sign }}</div>
          <span v-if="user.pfs.profile.vip.due_date > 0" class="vip-info">大会员开通至 {{ formatTimestampYMD(user.pfs.profile.vip.due_date) }} </span>
          <span v-else class="non-vip">未开通大会员</span>
        </div>
      </div>
    </div>

    <div class="card">
      <RouterTabs :links="links"></RouterTabs>
      <div class="content">
        <RouterView></RouterView>
      </div>
    </div>
    
    <!-- <el-tabs v-model="activeName" @tab-change="change">
      <el-tab-pane label="收藏夹备份" name="favorites"></el-tab-pane>
      <el-tab-pane label="历史记录备份" name="history"></el-tab-pane>
      <el-tab-pane label="UP主备份" name="uploader"></el-tab-pane>
      <el-tab-pane label="配置" name="config">
        <h3>Cookie</h3>
        <el-input class="cookie-input" v-model="cookieInput" :autosize="{ minRows: 3 }" type="textarea"
          placeholder="请输入Cookie" />
        <el-button type="primary" @click="update()" :loading="updating">更新</el-button>
        <el-button type="primary">扫码登录</el-button>
        <el-button type="primary" @click="clear()" plain>清空</el-button>
      </el-tab-pane>
      
    </el-tabs> -->



  </div>
</template>

<script setup>
import { useRoute } from "vue-router";
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { getBCUserAvatarUrl,formatTimestampYMD } from "../util";
import axios from 'axios'
import BiliLevel from "./icons/BiliLevel.vue"
import RouterTabs from "./RouterTabs.vue";
import { useUserStore } from "../stores/userStore"


const links = ref([
  { to: { name: 'favorites' }, name: '收藏夹' },
  { to: { name: 'history' }, name: "历史记录" },
  { to: { name: 'uploader' }, name: "UP主" },
  { to: { name: 'user-config' }, name: "配置" }
])

const route = useRoute();

const uid = route.params.uid;

const cookieInput = ref("");

const user = ref(null);

const userStore = useUserStore();


userStore.getOrAsyncLoad(uid,u => {
  if(!u){
    ElMessage.error("未找到用户："+uid)
  }
  user.value = u;
})

// let userCache = userStore.getByUid(uid);
// if(userCache){
//   user.value = userCache;
// } else {
//   axios.get(`/api/users/${uid}`)
//   .then(response => {
//     user.value = response.data.data;
//     cookieInput.value = user.value.cookie;
//   })
// }





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
        user.value = resp.data;
        pfs.value = user.value.pfs;
        ElMessage.success('cookie更新成功！');
      } else {
        ElMessage.error(resp.message);
      }
      updating.value = false;
    })
}

</script>




<style scoped>
.mdui-container {
  padding: 16px 0px 16px 0px;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 16px;
}

.container {
  background-color: white;
  border-radius: 3px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
}

.icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
}

.contents {
  min-height: 200px;
}

.avatar {
  width: 80px !important;
  /*有概率可能不生效导致被挤扁，所以加个important*/
  height: 80px;
  border-radius: 50%;
  margin-right: 16px;
  border: 2px solid #ddd;
}

.user-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-meta {
  display: flex;
  align-items: center;
}

.user-name {
  font-size: 18px;
  font-weight:600;
  margin-right: 8px;
}

.user-level {
  height: 18px;
  width: 36px;
  color: #555;
  margin-right: 8px;
}


.user-sign {
  margin-top: 6px;
  font-size: 14px;
  color: #888;
}

.card {
  background-color: white;
  border-radius: 3px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
}

.content {
  padding: 10px;
}



.vip-info{
  width: fit-content;
  margin-top: 6px;
  background-color: #FF669A;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;  
  height: 14px;
  line-height: 14px;
}

.non-vip{
  width: fit-content;
  margin-top: 6px;
  background-color: #ABABAB;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>