<template>
  <div class="grid">
    <div v-for="(up, index) in upList" class="up-card">
      <div class="header">
        <img class="up-avatar" :src="getBCUploaderAvatarUrl(up.avatarUrl)">
        <div class="up-details" style="flex: 1;">
          <p class="up-name">{{ up.name }}</p>
          <p class="uid">UID:{{ up.uid }}</p>
        </div>

        <el-dropdown trigger="click">
          <span class="options-icon mdui-ripple">⋮</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="changeConfig(up)">修改配置</el-dropdown-item>
              <el-dropdown-item @click="openTransferBackupUserDialog(up)">移交备份用户</el-dropdown-item>
              <el-dropdown-item @click="refreshUpVideos(up)">刷新所有视频状态</el-dropdown-item>
              <el-dropdown-item @click="deleteUpBackup(up, index)">删除备份</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="desc">{{ up.desc }}</div>
      <div class="actions">
        <button class="video-btn" @click="open('up-video', up)">
          <svg t="1729673849414" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="23589" width="200" height="200">
            <path
              d="M206.935452 206.935452a50.844091 50.844091 0 0 0-50.844092 50.844091v508.440914a50.844091 50.844091 0 0 0 50.844092 50.844091h610.129096a50.844091 50.844091 0 0 0 50.844092-50.844091v-508.440914a50.844091 50.844091 0 0 0-50.844092-50.844091z m0-101.688183h610.129096a152.532274 152.532274 0 0 1 152.532274 152.532274v508.440914a152.532274 152.532274 0 0 1-152.532274 152.532274h-610.129096a152.532274 152.532274 0 0 1-152.532274-152.532274v-508.440914a152.532274 152.532274 0 0 1 152.532274-152.532274z"
              fill="#ffffff" p-id="23590"></path>
            <path
              d="M651.821251 559.285005l-162.701092 108.297915a50.844091 50.844091 0 0 1-78.808342-42.200596V410.311817a50.844091 50.844091 0 0 1 78.808342-42.200596l162.701092 108.297915a50.844091 50.844091 0 0 1 0 84.401192z"
              fill="#ffffff" p-id="23591"></path>
          </svg>
          视频
        </button>

        <button class="dynamic-btn" @click="open('up-dynamic', up)">
          <svg t="1729673705449" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="19712" width="200" height="200">
            <path
              d="M512 85.333333a229.845333 229.845333 0 0 1 161.28 393.642667h232.32c18.261333 0 33.066667 14.762667 33.066667 33.024a229.845333 229.845333 0 0 1-393.642667 161.28v232.32A33.066667 33.066667 0 0 1 512 938.666667a229.845333 229.845333 0 0 1-161.28-393.642667H118.4A33.066667 33.066667 0 0 1 85.333333 512a229.845333 229.845333 0 0 1 393.642667-161.28V118.4c0-18.261333 14.762667-33.066667 33.024-33.066667z m-33.066667 463.104l-2.048 0.426667a163.84 163.84 0 0 0 0 319.914667l2.048 0.341333v-320.682667z m390.186667-3.413333h-320.682667l0.426667 2.133333a163.84 163.84 0 0 0 150.954667 128.341334l8.96 0.256a163.84 163.84 0 0 0 160-128.64l0.341333-2.048zM315.178667 348.288a163.84 163.84 0 0 0-159.957334 128.64l-0.426666 2.048h320.725333l-0.341333-2.048a163.84 163.84 0 0 0-150.997334-128.384z m229.888-193.450667v320.725334l2.048-0.341334a163.84 163.84 0 0 0 128.384-150.997333l0.256-8.96a163.84 163.84 0 0 0-128.64-160l-2.048-0.426667z"
              fill="#ffffff" p-id="19713"></path>
          </svg>
          动态
        </button>

        <button class="article-btn" @click="open('up-article', up)">
          <svg t="1729673615364" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="9283" width="200" height="200">
            <path
              d="M298.666667 256h170.666666v170.666667H298.666667zM554.666667 298.666667h170.666666v85.333333h-170.666666zM298.666667 512h426.666666v85.333333H298.666667zM298.666667 682.666667h426.666666v85.333333H298.666667z"
              fill="#09121F" p-id="9284"></path>
            <path
              d="M853.333333 85.333333H170.666667c-25.6 0-42.666667 17.066667-42.666667 42.666667v768c0 25.6 17.066667 42.666667 42.666667 42.666667h682.666666c25.6 0 42.666667-17.066667 42.666667-42.666667V128c0-25.6-17.066667-42.666667-42.666667-42.666667z m-42.666666 768H213.333333V170.666667h597.333334v682.666666z"
              fill="#09121F" p-id="9285"></path>
          </svg>
          文章
        </button>

        <button class="live-btn" @click="open('up-live-record', up)">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M8 1a7 7 0 1 1-7 7 7 7 0 0 1 7-7zm0 1.5a5.5 5.5 0 1 0 5.5 5.5A5.5 5.5 0 0 0 8 2.5zm0 1.5a4 4 0 1 1-4 4 4 4 0 0 1 4-4zm0 1a3 3 0 1 0 3 3 3 3 0 0 0-3-3zm0 1.5a1.5 1.5 0 1 1-1.5 1.5A1.5 1.5 0 0 1 8 6.5z" />
          </svg>
          直播录制
        </button>
      </div>

    </div>
    <div class="up-card add-up mdui-ripple" @click="addUpDialogVisible = true; isAddMode = true; closeDrawer();">
      <div class="add-content">
        <span class="add-icon">+</span>
        <p>添加UP主备份</p>
      </div>
    </div>
  </div>
  <el-dialog v-model="addUpDialogVisible" title="输入UP主的UID" style="min-width: 300px;width: 75%; max-width: 400px;"
    @closed="pendingAddUpUid = null">
    <el-input placeholder="UID" v-model="pendingAddUpUid" />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="addUpDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pendingAddUpUid" @click="toAddUploader(pendingAddUpUid)"
          :loading="isGettingUploaderInfo">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="editConfigDialogVisible" title="编辑UP主备份信息" style="width: 90%; max-width: 800px; min-width: 300px"
    align-center>
    <el-form :model="form" label-width="120px" :label-position="labelPosition">
      <!-- 禁用字段 -->
      <el-form-item label="UID">
        <el-input v-model="form.uid" disabled></el-input>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="form.name" disabled></el-input>
      </el-form-item>
      <el-form-item label="描述">
        <el-input type="textarea" v-model="form.desc" disabled></el-input>
      </el-form-item>
      <el-form-item label="头像链接">
        <el-input v-model="form.avatarUrl" disabled></el-input>
      </el-form-item>
      <el-form-item label="备份用户ID" prop="backupUserId">
        <el-input v-model="form.backupUserId" disabled />
      </el-form-item>

      <!-- 可配置字段 -->
      <el-form-item label="备份起始时间点">
        <el-date-picker v-model="backupStartTime" type="datetime" placeholder="留空则从第一个视频开始"
          style="width: 100%;"></el-date-picker>
      </el-form-item>

      <el-form-item label="视频备份">
        <el-switch v-model="form.videoBackupEnable"></el-switch>
      </el-form-item>
      <el-form-item label="视频备份配置">
        <el-select v-model="form.videoBackupConfigId" placeholder="请选择配置" v-loading="!videoBackupConfigs">
          <el-option v-for="config in videoBackupConfigs" :key="config.id" :label="config.name"
            :value="config.id"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="动态备份(未开发)">
        <el-switch v-model="form.dynamicBackupEnable" disabled></el-switch>
      </el-form-item>

      <el-form-item label="文章备份(未开发)">
        <el-switch v-model="form.articleBackupEnable" disabled></el-switch>
      </el-form-item>

      <el-form-item label="直播录制(未开发)">
        <el-switch v-model="form.liveRecordingEnable" disabled></el-switch>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer" style="display: flex; justify-content: space-between; align-items: center;">
        <el-button type="primary" style="margin-right: auto;" v-if="!isAddMode" @click="updateUploaderInfo(form)"
          plain>刷新UP主信息</el-button>
        <div style="flex-grow: 1; text-align: right;">
          <el-button @click="editConfigDialogVisible = false">取消</el-button>
          <el-button v-if="isAddMode" type="primary" @click="addUploaderBackup(form)"
            :disabled="!form.videoBackupConfigId" :loading="isSubmitting">添加</el-button>
          <el-button v-else type="primary" @click="updateUploaderBackup(form)" :loading="isSubmitting">更新</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="transferDialogVisible" title="选择移交备份的用户" style="min-width: 300px;width: 75%; max-width: 400px;">
    <el-select v-model="form.backupUserId" placeholder="请选择用户">
      <el-option v-for="user in userStore.userList" :key="user.uid" :label="user.name" :value="user.uid"></el-option>
    </el-select>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="form.backupUserId && form.backupUserId == uid"
          @click="transferBackupUser(form)" :loading="isSubmitting">移交</el-button>
      </div>
    </template>
  </el-dialog>
  <OperationalDialog :prog="operationalProgress"></OperationalDialog>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useRoute } from 'vue-router';
import { getBCUploaderAvatarUrl } from "../util"
import { ElMessage } from 'element-plus';
import { closeDrawer, confirmWithInput } from '../util';
import { ElMessageBox } from 'element-plus';
import OperationalDialog from '../components/operationaldialog.vue';
import { useUserStore } from '../stores/userStore';


const router = useRouter();
const route = useRoute();

const uid = route.params.uid;

const upList = ref([])

const pendingAddUpUid = ref(null);

const addUpDialogVisible = ref(false);
const editConfigDialogVisible = ref(false);
const transferDialogVisible = ref(false);

const isGettingUploaderInfo = ref(false);
const isAddMode = ref(true);

const isSubmitting = ref(false);

const userStore = useUserStore();

const windowWidth = ref(window.innerWidth);

const operationalProgress = ref(null);


// 响应式计算 label-position
const labelPosition = computed(() => (windowWidth.value > 600 ? 'right' : 'top'));

// 监听窗口变化
const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth;
};

onMounted(() => {
  window.addEventListener('resize', updateWindowWidth);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateWindowWidth);
});

const form = reactive({});

const videoBackupConfigs = ref(null);

const backupStartTime = computed({
  get() {
    if (form.backupStartTime) {
      return new Date(form.backupStartTime * 1000);
    }
    return null;
  },
  set(date) {
    if (date) {
      form.backupStartTime = date.getTime() / 1000;
    } else {
      form.backupStartTime = null;
    }
  }
});


function open(name, up) {
  router.push({ name, params: { upUid: up.uid } })
}


axios.get("/api/backup/user-uploader/" + uid)
  .then(resp => {
    if (resp.data.code == 0) {
      upList.value = resp.data.data;
    }
  });

function changeConfig(up) {
  closeDrawer();
  if (!videoBackupConfigs.value) {
    axios.get("/api/backup/video-backup-configs")
      .then(({ data }) => {
        if (data.code == 0) {
          videoBackupConfigs.value = data.data;
        } else {
          ElMessage.error(data.message);
        }
      })
  }
  editConfigDialogVisible.value = true;
  isAddMode.value = false;
  Object.assign(form, up);
}

async function toAddUploader(upUid) {
  isGettingUploaderInfo.value = true;
  let resp = (await axios.get("/api/bili-api-proxy/x/v2/space", { params: { vmid: upUid, cookie_uid: uid } })).data;

  console.log(resp);
  if (resp.code != 0) {
    ElMessage.error(resp.message);
    isGettingUploaderInfo.value = false;
    return;
  }
  if (resp.data.code != 0) {
    ElMessage.error(resp.data.message);
    isGettingUploaderInfo.value = false;
    return;
  }

  let data = resp.data.data;


  if (!videoBackupConfigs.value) {
    resp = await axios.get("/api/backup/video-backup-configs");
    if (resp.data.code == 0) {
      videoBackupConfigs.value = resp.data.data;
    } else {
      ElMessage.error(resp.data.message);
      return;
    }
  }

  isGettingUploaderInfo.value = false;

  let newForm = {
    uid: data.card.mid,
    name: data.card.name,
    desc: data.card.sign,
    avatarUrl: data.card.face,
    backupStartTime: null,
    backupUserId: uid,
    videoBackupEnable: true,
    videoBackupConfigId: null,
    dynamicBackupEnable: false,
    dynamicBackupConfig: null,
    articleBackupEnable: false,
    articleBackupConfig: null,
    liveRecordingEnable: false,
    liveRecordingConfig: null,
  }
  Object.assign(form, newForm);
  addUpDialogVisible.value = false;
  editConfigDialogVisible.value = true;
  //addUpDialogVisible.value = false;
}

function addUploaderBackup(formData) {
  isSubmitting.value = true;
  axios.post("/api/backup/uploader", formData)
    .then(({ data }) => {
      isSubmitting.value = false;
      if (data.code == 0) {
        upList.value.push(data.data);
        editConfigDialogVisible.value = false;
        ElMessage.success("添加成功");
      } else {
        ElMessage.error(data.message);
      }
    })
}

function updateUploaderBackup(formData) {
  isSubmitting.value = true;
  axios.put("/api/backup/uploader", formData)
    .then(({ data }) => {
      isSubmitting.value = false;
      if (data.code == 0) {
        let uploader = data.data;
        for (let up of upList.value) {
          if (up.uid == uploader.uid) {
            Object.assign(up, uploader);
            break;
          }
        }
        editConfigDialogVisible.value = false;
        ElMessage.success("更新成功");
      } else {
        ElMessage.error(data.message);
      }
    })
}

function updateUploaderInfo(formData) {
  axios.get("/api/bili-api-proxy/x/v2/space", { params: { vmid: formData.uid, cookie_uid: uid } })
    .then(({ data }) => {
      if (data.code == 0) {
        if (data.data.code == 0) {
          data = data.data.data;
          form.name = data.card.name;
          form.desc = data.card.sign;
          form.avatarUrl = data.card.face;
          ElMessage.success("已刷新UP主信息，但还未保存");
        } else {
          ElMessage.error(data.data.message)
        }
      } else {
        ElMessage.error(data.message)
      }
    });
}

function deleteUpBackup(up, index) {
  closeDrawer();
  confirmWithInput('警告',
    '确认删除UP主备份『' + up.name + '』吗？视频将一并删除（如果没有其他备份项引用）',
    "确认删除", () => {
      let source = new EventSource("/api/backup/delete-uploader/" + up.uid)
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
              ElMessage.error("UP主备份项未找到");
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
          upList.value.splice(index, 1)
          ElMessage.success("删除成功！");
        } else {
          operationalProgress.value = prog;
        }
      }
    })
}

function openTransferBackupUserDialog(up) {
  closeDrawer();
  Object.assign(form, up);
  transferDialogVisible.value = true;
}

function transferBackupUser(formData) {
  isSubmitting.value = true;
  axios.put("/api/backup/uploader", formData)
    .then(({ data }) => {
      transferDialogVisible.value = false;
      isSubmitting.value = false;
      if (data.code == 0) {
        let uploader = data.data;
        for (let i in upList.value) {
          if (upList.value[i].uid == uploader.uid) {
            upList.value.splice(i, 1)
            break;
          }
        }
        ElMessage.success("移交成功");
      } else {
        ElMessage.error(data.message);
      }
    })
}

function refreshUpVideos(up) {
  closeDrawer();
  let source = new EventSource("/api/backup/update-uploader-videos/" + up.uid);
  let isFirst = true;
  source.onmessage = event => {
    let data = event.data;
    if (isFirst) {
      isFirst = false;
      switch (data) {
        case "SUCCESS":
          return;
        case "NOT_FOUND":
          ElMessage.error("UP主备份未找到");
          break;
        case "OTHER_TASKS_IN_PROGRESS":
          ElMessage.error("当前无法刷新，因为有其他任务在进行，请等待它们执行完毕再操作");
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
      ElMessage.success("刷新完成");
    } else {
      operationalProgress.value = prog;
    }
  }
}

</script>

<style scoped>
/*  */

@media only screen and (min-width: 620px) {
  .grid {
    display: grid;
    gap: 10px;
    grid-template-columns: repeat(auto-fill, minmax(560px, 1fr));
  }
}

.up-card {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 0 4px 0 rgba(0, 0, 0, 0.20);
  padding: 20px;
  margin-bottom: 20px;
  /* max-width: 400px; */
  margin: 4px 4px 8px 4px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.up-avatar {
  border-radius: 50%;
  width: 60px;
  height: 60px;
  margin-right: 15px;
  object-fit: cover;
}

.up-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.up-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.uid {
  font-size: 14px;
  color: #777;
  margin: 5px 0 0 0;
}

.desc {
  font-size: 16px;
  color: #555;
  margin: 10px 0;
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  /* 自动填充，每个按钮最小宽度120px，最大为1fr */
  gap: 10px;
  margin-top: 15px;
}

.actions button {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #007BFF;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  font-size: 14px;
  text-align: center;
}

.actions button:hover {
  background-color: #0056b3;
}

.actions button svg {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  fill: currentColor;
  /* 让SVG继承文本颜色 */
}

.actions button.video-btn {
  background-color: #28a745;
}

.actions button.video-btn:hover {
  background-color: #218838;
}

.actions button.dynamic-btn {
  background-color: #17a2b8;
}

.actions button.dynamic-btn:hover {
  background-color: #117a8b;
}

.actions button.article-btn {
  background-color: #ffc107;
  color: #212529;
}

.actions button.article-btn:hover {
  background-color: #e0a800;
}

.actions button.live-btn {
  background-color: #dc3545;
}

.actions button.live-btn:hover {
  background-color: #c82333;
}


/* =============== */

/* .up-card {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 0 4px 0 rgba(0, 0, 0, 0.20);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
} */

.options-icon {
  font-size: 24px;
  width: 30px;
  height: 30px;
  color: #888;
  cursor: pointer;
  border-radius: 50%;
  text-align: center;
  line-height: 30px;
}

.add-up {
  color: #999;
  cursor: pointer;
  border: 1px dashed #ccc;
  background-color: #f9f9f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.add-icon {
  font-size: 50px;
  margin-bottom: 10px;
}

.add-content p {
  font-size: 16px;
  font-weight: bold;
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