<template>
  <div class="mdui-container">
    <div class="card header">
      <h2>内容更新任务列表</h2>
    </div>
    <div class="card container">
      <el-timeline v-infinite-scroll="load" :infinite-scroll-disabled="loading" infinite-scroll-distance="1080">
        <el-timeline-item :timestamp="formatTimestamp(plan.timestamp)" placement="top" v-for="(plan, index) in planList"
          :key="plan.id">
          <p>{{ plan.bvid + " · " + plan.title }}</p>
          <p>
            <el-button plain size="small" type="danger" @click="askRemovePlan(index,plan.id)">删除该更新计划</el-button>
            <el-button plain size="small" type="danger" @click="askRemoveVideoAllPlans(plan.bvid)">删除该视频所有计划</el-button>
          </p>
        </el-timeline-item>
      </el-timeline>
      <div v-if="loading" class="mdui-progress">
        <div class="mdui-progress-indeterminate"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElTimeline, ElTimelineItem, ElButton, ElMessage, ElMessageBox } from 'element-plus';
import { formatTimestamp } from "../util"
import axios from 'axios'

const planList = reactive([])
const loading = ref(false);

const pn = ref(1);
const ps = 30;
const noMore = ref(false);

function load() {
  if(noMore.value){
    return;
  }
  loading.value = true;
  axios.get("/api/backup/video-update-plans", { params: { ps, pn: pn.value } })
    .then(({ data }) => {
      loading.value = false;
      if (data.code == 0) {
        if(data.data.length < 1){
          noMore.value = true;
        }
        planList.push(...data.data);
        pn.value++;
      }
    });
}


// 移除该计划
function askRemovePlan(index,id){
  ElMessageBox.confirm("确认删除吗？")
    .then(() => {
      removePlan(index,id);
    });
}

function askRemoveVideoAllPlans(bvid){
  ElMessageBox.confirm("确认删除吗？")
    .then(() => {
      removeVideoAllPlans(bvid);
    });
}

function removePlan(index,id) {
  axios.delete("/api/backup/video-update-plans/id/"+id)
  .then(({data}) => {
    if(data.code == 0){
      planList.splice(index, 1);
      ElMessage.success("删除成功");
    }
  })
}

// 移除该视频所有计划
function removeVideoAllPlans(bvid) {
  axios.delete("/api/backup/video-update-plans/bvid/"+bvid)
    .then(({data}) => {
        if(data.code == 0){
          ElMessage.success("已删除"+data.data+"个更新计划");
          pn.value = 1;
          planList.length = 0;
          load();
        }
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

@media only screen and (max-width: 600px) {
  .el-timeline {
    padding-left: 0px;
  }
}

.mdui-progress {
  max-width: 300px;
  margin-top: 10px;
  margin-inline: auto;
}
</style>