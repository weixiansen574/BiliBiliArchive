<template>
  <div class="fav-list">
    <div v-for="(fav, index) in favList" :key="fav.backup.favId" @click="openFav(fav.backup.favId)"
      class="fav-item mdui-ripple">
      <div class="fav-thumbnail">
        <div class="thumbnail-overlay t1"></div>
        <div class="thumbnail-overlay t2"></div>
        <div></div>
        <img :src="getCoverUrl(fav.firstVideo)" :alt="fav.backup.favName" class="video-thumbnail" />
        <div class="fav-overlay">
          <span class="icon"></span>
          <span>{{ fav.videoCount }}</span>
        </div>
      </div>
      <div class="fav-meta">
        <h3>{{ fav.backup.favName }}</h3>
        <p>ID: {{ fav.backup.favId }}</p>
        <p>所属：{{ fav.backup.ownerName }}</p>
      </div>
      <el-dropdown class="fav-options" trigger="click">
        <span class="options-icon mdui-ripple" @click.stop="">⋮</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="editFavConfig(fav.backup)">修改配置</el-dropdown-item>
            <el-dropdown-item @click="openTransferBackupUserDialog(fav.backup)">移交备份用户</el-dropdown-item>
            <el-dropdown-item @click="freshFav(fav.backup.favId)">刷新所有视频状态</el-dropdown-item>
            <el-dropdown-item @click="askToDeleteFav(fav.backup, index)">删除收藏夹备份</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <!-- <div class="fav-options mdui-ripple" @click.stop="showOptions(fav)">
        <span class="options-icon">⋮</span>
        
      </div> -->
    </div>
    <div class="fav-item mdui-ripple" @click="addFavBackup()">
      <div class="fav-thumbnail">
        <div class="thumbnail-overlay t1"></div>
        <div class="thumbnail-overlay t2"></div>
        <div></div>
        <div class="video-thumbnail add-new">+</div>
        <!-- <img :src="getCoverUrl(fav.first)" :alt="fav.name" class="video-thumbnail" /> -->
      </div>
      <div class="fav-meta">
        <h3>添加收藏夹备份</h3>
      </div>
    </div>
  </div>
  <el-dialog v-model="addFavDialogVisible" title="添加收藏夹" class="dialog" align-center
    style="width: 90%; max-width: 700px; min-width: 300px" @closed="radio = null;
    favAddList = null;
    ">
    <el-tabs v-model="activeName">
      <el-tab-pane label="我的收藏夹" name="my-fav">
        <el-scrollbar max-height="400px">
          <el-table ref="multipleTable" :data="favAddList" v-loading="!favAddList">
            <el-table-column label="收藏夹" min-width="180px">
              <template #default="scope">
                <el-radio :label="scope.row.id" v-model="radio" :disabled="scope.row.disabled">{{ scope.row.title
                }}</el-radio>
              </template>
            </el-table-column>
            <!-- <el-table-column align="center" prop="title" label="名称"></el-table-column> -->
            <el-table-column align="center" prop="id" label="ID" min-width="110px"></el-table-column>
            <el-table-column align="center" prop="media_count" label="数量" width="60px"></el-table-column>
            <!-- <el-table-column align="center" prop="phone" label="账号"></el-table-column> -->
          </el-table>
        </el-scrollbar>
      </el-tab-pane>
      <el-tab-pane label="手动输入ID" name="id-fav">
        <el-input v-model="radio" placeholder="请输入收藏夹ID" />
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="addFavDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="toAddFav(radio)" :disabled="radio == 0 || !radio" :loading="edtFavLoading">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="edtCondfigDialogVisible" title="编辑收藏夹备份信息" @closed="Object.assign(formData, {})"
    style="width: 85%; max-width: 800px; min-width: 300px">
    <el-form :model="formData" :label-width="formLabelWidth" v-loading="!formData.favId">
      <el-form-item label="收藏夹ID" prop="favId">
        <el-input v-model="formData.favId" disabled />
      </el-form-item>

      <el-form-item label="收藏夹名称" prop="favName">
        <el-input v-model="formData.favName" disabled />
      </el-form-item>

      <el-form-item label="所属用户UID" prop="ownerUid">
        <el-input v-model="formData.ownerUid" disabled />
      </el-form-item>

      <el-form-item label="所属用户名" prop="ownerName">
        <el-input v-model="formData.ownerName" disabled />
      </el-form-item>

      <el-form-item label="备份用户ID" prop="backupUserId">
        <el-input v-model="formData.backupUserId" disabled />
      </el-form-item>

      <el-form-item label="视频备份启用" prop="videoBackupEnable">
        <el-switch v-model="formData.videoBackupEnable" />
      </el-form-item>

      <el-form-item label="视频备份配置" prop="videoBackupConfigId">
        <el-select v-model="formData.videoBackupConfigId" placeholder="请选择备份配置" v-loading="!backupConfigOptions">
          <el-option v-for="item in backupConfigOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="edtCondfigDialogVisible = false">取消</el-button>
        <el-button v-if="edtCondfigDialogMode" type="primary" @click="update(formData)" :loading="edtFavLoading"
          :disabled="!formData.videoBackupConfigId">
          更新
        </el-button>
        <el-button v-else type="primary" @click="submit(formData)" :loading="edtFavLoading"
          :disabled="!formData.videoBackupConfigId">
          添加
        </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="transferDialogVisible" title="选择移交备份的用户" style="min-width: 300px;width: 75%; max-width: 400px;">
    <el-select v-model="formData.backupUserId" placeholder="请选择用户">
      <el-option v-for="user in userStore.userList" :key="user.uid" :label="user.name" :value="user.uid"></el-option>
    </el-select>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="formData.backupUserId && formData.backupUserId == uid"
          @click="transferBackupUser(formData)" :loading="edtFavLoading">移交</el-button>
      </div>
    </template>
  </el-dialog>
  <OperationalDialog :prog="operationalProgress"></OperationalDialog>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus'
import { closeDrawer, confirmWithInput } from '../util';
import OperationalDialog from "../components/OperationalDialog.vue"
import { useUserStore } from '../stores/userStore';
import playlistbg from "../assets/playlistbg.png"

const route = useRoute();
const router = useRouter();

const radio = ref(null)

const uid = route.params.uid;

const favList = ref([]);
const favAddList = ref();

const addFavDialogVisible = ref(false);
const edtCondfigDialogVisible = ref(false);
const edtCondfigDialogMode = ref(false);//false添加模式，true编辑模式
const edtFavLoading = ref(false);
const transferDialogVisible = ref(false);

const formLabelWidth = '120px';

const formData = reactive({});

const activeName = ref("my-fav");

const userStore = useUserStore();

// const rules =reactive( {
//         videoBackupConfigId: [
//           { required: true, message: '请选择备份配置', trigger: 'change' }  // 添加校验规则，必填，在值改变时触发校验
//         ]
//       })

const backupConfigOptions = ref(null);


const operationalProgress = ref(null);

axios.get(`/api/backup/user-fav/${uid}`)
  .then(response => {
    favList.value = response.data.data;
  })

function openFav(favId) {
  router.push({
    name: "fav-videos",
    params: {
      favId
    }
  })
}


function freshFav(favId) {
  closeDrawer();
  let source = new EventSource("/api/backup/update-fav-videos/" + favId);
  let isFirst = true;
  source.onmessage = event => {
    let data = event.data;
    if (isFirst) {
      isFirst = false;
      switch (data) {
        case "SUCCESS":
          return;
        case "NOT_FOUND":
          ElMessage.error("收藏夹未找到");
          break;
        case "OTHER_TASKS_IN_PROGRESS":
          ElMessage.error("当前无法更新，因为有其他任务在进行，请等待它们执行完毕再操作");
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

function getCoverUrl(video) {
  if (video == null) {
    return playlistbg;
  }
  return `/files/archives/videos/${video.bvid}/cover`;
}

function addFavBackup() {
  closeDrawer();
  addFavDialogVisible.value = true;
  axios.get("/api/bili-api-proxy/x/v3/fav/folder/created/list-all", { params: { up_mid: uid, cookie_uid: uid } })
    .then(resp => {
      let body = resp.data;
      if (body.code == 0) {
        if (body.data.code == 0) {
          favAddList.value = body.data.data.list;
        } else {
          ElMessage.error(body.data.message);
        }
      } else {
        ElMessage.error(body.message);
      }
    })
}

async function toAddFav(favId) {
  edtCondfigDialogMode.value = false;
  addFavDialogVisible.value = false;
  edtCondfigDialogVisible.value = true;
  let resp = await axios.get("/api/bili-api-proxy/x/v3/fav/folder/info", { params: { media_id: favId, cookie_uid: uid } })
  let body = resp.data;
  if (body.code == 0) {
    if (body.data.code == 0) {
      let info = body.data.data;
      Object.assign(formData, {
        favId: info.id,
        favName: info.title,
        ownerUid: info.upper.mid,
        ownerName: info.upper.name,
        backupUserId: uid,
        videoBackupEnable: true,
        videoBackupConfigId: undefined,
      });
    } else {
      ElMessage.error(body.data.message);
    }
  } else {
    ElMessage.error(body.message);
  }
  if (backupConfigOptions.value == null) {
    resp = await axios.get("/api/backup/video-backup-configs");
    if (resp.data.code == 0) {
      backupConfigOptions.value = resp.data.data;
    }
  }
}

function submit(config) {
  console.log("配置信息", config);
  edtFavLoading.value = true;
  axios.post("/api/backup/fav", config)
    .then(resp => {
      edtFavLoading.value = false;
      if (resp.data.code == 0) {
        ElMessage.success("添加成功");
        favList.value.push(resp.data.data);
        edtCondfigDialogVisible.value = false;
      } else {
        ElMessage.error(resp.data.message);
      }
    });
}

function editFavConfig(config) {
  closeDrawer();
  edtCondfigDialogMode.value = true;
  edtCondfigDialogVisible.value = true;
  Object.assign(formData, config);
  if (!backupConfigOptions.value) {
    axios.get("/api/backup/video-backup-configs")
      .then(resp => {
        if (resp.data.code == 0) {
          backupConfigOptions.value = resp.data.data;
        }
      })
  }
}

function update(config) {
  edtFavLoading.value = true;
  axios.put("/api/backup/fav", config)
    .then(resp => {
      edtFavLoading.value = false;
      if (resp.data.code == 0) {
        ElMessage.success("修改成功");
        let data = resp.data.data
        for (let fav of favList.value) {
          if (data.favId == fav.favId) {
            Object.assign(fav, data);
            break;
          }
        }
        edtCondfigDialogVisible.value = false;
      } else {
        ElMessage.error(resp.data.message);
      }
    });
}

function askToDeleteFav(bak, index) {
  closeDrawer();
  confirmWithInput('警告', '确认删除收藏夹『' + bak.favName + '』吗？视频将一并删除（如果没有其他备份项引用）', '确认删除',
    () => { deleteFav(bak.favId, index); }
  );
}

function deleteFav(favId, index) {
  let source = new EventSource("/api/backup/delete-fav/" + favId)
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
          ElMessage.error("收藏夹未找到");
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
      favList.value.splice(index, 1)
      ElMessage.success("删除成功！");
    } else {
      operationalProgress.value = prog;
    }
  }
}


function openTransferBackupUserDialog(fav) {
  closeDrawer();
  Object.assign(formData, fav);
  transferDialogVisible.value = true;
}

function transferBackupUser(formData) {
  edtFavLoading.value = true;
  axios.put("/api/backup/fav", formData)
    .then(({ data }) => {
      edtFavLoading.value = false;
      transferDialogVisible.value = false;
      if (data.code == 0) {
        let fav = data.data;
        for (let i in favList.value) {
          if (favList.value[i].favId == fav.favId) {
            favList.value.splice(i, 1);
            break;
          }
        }
        ElMessage.success("移交成功");
      } else {
        ElMessage.error(data.message);
      }
    })
}


</script>

<style scoped>
.fav-list {
  /* display: flex;
    flex-direction: column; */
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  column-gap: 30px;
}

.fav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  height: 110px;
}

.fav-thumbnail {
  width: 120px;
  aspect-ratio: 16 / 10;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-thumbnail {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 5px;
  object-fit: cover;
  object-fit: cover;
  background-color: #c9ccd0;
  /* 确保图片位于顶部 */
}

.thumbnail-overlay {
  position: absolute;
  border-radius: 5px;
  /* 确保叠层效果显示 */
}

.t1 {
  bottom: 9px;
  left: 10%;
  width: 80%;
  height: 100%;
  background-color: #e3e5e7;
}

.t2 {
  bottom: 6px;
  left: 4%;
  width: 92%;
  height: 98%;
  background-color: #aeb3b9;
}

.fav-overlay {
  position: absolute;
  bottom: 5px;
  right: 5px;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 3px;
  padding: 2px 5px;
  color: #fff;
  font-size: 12px;
  z-index: 4;
}

.fav-meta {
  flex-grow: 1;
  margin-right: 10%;
}

.fav-meta p {
  margin: 5px 0px;
  color: #888;
}

.fav-meta h3 {
  margin: 10px 0px;
}

.fav-options {
  position: absolute;
  bottom: 15px;
  right: 10px;
  /* 使选项按钮靠右下角 */
  text-align: center;
}

.options-icon {
  font-size: 24px;
  width: 24px;
  height: 24px;
  color: #888;
  cursor: pointer;
  border-radius: 50%;
}

.add-new {
  /* background-color: #888; */
  border-radius: 5px;
  color: white;
  font-size: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog {
  width: 80%;
  max-width: 600px;
  min-width: 300px;
}

.sketch_content {
  overflow: auto;
  height: 434px;
  border-top: 1px solid #eff1f4;
  border-bottom: 1px solid #eff1f4;
  padding: 0px 30px 11px 27px;
}
</style>
