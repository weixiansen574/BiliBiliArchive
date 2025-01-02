<template>
  <el-empty description="还没有视频" v-if="videoList.length < 1 && !loading"/>
  <div 
    class="video-grid"
    v-infinite-scroll="loadMore"
    :infinite-scroll-disabled="loading"
    infinite-scroll-distance="1080"
  >
    <VideoCard v-for="video in videoList" :video="video" :key="video.bvid" :class="{'fav-ban':video.favBan}" :actions="actions">
      <template #meta1>
        <MetaUploader :video="video"></MetaUploader>
      </template>
      <template #meta2>
        <MetaVideoStat :video="video"></MetaVideoStat>
      </template>
      <template #meta3>
        <MetaFavorite :time="video.favTime"></MetaFavorite>
      </template>
    </VideoCard>
  </div>
  <div v-if="loading" class="mdui-progress">
    <div class="mdui-progress-indeterminate"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage,ElMessageBox,ElNotification } from 'element-plus';
import { closeDrawer } from '../util';

import MetaFavorite from './MetaFavorite.vue';
import MetaUploader from './MetaUploader.vue';
import MetaVideoStat from './MetaVideoStat.vue';
import VideoCard from './VideoCard.vue';

const route = useRoute();
const favId = route.params.favId;
const videoList = ref([]);
const loading = ref(false);

const currentPage = ref(1); // 当前页码
const pageSize = ref(30); // 页大小
const hasMore = ref(true); // 是否还有更多数据

// 获取视频列表
const fetchVideos = async (pn, ps) => {
  try {
    const response = await axios.get(`/api/videos/favorite/${favId}`, {
      params: {
        pn: pn || currentPage.value,
        ps: ps || pageSize.value
      }
    });
    const { data } = response.data;
    if (data && data.length > 0) {
      videoList.value.push(...data);
      if (data.length < pageSize.value) {
        hasMore.value = false; // 数据不足一页，说明没有更多了
      }
    } else {
      hasMore.value = false; // 没有数据
    }
  } catch (error) {
    ElMessage.error('加载视频列表失败');
  } finally {
    loading.value = false;
  }
};

// 加载更多
const loadMore = async () => {
  if (loading.value || !hasMore.value) return;
  loading.value = true;
  currentPage.value++;
  await fetchVideos(currentPage.value, pageSize.value);
};

// 初始化加载第一页数据
loading.value = true;
fetchVideos();

const actions = [{
  title: "删除收藏视频", onClick: video => {
    closeDrawer();
    ElMessageBox.confirm(
      `确认删除此收藏视频『${video.title}』吗？需要你先在B站的收藏夹进行删除，否则不可删除`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        let notification = ElNotification({
          title: '正在删除收藏视频……',
          message: video.title,
          duration: 0,
        });
        axios.delete(`/api/backup/fav/${favId}/${video.bvid}`)
          .then(({ data }) => {
            notification.close();
            if (data.code == 0) {
              videoList.value.splice(videoList.value.indexOf(video), 1);
              if (data.data) {
                ElMessage.success("收藏及视频存档成功");
              } else {
                ElMessage.success("收藏删除成功，但视频存档有其他备份项在引用，已保留");
              }

            } else {
              ElMessage.error(data.message);
            }
          })

      })
  }
}]
</script>

<style scoped>
.mdui-progress {
  max-width: 300px;
  margin-top: 10px;
  margin-inline: auto;
}

.fav-ban{
  background: repeating-linear-gradient(
    -45deg,
    #f0f0f0, /* 浅灰色 */
    #f0f0f0 10px,
    #d0d0d090 10px,
    #d0d0d090 20px
  );
  border-radius: 4px;
}


</style>