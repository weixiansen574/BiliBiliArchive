<template>
  <el-timeline v-infinite-scroll="load" :infinite-scroll-disabled="loading" infinite-scroll-distance="1080">
    <el-timeline-item :timestamp="formatTimestamp(plan.timestamp)" placement="top" v-for="(plan, index) in planList"
      :key="plan.id">
      <p>{{ plan.bvid + " · " + plan.title }}</p>
      <p>
        <el-button plain size="small" type="danger" @click="askRemovePlan(index, plan.id)">删除该更新计划</el-button>
        <el-button plain size="small" type="danger" @click="askRemoveVideoAllPlans(plan.bvid)">删除该视频所有计划</el-button>
      </p>
    </el-timeline-item>
  </el-timeline>
  <el-empty v-if="!loading && planList.length == 0" :image-size="200" description="没有更新计划"/>
  <div v-if="loading" class="mdui-progress">
    <div class="mdui-progress-indeterminate"></div>
  </div>
</template>

<script setup>
import { ElTimeline, ElTimelineItem, ElButton, ElMessage, ElMessageBox } from 'element-plus';
import { formatTimestamp } from "../util";
import { ref, reactive, defineProps } from 'vue';
import axios from 'axios'

const props = defineProps(["bvid"]);

const bvid = props.bvid;

const planList = reactive([])
const loading = ref(false);

const pn = ref(1);
const ps = 30;
const noMore = ref(false);

function load() {
  if (noMore.value) {
    return;
  }
  loading.value = true;
  let path = (bvid != null ? ("/api/backup/video-update-plans/bvid/" + bvid) : "/api/backup/video-update-plans");
  axios.get(path, { params: { ps, pn: pn.value } })
    .then(({ data }) => {
      loading.value = false;
      if (data.code == 0) {
        if (data.data.length < 1) {
          noMore.value = true;
        }
        planList.push(...data.data);
        pn.value++;
      }
    });
}


// 移除该计划
function askRemovePlan(index, id) {
  ElMessageBox.confirm("确认删除吗？")
    .then(() => {
      removePlan(index, id);
    });
}

function askRemoveVideoAllPlans(bvid) {
  ElMessageBox.confirm("确认删除吗？")
    .then(() => {
      removeVideoAllPlans(bvid);
    });
}

function removePlan(index, id) {
  axios.delete("/api/backup/video-update-plans/id/" + id)
    .then(({ data }) => {
      if (data.code == 0) {
        planList.splice(index, 1);
        ElMessage.success("删除成功");
      }
    })
}

// 移除该视频所有计划
function removeVideoAllPlans(bvid) {
  axios.delete("/api/backup/video-update-plans/bvid/" + bvid)
    .then(({ data }) => {
      if (data.code == 0) {
        ElMessage.success("已删除" + data.data + "个更新计划");
        pn.value = 1;
        planList.length = 0;
        load();
      }
    })
}
</script>

<style>
.el-timeline {
  padding-left: 3px;
}
</style>