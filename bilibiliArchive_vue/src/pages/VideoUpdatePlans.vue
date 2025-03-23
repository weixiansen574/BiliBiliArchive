<template>
  <div class="mdui-container">
    <div class="card header">
      <h2>内容更新任务列表</h2>
    </div>
    <div class="card container">
      <el-button type="danger" @click="removeAllPlans">删除所有更新计划</el-button>
      <UpdatePlans></UpdatePlans>
    </div>
  </div>
</template>

<script setup>
import UpdatePlans from '../components/UpdatePlans.vue';
import { ref, reactive } from 'vue';
import { ElTimeline, ElTimelineItem, ElButton, ElMessage, ElMessageBox } from 'element-plus';
import { formatTimestamp, confirmWithInput } from "../util"
import axios from 'axios'


function removeAllPlans() {
  confirmWithInput("确定吗？", "这将清除所有更新计划", "确认删除", () => {
      axios.delete("/api/backup/video-update-plans")
      .then(({data}) => {
        ElMessage.success("已删除"+data.data+"条更新计划");
      })
  })
}
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

h3 {
  margin-top: 5px;
  margin-bottom: 10px;
}

.container {
  padding: 10px;
}

.el-button{
  margin-bottom: 10px;
}

</style>