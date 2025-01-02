<template>
  <div class="history-header">
    <el-date-picker v-model="asOf" type="datetime" @change="change" placeholder="设置截止日期" />
    <div style="display: flex;">
      <el-checkbox v-model="failedOnly" label="只看失效" @change="change" size="default" border />
      <el-button :icon="Setting" @click="openSetting()" :loading="settingLoading" />
    </div>
  </div>
  <el-dialog v-model="dialogFormVisible" title="历史记录备份设置" style="min-width: 350px;width: 90%; max-width: 600px;"
    align-center>
    <!-- <el-scrollbar max-height="80vh"> -->
    <el-form :model="form" label-position="top">
      <el-form-item label="用户ID">
        <el-input v-model="form.uid" disabled />
      </el-form-item>

      <el-form-item label="自动删除方法">
        <el-select v-model="form.autoDeleteMethod" placeholder="请选择自动删除方法">
          <el-option label="按天数" value="DAYS" />
          <el-option label="按磁盘使用量" value="DISK_USAGE" />
          <el-option label="按条目数量" value="ITEM_QUANTITY" />
        </el-select>
      </el-form-item>

      <template v-if="form.autoDeleteMethod === 'DAYS'">
        <el-form-item label="按天数删除">
          <el-input-number v-model="form.deleteByDays" :min="0" placeholder="天数" />
        </el-form-item>
      </template>

      <template v-if="form.autoDeleteMethod === 'DISK_USAGE'">
        <el-form-item label="按磁盘使用删除">
          <div style="display: flex;width: 100%;">
            <el-input-number v-model="form.deleteByDiskUsage" :min="0" placeholder="磁盘使用量" style="flex: 1000;" />
            <el-select v-model="form.deleteByDiskUsageUnit" placeholder="选择单位" style="flex: 618;">
              <el-option label="Byte" value="B" />
              <el-option label="KB" value="KB" />
              <el-option label="MB" value="MB" />
              <el-option label="GB" value="GB" />
              <el-option label="TB" value="TB" />
            </el-select>
          </div>
        </el-form-item>
      </template>

      <template v-if="form.autoDeleteMethod === 'ITEM_QUANTITY'">
        <el-form-item label="按条目数量删除">
          <el-input-number v-model="form.deleteByItemQuantity" :min="0" placeholder="条目数量" />
        </el-form-item>
      </template>

      <el-form-item label="排除发布日期长度(天)">
        <el-input-number v-model="form.releaseTimeLimitDay" placeholder="不排除" />
      </el-form-item>

      <el-form-item label="UP主黑名单">
        <UpBlackList v-model="form.uploaderBlackList"></UpBlackList>
      </el-form-item>

      <el-form-item label="启用视频备份">
        <el-switch v-model="form.videoBackupEnable" />
      </el-form-item>
      <el-form-item label="视频备份配置">
        <el-select v-model="form.videoBackupConfigId" placeholder="请选择备份配置" v-loading="!backupConfigOptions">
          <el-option v-for="item in backupConfigOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer" style="display: flex; justify-content: space-between; align-items: center;">
        <!-- 删除备份按钮放左边，仅当isCreateNewConfig为false时显示 -->
        <el-button type="danger" style="margin-right: auto;" v-if="!isCreateNewConfig"
          @click="deleteHistoryBackup()">删除备份</el-button>

        <!-- 右侧的按钮，添加flex-grow: 1来保持右侧位置 -->
        <div style="flex-grow: 1; text-align: right;">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button v-if="isCreateNewConfig" type="primary" :disabled="!form.videoBackupConfigId"
            @click="createNewConfig()" :loading="confirmLoading">
            创建配置
          </el-button>
          <el-button v-else type="primary" @click="updateConfig()" :loading="confirmLoading">
            更新
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
  <OperationalDialog :prog="operationalProgress"></OperationalDialog>
  <!-- <VideoGrid v-infinite-scroll="load" :videoList="videoList"> -->
  <el-empty description="还没有视频" v-if="videoList.length < 1 && !loading" />
  <div class="video-grid" v-infinite-scroll="load" :infinite-scroll-disabled="loading || noMore" infinite-scroll-distance="1080">
    <VideoCard v-for="video in videoList" :video="video" :key="video.bvid" :actions="actions">
      <template #meta1>
        <MetaUploader :video="video"></MetaUploader>
      </template>
      <template #meta2>
        <MetaVideoStat :video="video"></MetaVideoStat>
      </template>
      <template #meta3>
        <MetaViewAt :timestamp="video.viewAt"></MetaViewAt>
      </template>
    </VideoCard>
  </div>
  <div v-if="loading" class="mdui-progress">
    <div class="mdui-progress-indeterminate"></div>
  </div>
  <!-- <VideoGrid :videoList="videoList">

  </VideoGrid> -->
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Setting } from "@element-plus/icons-vue";
import { closeDrawer, confirmWithInput } from '../util';
import { reactive } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import axios from 'axios';
import MetaViewAt from '../components/MetaViewAt.vue';
import MetaUploader from '../components/MetaUploader.vue';
import MetaVideoStat from '../components/MetaVideoStat.vue';
import UpBlackList from '../components/UpBlackList.vue';
import OperationalDialog from '../components/OperationalDialog.vue';
import VideoCard from '../components/VideoCard.vue';

const roter = useRoute();
const uid = roter.params.uid;
const videoList = ref([]);
var loading = ref(false);
var pn = 1;

const settingLoading = ref(false);
const isCreateNewConfig = ref(false);

const asOf = ref(null)
const failedOnly = ref(false);

const dialogFormVisible = ref(false);

const backupConfigOptions = ref(null);

const confirmLoading = ref(false);

const operationalProgress = ref(null);

const noMore = ref(false);

const actions = [{
  title: "删除历史视频", onClick: video => {
    closeDrawer();
    ElMessageBox.confirm(
      `确认删除此历史记录视频『${video.title}』吗？请确保你以后不会再看它，否则会再次存档！！！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        let notification = ElNotification({
          title: '正在删除历史记录视频……',
          message: video.title,
          duration: 0,
        });
        axios.delete(`/api/backup/history/${uid}/${video.bvid}`)
          .then(({ data }) => {
            notification.close();
            if (data.code == 0) {
              videoList.value.splice(videoList.value.indexOf(video), 1);
              if (data.data) {
                ElMessage.success("历史记录及视频存档成功");
              } else {
                ElMessage.success("历史记录删除成功，但视频存档有其他备份项在引用，已保留");
              }

            } else {
              ElMessage.error(data.message);
            }
          })

      })
  }
}]

const load = () => {
  if (loading.value || noMore.value) {
    return;
  }
  loading.value = true;
  axios.get(`/api/videos/history/${uid}`, { params: { pn, failed_only: failedOnly.value, as_of: asOf.value ? asOf.value.getTime() / 1000 : null } })
    .then(({data}) => {
      let list = data.data;
      if(list.length < 1){
        noMore.value = true;
      } else {
        videoList.value.push(...list);
        pn++;
      }
      loading.value = false;
    })
}

load();

function change() {
  console.log("change")
  videoList.value = [];
  pn = 1;
  load();
}

async function openSetting() {
  settingLoading.value = true;
  closeDrawer();
  let data = (await axios.get("/api/backup/history/" + uid)).data;
  if (data.code == 0) {
    isCreateNewConfig.value = false;
    Object.assign(form, data.data);
  } else if (data.code == 404) {
    isCreateNewConfig.value = true;
  } else {
    ElMessage.error(data.message)
    return;
  }

  if (!backupConfigOptions.value) {
    data = (await axios.get("/api/backup/video-backup-configs")).data;
    if (data.code == 0) {
      backupConfigOptions.value = data.data;
    }
  }
  settingLoading.value = false;
  dialogFormVisible.value = true;
}


const form = reactive({
  uid: uid,
  autoDeleteMethod: 'DAYS',
  deleteByDays: 30,
  deleteByDiskUsage: 0,
  deleteByItemQuantity: 0,
  releaseTimeLimitDay: null,
  uploaderBlackList: [],
  videoBackupEnable: true,
  deleteByDiskUsageUnit: "MB",
  videoBackupConfigId: null
});


function createNewConfig() {
  confirmLoading.value = true;
  console.log(form);
  axios.post("/api/backup/history", form)
    .then(({ data }) => {
      confirmLoading.value = false;
      if (data.code == 0) {
        dialogFormVisible.value = false;
        ElMessage.success("创建配置成功")
      } else {
        ElMessage.error(data.message);
      }
    })
}

function updateConfig() {
  confirmLoading.value = true;
  console.log(form);
  axios.put("/api/backup/history", form)
    .then(({ data }) => {
      confirmLoading.value = false;
      if (data.code == 0) {
        dialogFormVisible.value = false;
        ElMessage.success("更新配置成功")
      } else {
        ElMessage.error(data.message);
      }
    })
}

function deleteHistoryBackup() {
  confirmWithInput("警告", "确认删除历史记录备份吗？这将一同删除备份的视频（如果没有其他备份项引用）", "确认删除",() => {
    dialogFormVisible.value = false;
    let source = new EventSource("/api/backup/delete-history/" + uid)
    let isFirst = true;
    source.onmessage = event => {
      let data = event.data;
      if (isFirst) {
        isFirst = false;
        switch (data) {
          case "SUCCESS":
            return;
          case "BACKUP_IS_RUNNING":
            ElMessage.error("备份正在运行，请先停止运行");
            break;
          case "NOT_FOUND":
            ElMessage.error("历史记录备份未找到");
            break;
          case "OTHER_TASKS_IN_PROGRESS":
            ElMessage.error("无法删除，因为有其他删除或刷新任务在进行，请等待它们执行完毕再操作");
            break;
          default:
            ElMessage.warning(data);
        }
        source.close();
        return;
      }
      let prog = JSON.parse(event.data)
      console.log(prog)
      if (prog.type == -1) {
        source.close();
        operationalProgress.value = null;
        videoList.value = [];
        ElMessage.success("删除成功！");
      } else {
        operationalProgress.value = prog;
      }
    }
  })
}
</script>

<style scoped>
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.el-form {
  max-height: 77vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.el-form::-webkit-scrollbar {
  display: none;
}

.mdui-progress {
  max-width: 300px;
  margin-top: 10px;
  margin-inline: auto;
}
</style>