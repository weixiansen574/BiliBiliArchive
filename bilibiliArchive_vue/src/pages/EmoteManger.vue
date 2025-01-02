<template>
  <div class="mdui-container">
    <div class="card header">
      <h2>评论表情管理</h2>
    </div>
    <div class="card container" v-loading="loading">
      <div class="operate">
        <div class="operate-item">
          <el-input v-model="searchQuery" placeholder="输入表情名称进行搜索" />
          <el-button @click="handleSearch" type="primary" style="margin-left: 8px;">搜索</el-button>
        </div>
        <div class="operate-item">
          <el-select v-model="operateUid" placeholder="请选择操作用户">
            <el-option v-for="user in userStore.userList" :key="user.uid" :label="user.name" :value="user.uid" />
          </el-select>
          <el-button type="primary" style="margin-left: 8px;" :disabled="!operateUid" :loading="updating"
            @click="updateEmote">更新</el-button>
        </div>
      </div>
      <div class="emote-container">
        <div class="emote-item mdui-ripple" v-for="emote in filteredEmotes" :key="emote.id" 
        @click="ask('要删除表情吗？','删除后评论列表将不会渲染此表情',
        () => {deleteEmote(emote.id)})">
          <img :src="'/files/emote/' + emote.fileName" :alt="emote.text" class="emote-img" />
          <span class="emote-text">{{ emote.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useUserStore } from "../stores/userStore";
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ask } from '../util';

const userStore = useUserStore();

const loading = ref(true);
const searchQuery = ref('');
const operateUid = ref(null);
const updating = ref(false);

// 用于存储从后端加载的表情列表
const emotes = reactive([]);
const filteredEmotes = ref(emotes);

function load() {
  loading.value = true;
  axios.get("/api/emote")
    .then(({ data }) => {
      if (data.code == 0) {
        emotes.splice(0, emotes.length, ...data.data); // 使用 splice 替换原数组
        filteredEmotes.value = emotes;
      } else {
        ElMessage.error("加载表情失败：" + data.message);
      }
    })
    .finally(() => {
      loading.value = false;
    });
}

// 处理搜索功能
function handleSearch() {
  if (searchQuery.value) {
    filteredEmotes.value = emotes.filter(emote =>
      emote.text.includes(searchQuery.value)
    );
  } else {
    filteredEmotes.value = emotes;
  }
};

// 更新表情
function updateEmote() {
  updating.value = true;
  axios.get("/api/emote/update-list/" + operateUid.value)
    .then(({ data }) => {
      updating.value = false;
      if (data.code == 0) {
        ElMessage.success("更新成功，已更新" + data.data + "个表情");
        load(); // 更新表情列表
      } else {
        ElMessage.error(data.message);
      }
    })
    .catch(() => {
      updating.value = false;
      ElMessage.error("更新失败，请稍后重试");
    });
}

// 删除表情
function deleteEmote(id) {
  axios.delete("/api/emote/" + id)
    .then(({ data }) => {
      if (data.code == 0 && data.data) {
        ElMessage.success("删除成功");
        // 从 emotes 和 filteredEmotes 中移除该表情
        const index = emotes.findIndex(emote => emote.id === id);
        if (index !== -1) {
          emotes.splice(index, 1); // 删除该表情
          filteredEmotes.value = [...emotes]; // 更新 filteredEmotes
        }
      } else {
        ElMessage.error("删除失败：" + data.message);
      }
    })
    .catch(() => {
      ElMessage.error("删除失败，请稍后重试");
    });
}

// 初始化加载数据
load();
</script>


<style scoped>
.card {
  background-color: white;
  border-radius: 3px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin-top: 12px;
  margin-bottom: 12px;
}

.header {
  padding: 20px 10px;
}

h2 {
  margin: 0;
  font-weight: bold;
  color: #32325d;
}

.operate {
  display: flex;
  /* flex-wrap: wrap; */
}

.operate-item {
  display: flex;
  padding-top: 10px;
  padding-inline: 10px;
  width: 100%;
  max-width: 400px;
}

@media only screen and (max-width: 600px) {
  .operate {
    flex-wrap: wrap;
  }

  .operate-item {
    max-width: none;
  }
}

.emote-container {
  padding-top: 10px;
  padding-bottom: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  /* gap: 16px; */
  /* padding: 16px; */
  justify-items: center;
}

.emote-item {
  width: 100%;
  padding-block: 4px;
  text-align: center;
}

.emote-img {
  width: 38px;
  height: 38px;
  object-fit: cover;
  margin-bottom: 4px;
}

.emote-text {
  font-size: 12px;
  color: #555;
  display: block;
}
</style>
