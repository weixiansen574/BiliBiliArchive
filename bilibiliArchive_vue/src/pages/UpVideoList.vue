<template>
  <el-empty description="还没有视频" v-if="videoList.length < 1 && !loading"/>
  <div 
    class="video-grid" 
    v-infinite-scroll="loadMore" 
    :infinite-scroll-disabled="loading" 
    infinite-scroll-distance="1080"
  >
    <VideoCard v-for="video in videoList" :video="video" :key="video.bvid">
      <template #meta1>
        <MetaUploader :video="video"></MetaUploader>
      </template>
      <template #meta2>
        <MetaVideoStat :video="video"></MetaVideoStat>
      </template>
      <template #meta3>
        <MetaUploadDate :timestamp="video.ctime"></MetaUploadDate>
      </template>
    </VideoCard>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

import MetaUploadDate from '../components/MetaUploadDate.vue';
import MetaUploader from '../components/MetaUploader.vue';
import MetaVideoStat from '../components/MetaVideoStat.vue';
import VideoCard from "../components/VideoCard.vue";

const props = defineProps(["uid", "upUid"]);
const upUid = props.upUid;

const videoList = ref([]); // 视频列表
const loading = ref(false); // 加载状态
const hasMore = ref(true); // 是否还有更多数据

const currentPage = ref(1); // 当前页码，初始为1
const pageSize = ref(30); // 每页数据条数

// 获取视频列表
const fetchVideos = async (pn, ps) => {
  try {
    const response = await axios.get(`/api/videos/uploader/${upUid}`, {
      params: {
        pn: pn || currentPage.value,
        ps: ps || pageSize.value
      }
    });
    const { data } = response.data;
    if (data && data.length > 0) {
      videoList.value.push(...data);
      if (data.length < pageSize.value) {
        hasMore.value = false; // 如果返回的数据小于pageSize，说明没有更多数据了
      }
    } else {
      hasMore.value = false; // 没有数据了
    }
  } catch (error) {
    console.error('加载视频失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载更多数据
const loadMore = async () => {
  if (loading.value || !hasMore.value) return; // 防止重复加载
  loading.value = true;
  currentPage.value++;
  await fetchVideos(currentPage.value, pageSize.value);
};

// 初始化加载第一页数据
loading.value = true;
fetchVideos();
</script>
