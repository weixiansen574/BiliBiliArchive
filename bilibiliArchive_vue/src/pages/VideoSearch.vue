<template>
    <div class="mdui-container">
        <div class="card header">
            <h2>视频搜索</h2>
        </div>
        <div class="card container">
            <div class="search">
                <el-input v-model="searchQuery" @keyup.enter="handleSearch" placeholder="搜索视频标题" />
                <el-button @click="handleSearch" type="primary" style="margin-left: 8px;" :loading="loading">搜索</el-button>
            </div>
            <div v-if="videoList && videoList.length > 0" class="video-grid" >
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
            <el-empty v-else :description="videoList ? '啥也没找到' : '请输入关键词搜索视频'" />
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import VideoCard from '../components/VideoCard.vue';
import { ElMessage } from 'element-plus';
import MetaUploadDate from '../components/MetaUploadDate.vue';
import MetaUploader from '../components/MetaUploader.vue';
import MetaVideoStat from '../components/MetaVideoStat.vue';

const loading = ref(false);
const searchQuery = ref("");
const videoList = ref(null);


function handleSearch() {
    loading.value = true;
    axios.get('/api/videos/search', {
        params: { text: searchQuery.value.trim() }
    }).then(({ data }) => {
        loading.value = false;
        if (data.code == 0) {
            videoList.value = data.data;
        } else {
            ElMessage.error(data.message);
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

.container {
    padding: 10px;
}

.search {
    display: flex;
}

.el-input {
    max-width: 400px;
}

.video-grid{
    margin-top: 10px;
}
</style>