<template>
  <div class="video-page mdui-container">
    <!-- 视频播放器 -->
    <div class="video-play-card card">
      <div ref="videoPlayer" class="video-player"></div>
    </div>

    <div class="video-extensions-card card">
      <template v-if="!videoInfo">
        <!-- 骨架屏 -->
        <el-skeleton animated>
          <el-skeleton-item variant="avatar" style="width: 35px; height: 35px" />
          <el-skeleton-item variant="text" style="width: 150px; margin-top: 10px;" />
          <el-skeleton-item variant="text" style="width: 80%; margin-top: 10px;" />
        </el-skeleton>
      </template>
      <el-tabs v-model="activeName" v-else>
        <el-tab-pane label="详情" name="details" class="tab1">
          <div class="uploader">
            <a :href="'//space.bilibili.com/' + videoInfo.ownerMid" target="_blank">
              <img :src="getUpAvatarUrl(videoInfo.ownerAvatarUrl)" alt="头像">
            </a>
            {{ videoInfo.ownerName }}
          </div>

          <h3 class="video-title">{{ videoInfo.title }}</h3>

          <div class="video-info-1">
            <ViewsIcon class="instruct-icon" /><span>{{ videoInfo.view }}</span>
            <DanmakusIcon class="instruct-icon" /><span>{{ videoInfo.danmaku }}</span>
            <span>{{ formatTimestamp(videoInfo.ctime * 1000) }}</span>
          </div>
          <div class="video-id">
            <div>{{ bvid }}</div>
            <div>AV{{ videoInfo.avid }}</div>
          </div>
          <div class="tags" v-if="videoInfo.tags">
            <span class="tag" v-for="(tag, index) in videoInfo.tags" :key="tag.tag_id">
              {{ tag.tag_name }}
            </span>
          </div>
          <div class="san-lian">
            <div>
              <BigLike class="san-lian-icon" />
              <span>{{ videoInfo.like }}</span>
            </div>
            <div>
              <BigCoin class="san-lian-icon" />
              <span>{{ videoInfo.coin }}</span>
            </div>
            <div>
              <BigStar class="san-lian-icon" />
              <span>{{ videoInfo.favorite }}</span>
            </div>
            <div>
              <BigShare class="san-lian-icon" />
              <span>{{ videoInfo.share }}</span>
            </div>
          </div>
          <div class="description">{{ videoInfo.desc }}</div>
        </el-tab-pane>
        <el-tab-pane :label="videoInfo ? '评论 ' + commentCount + ' ' + allCommentCount : '评论'" name="comments">
          <Comments v-if="videoInfo" :oid="videoInfo.avid" type="1" :up-uid="videoInfo.ownerMid" />
        </el-tab-pane>
        <el-tab-pane label="存档信息">
          <div v-if="videoInfo">
            <h3>剧集版本</h3>
            <div class="page-versions">
              <div v-for="(pageVersion, index) in pageVersions" :key="index" class="page-version">
                <h4>存档于：{{ formatTimestamp(pageVersion.versionTime) }}</h4>
                <div class="pages">
                  <div v-for="page in pageVersion.pages" :key="page.page" class="page mdui-ripple"
                    :class="{ 'current-page': currentPage === page }" @click="switchPage(page)">
                    <div class="quality-info">{{ getCodecName(page.codecId) }} {{
                      getQualityName(page.qn) }}
                    </div>
                    <div class="page-title">{{ page.part }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="custom-tag-container">
              <div class="custom-tag">
                Part: {{ currentPage.part }}
              </div>
              <div class="custom-tag">
                CID: {{ currentPage.cid }}
              </div>
            </div>
            <h3>下载</h3>
            <p>
              <el-button :icon="VideoCamera" type="primary" @click="downloadVideo">视频文件</el-button>
              <el-button :icon="Postcard" type="primary" @click="downloadDanmuku">弹幕文件</el-button>
            </p>

            <h3>更多信息</h3>
            <div class="info-cards">
              <div>
                <div>视频状态</div>
                <div>{{ getVideoStateDesc(videoInfo.state) }}</div>
              </div>
              <div>
                <div>存档保存日期</div>
                <div>{{ formatTimestamp(videoInfo.saveTime) }}</div>
              </div>
              <div>
                <div>存档更新日期</div>
                <div>{{ formatTimestamp(videoInfo.communityUpdateTime) }}</div>
              </div>
              <div>
                <div>评论区根评论总数</div>
                <div>{{ rootCommentCount }}</div>
              </div>
              <div>
                <div>视频总硬盘占用</div>
                <div>{{ formatDiskUsage(diskUsage) }}</div>
              </div>
            </div>

            <h3 v-if="showFloorInfo">评论区楼层</h3>
            <div class="info-cards" v-if="showFloorInfo">
              <div>
                <div>楼层总数</div>
                <div>{{ videoInfo.totalCommentFloor }}</div>
              </div>
              <el-tooltip class="box-item" effect="dark" content="需评论全量下载才有意义" placement="top-start">
                <div>
                  <div>已显示比例(仅供参考)</div>
                  <div :style="{ color: percentageColor }">{{ percentage }}%</div>
                </div>
              </el-tooltip>
            </div>
            <h3>更新存档</h3>
            <el-dialog title="更新视频" v-model="updateVideoDialogVisible"
              style="min-width: 350px;width: 90%; max-width: 600px;" align-center>
              <el-form :model="updateVideoForm" label-width="auto">
                <el-card header="基础选项">
                  <el-form-item label="BV号">
                    <el-input v-model="updateVideoForm.bvid" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="更新执行用户">
                    <el-select v-model="updateVideoForm.uid" placeholder="请选择用户">
                      <el-option v-for="user in userStore.userList" :key="user.uid" :label="user.name"
                        :value="user.uid" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="更新视频和弹幕">
                    <el-switch v-model="updateVideoForm.updateVideoAndDanmaku" />
                  </el-form-item>
                </el-card>
                <VideoConfig v-model="updateVideoForm.video" v-if="updateVideoForm.updateVideoAndDanmaku" title="视频更新" />
                <CommentConfig v-model="updateVideoForm.commentConfig" title="评论更新"></CommentConfig>
              </el-form>

              <template #footer>
                <el-button @click="updateVideoDialogVisible = false">取消</el-button>
                <el-button @click="updateVideo()" type="primary" :disabled="!updateVideoForm.uid"
                  :loading="updateSubmitting">确定</el-button>
              </template>
            </el-dialog>
            <el-dialog v-model="addUpdatePlanDialogVisible" title="编辑更新计划"
              style="min-width: 350px;width: 90%; max-width: 800px;" align-center>
              <!-- <el-dialog v-model="selectVideoConfigVisible" title="选择视频备份配置加载"
               style="min-width: 350px;width: 90%; max-width: 600px;" align-center>
                <el-form-item label="选择配置">
                  <el-select  placeholder="请选择配置">

                  </el-select>
                </el-form-item>
              </el-dialog> -->
              <el-form v-model="addUpdateForm" label-width="auto">
                <el-card header="基础选项">
                  <el-form-item label="BV号">
                    <el-input v-model="addUpdateForm.bvid" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="更新执行用户">
                    <el-select v-model="addUpdateForm.uid" placeholder="请选择用户">
                      <el-option v-for="user in userStore.userList" :key="user.uid" :label="user.name"
                        :value="user.uid" />
                    </el-select>
                  </el-form-item>
                </el-card>
                <VideoConfig v-model="addUpdateForm.video" title="视频更新" />
                <UpdateConfigs v-model="addUpdateForm.update" />
              </el-form>
              <template #footer>
                <div class="dialog-footer" style="display: flex; justify-content: space-between; align-items: center;">
                  <el-dropdown trigger="click" :disabled="!videoBackupConfigs">
                    <el-button type="primary" style="margin-right: auto;" plain
                      :loading="!videoBackupConfigs">载入视频备份配置</el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item v-for="cfg in videoBackupConfigs" :key="cfg.id"
                          @click="useVideoBackupConfig(cfg)">
                          {{ cfg.name }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>

                  <div style="flex-grow: 1; text-align: right;">
                    <el-button @click="addUpdatePlanDialogVisible = false">取消</el-button>
                    <el-button type="primary" :disabled="!addUpdateForm.uid || addUpdateForm.update.length < 1"
                      @click="addVideoUpdatePlans" :loading="addUpdatePlanSubmitting">确定</el-button>
                  </div>
                </div>
              </template>
            </el-dialog>
            <el-button type="primary" @click="ask('确认更新吗？',
              '更新视频，需要更新计划列表里不存在此视频的计划，否则因冲突无法更新。执行更新，视频备份配置优先级会调整到FINAL，将来不会被其他备份配置覆盖！',
              () => updateVideoDialogVisible = true)">立即更新</el-button>
            <el-button type="primary" @click="ask('说明',
              '向视频更新计划列表插入视频备份配置，起始时间为当前时间。此操作将移除已存在的视频更新计划。接着会将备份配置优先级将调到FINAL，将来不会被其他备份配置覆盖',
              () => { addUpdatePlanDialogVisible = true; loadVideoBackupConfigs() })">添加更新计划</el-button>
          </div>

        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
  
<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRoute } from 'vue-router';
import { formatTimestamp } from '../util';
import axios from 'axios';
import Artplayer from 'artplayer';
import artplayerPluginDanmuku from 'artplayer-plugin-danmuku';
import DanmakusIcon from './icons/DanmakusIcon.vue';
import ViewsIcon from './icons/ViewsIcon.vue';
import BigLike from './icons/BigLike.vue';
import BigCoin from './icons/BigCoin.vue';
import BigStar from './icons/BigStar.vue';
import BigShare from "./icons/BigShare.vue";
import Comments from './Comments.vue';
import subtitleIcon from '../assets/subtitle.svg';
import { VideoCamera, Postcard } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import CommentConfig from "./CommentConfig.vue"
import VideoConfig from './VideoConfig.vue';
import UpdateConfigs from './UpdateConfigs.vue';
import { ask,formatDiskUsage,getVideoStateDesc } from "../util"
import { useUserStore } from "../stores/userStore";

const userStore = useUserStore();

const route = useRoute();
const bvid = route.params.bvid;
const activeName = ref("details");

const videoInfo = ref(null);

const allCommentCount = ref(0);
const rootCommentCount = ref(0);
const commentCount = ref(0);
const diskUsage = ref(0);

const pageVersions = ref([]);

const currentPage = ref(null);
const videoPlayer = ref(null);

//⚠️使用守则：禁止宣传隐藏功能，无论在什么地方，只允许会编程的你自己把玩此功能⚠️
const showFloorInfo = computed(() => localStorage.getItem('enable_hidden_feature') == 'true' && videoInfo.value.totalCommentFloor && videoInfo.value.totalCommentFloor > 0)

const updateVideoDialogVisible = ref(false);
const updateSubmitting = ref(false);

const addUpdatePlanDialogVisible = ref(false);
const videoBackupConfigs = ref(null);
const addUpdatePlanSubmitting = ref(false);


let localVideoCfg = JSON.parse(localStorage.getItem("update_cfg_video"));
let localCommentCfg = JSON.parse(localStorage.getItem("update_cfg_comment"))

const updateVideoForm = reactive({
  bvid: bvid,
  uid: null,
  updateVideoAndDanmaku: true,
  video: localVideoCfg ? localVideoCfg : {
    clarity: 80,
    codecId: 12
  },
  commentConfig: localCommentCfg ? localCommentCfg : {
    mode: "ALL",
    rootLimit: 200,
    replyMode: "ALL",
    replyLimit: 0
  }
});

const addUpdateForm = reactive({
  bvid: bvid,
  uid: null,
  updateVideoAndDanmaku: true,
  video: localVideoCfg ? localVideoCfg : {
    clarity: 80,
    codecId: 12
  },
  update: []
})

let player;

const subtitleSetting = {
  width: 200,
  html: '字幕',
  tooltip: '关闭',
  icon: `<img width="22" heigth="22" src="${subtitleIcon}">`,
  selector: [
    {
      html: '开关',
      tooltip: '启用',
      switch: true,
      onSwitch: function (item) {
        item.tooltip = item.switch ? '关闭' : '启用';
        player.subtitle.show = !item.switch;
        return !item.switch;
      },
    }
  ],
  onSelect: function (item) {
    console.log(item)
    player.subtitle.switch(item.url, {
      name: item.html,
    });
    return item.html;
  },
}


onMounted(() => {
  //const bvidParam = 'BV13f421B7bq'; // 动态获取bvid
  axios.get(`/api/videos/info-ext/${bvid}`).then(({ data }) => {
    if (data.code != 0) {
      ElMessageBox.alert(data.message, '错误', {
        // if you want to disable its autofocus
        // autofocus: false,
        type: 'error',
        confirmButtonText: '确定'
      })
      return;
    }
    const videoData = data.data;
    console.log(videoData);
    if (videoData.videoInfo.state == "FAILED_AND_NO_BACKUP") {
      ElMessageBox.alert("该视频失效且未得到备份", '无效存档', {
        // if you want to disable its autofocus
        // autofocus: false,
        type: 'error',
        confirmButtonText: '确定'
      })
      return;
    }
    videoInfo.value = videoData.videoInfo;
    commentCount.value = videoInfo.value.reply;
    allCommentCount.value = videoData.allCommentCount;
    rootCommentCount.value = videoData.rootCommentCount;
    diskUsage.value = videoData.diskUsage;

    document.title = videoData.videoInfo.title;

    var firstPage = videoInfo.value.pagesVersionList[videoInfo.value.pagesVersionList.length - 1].pages[0];
    currentPage.value = firstPage;

    // 获取最新的剧集版本
    if (videoInfo.value.pagesVersionList && videoInfo.value.pagesVersionList.length > 0) {
      pageVersions.value = videoInfo.value.pagesVersionList;
    }

    player = new Artplayer({
      container: videoPlayer.value,
      url: `/files/archives/videos/${bvid}/${firstPage.cid}/video`, // 动态视频链接
      autoplay: false,
      fullscreen: true,
      aspectRatio: true,
      fullscreenWeb: true,
      playbackRate: true,
      subtitle: {
        type: 'srt',
        encoding: 'utf-8',
        escape: true,
        url: getDefaultSubtitleUrl(),
        style: {
          'color': '#FFFFFF',
          'font-size': '24px'
        },
      },
      setting: true,
      settings: [modifySubtitleItems()],
      plugins: [
        artplayerPluginDanmuku({
          danmuku: `/files/archives/videos/${bvid}/${firstPage.cid}/danmaku`, // 动态弹幕链接
          speed: 8,
          opacity: 0.7,
          fontSize: 12,
          emitter: false,
          heatmap: true,
          antiOverlap: false,
          color: '#FFFFFF',
          mode: 0, // 0 - 滚动，1 - 静止
          margin: [10, 10],
          synchronousPlayback: true,
          width: 200
        }),
      ],
    });
  });
});

onBeforeUnmount(() => {
  if (player) {
    player.destroy();  // 销毁播放器实例，释放内存
    console.log("销毁播放器")
  }
});


function switchPage(page) {
  if (currentPage.value === page) {
    return;
  }
  currentPage.value = page;
  player.pause();
  player.switchUrl(`/files/archives/videos/${bvid}/${page.cid}/video`);
  player.plugins.artplayerPluginDanmuku.config({
    danmuku: `/files/archives/videos/${bvid}/${page.cid}/danmaku`,
  });
  player.plugins.artplayerPluginDanmuku.load();
  player.setting.update(modifySubtitleItems());
}



function getUpAvatarUrl(originalUrl) {
  // 提取文件名部分，即最后的路径
  const fileName = originalUrl.split('/').pop();

  // 构造新的后端 API 地址
  const apiUrl = `/files/archives/uploader-avatars/${fileName}`;

  return apiUrl;
}

function getCodecName(codecId) {
  switch (codecId) {
    case 7:
      return "AVC";
    case 12:
      return "HEVC";
    case 13:
      return "AV1";
    default:
      return "未知编码";
  }
}

function getQualityName(qualityId) {
  switch (qualityId) {
    case 6:
      return "240P";
    case 16:
      return "360P";
    case 32:
      return "480P";
    case 64:
      return "720P";
    case 74:
      return "720P-60帧";
    case 80:
      return "1080P";
    case 112:
      return "1080P-高码率";
    case 116:
      return "1080P-60帧";
    case 120:
      return "4K";
    case 125:
      return "HDR真彩色";
    case 126:
      return "杜比视界";
    case 127:
      return "8K";
    default:
      return "未知画质";
  }
}

function modifySubtitleItems() {
  var firstSelector = subtitleSetting.selector[0];
  subtitleSetting.selector = [firstSelector];
  var defaultSubtitle = getDefaultSubtitle();
  if (defaultSubtitle != null) {
    subtitleSetting.tooltip = defaultSubtitle.lan_doc;
  }
  if (currentPage.value.subtitles) {
    currentPage.value.subtitles.forEach(element => {
      subtitleSetting.selector.push({
        default: defaultSubtitle === element,
        html: element.lan_doc,
        url: `/api/subtitles/${element.id_str}?type=srt`
      })
    });
  }
  return subtitleSetting;
}

function getDefaultSubtitle() {
  var subtitles = currentPage.value.subtitles;
  if (!subtitles) {
    return null;
  }
  for (var index in subtitles) {
    console.log(subtitles[index].lan);
    if (!subtitles[index].lan.startsWith("ai")) {
      return subtitles[index];
    }
  }
  return null;
}

function getDefaultSubtitleUrl() {
  var subtitle = getDefaultSubtitle();
  if (subtitle) {
    return `/api/subtitles/${subtitle.id_str}?type=srt`
  } else {
    return "";
  }
}

function downloadVideo() {
  let page = currentPage.value;
  // 对page.part进行URL编码
  let encodedPart = encodeURIComponent(page.part);
  let url = `/files/archives/videos/${bvid}/${page.cid}/video?filename=${bvid}-c_${page.cid}-${encodedPart}.mp4`;
  window.open(url);
}

function downloadDanmuku() {
  let page = currentPage.value;
  let encodedPart = encodeURIComponent(page.part);
  let url = `/files/archives/videos/${bvid}/${page.cid}/danmaku?filename=${bvid}-c_${page.cid}-${encodedPart}.xml`;
  window.open(url);
}

/*
楼层说明：
使用全部、热门、增量、楼层嗅探都可以获取到最顶楼层
开启隐藏功能才显示楼层相关功能（评论#楼层显示不做限制）
全量下载指的是 全部、增量、楼层嗅探 这三种都是全量下载（根评论），已显示百分比是有价值的。
*/
// 计算百分数 
const percentage = computed(() => (rootCommentCount.value / videoInfo.value.totalCommentFloor * 100).toFixed(2));

// 根据百分数返回对应颜色
// >= 70 绿
// >= 50 黄
// >= 20 红
// >= 0  紫
const percentageColor = computed(() => {
  const value = parseFloat(percentage.value);
  if (value >= 70) return 'green';
  if (value >= 50) return 'orange';
  if (value >= 20) return 'red';
  return 'purple'; // < 20% 为紫色
});


function updateVideo() {
  updateSubmitting.value = true;
  localStorage.setItem("update_cfg_video", JSON.stringify(updateVideoForm.video));
  localStorage.setItem("update_cfg_comment", JSON.stringify(updateVideoForm.commentConfig));
  axios.post("/api/backup/update-video", updateVideoForm)
    .then(({ data }) => {
      updateSubmitting.value = false;
      if (data.code == 0) {
        if (data.data) {
          ElMessage.success("执行更新成功，你可以在主页查看进度");
          updateVideoDialogVisible.value = false;
        } else {
          ElMessage.error("存档姬未运行，请先运行再执行更新")
        }
      } else if(data.code == 2400) {
        ask("处理冲突","视频与现有更新计划冲突，需要移除该视频所有更新计划，是否删除该视频的更新计划？",() => {
          axios.delete("/api/backup/video-update-plans/bvid/"+bvid)
            .then(({data}) => {
              if(data.code == 0){
                ElMessage.success("删除更新任务成功，已删除"+data.data+"个");
              } else {
                ElMessage.error(data.message);
              }
            })
        })
      } else {
        ElMessage.error(data.message);
      }
    })
}

function loadVideoBackupConfigs() {
  if (videoBackupConfigs.value) {
    return;
  }
  axios.get("/api/backup/video-backup-configs")
    .then(({ data }) => {
      if (data.code == 0) {
        videoBackupConfigs.value = data.data;
      } else {
        ElMessage.error(data.message)
      }
    })
}

function useVideoBackupConfig(config) {
  Object.assign(addUpdateForm.video, config.video);
  Object.assign(addUpdateForm.update, config.update);
}

function addVideoUpdatePlans() {
  addUpdatePlanSubmitting.value = true;
  axios.post("/api/backup/add-video-update-plans", addUpdateForm)
    .then(({ data }) => {
      addUpdatePlanSubmitting.value = false;
      if (data.code == 0) {
        if (data.data) {
          ElMessage.success("更新计划添加成功，你可以到更新任务列表查看详情");
          addUpdatePlanDialogVisible.value = false;
        } else {
          ElMessage.error("存档姬未运行，请先运行再执行更新")
        }
      } else {
        ElMessage.error(data.message);
      }
    })
}
</script>
  
<style scoped>
.video-page {
  padding: 0;
}

.card {
  margin-top: 8px;
  padding: 8px;
  background-color: white;
  border-radius: 3px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h3 {
  margin-bottom: 8px;
}

.video-play-card {}

.video-extensions-card {
  padding-inline: 15px;
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #000;
  /* 如果没有视频时的背景颜色 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.uploader {
  display: flex;
  align-items: center;
}

.uploader img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  margin-right: 10px;
  border: 1px solid #ddd;
}


.video-info {
  margin-bottom: 20px;
}

.video-info span {
  display: inline-block;
  margin-right: 10px;
}

.video-info-1 {
  display: flex;
  align-items: flex-end;
  color: gray;
}

.video-id {
  display: flex;
  margin-top: 10px;
  color: gray;
}

.video-id>div {
  margin-right: 8px;
}

.instruct-icon {
  width: 18px;
  height: 18px;
}

.video-info-1>span {
  margin-left: 3px;
  margin-right: 10px;
}

.san-lian {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-block: 12px;
  color: #61666D;
}

.san-lian>div {
  display: flex;
  align-items: center;
}

.san-lian-icon {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.description {
  color: gray;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: keep-all;
}

.page-version {
  background-color: #f1f2f3;
  border-radius: 3px;
  margin-bottom: 10px;
}

.page-version>h4 {
  padding: 10px;
  padding-bottom: 0;
  margin-bottom: 0;
}

.pages {
  display: grid;
  gap: 10px;
  padding: 10px;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
}

.page {
  /* height: 60px; */
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: #fff;
}

.current-page {
  border: 1px solid #FF6699;
  color: #FF6699;
}

.quality-info {
  padding-left: 3px;
  padding-right: 3px;
  line-height: 14px;
  background-color: skyblue;
  font-size: 12px;
  color: white;
  width: fit-content;
  border-radius: 3px 0 5px 0;
}

.page-title {
  /* padding: 4px;
  padding-bottom: 0;
  margin-bottom: 4px; */
  margin: 4px;
  line-height: 18px;
  height: 36px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: keep-all;
}

.tags {
  margin-top: 10px;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  color: #61666D;
  background-color: #F1F2F3;
  height: 28px;
  line-height: 28px;
  border-radius: 14px;
  font-size: 13px;
  padding: 0 12px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

@media only screen and (max-width: 600px) {
  .san-lian {
    justify-content: space-around;
  }

  .san-lian>div {
    flex-direction: column;
    align-items: center;
    font-size: 12px;
  }

  .san-lian-icon {
    margin-right: 0;
  }

  .pages {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

.custom-tag-container {
  display: flex;
  flex-wrap: wrap;
  /* 允许换行 */
  gap: 10px;
  /* 标签间的间距 */
  margin-top: 10px;
}

.custom-tag {
  display: inline-block;
  padding: 5px 10px;
  background-color: #f2f6fc;
  color: #409eff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  white-space: normal;
  /* 允许内容换行 */
  word-break: break-word;
  /* 如果是长词，允许断字 */
  cursor: default;
  box-sizing: border-box;
}

.custom-tag:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
}


/* .info-card{
  display: inline-block;
  border: gray 1px solid;
  border-radius: 4px;
  padding: 5px;
  text-align: center;
} */

.info-cards {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.info-cards>div {
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
  min-width: 140px;
  /* border: #409eff 1px solid; */
}

.info-cards>div>div:first-child {
  display: flex;
  color: white;
  width: 100%;
  /* height: 26px; */
  padding: 6px;
  background-color: #409eff;
  font-weight: bold;
  align-items: center;
  justify-content: center;
}

.info-cards>div>div:last-child {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F1F2F3;
  padding: 6px;
  width: 100%;
}

:deep(.el-card) {
  margin-bottom: 20px;
}

.el-form {
  max-height: 77vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.el-form::-webkit-scrollbar {
  display: none;
}
</style>