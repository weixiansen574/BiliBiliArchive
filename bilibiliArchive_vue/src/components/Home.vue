<template>
  <div class="mdui-container">

    <div class="status-section card">
      <svg v-if="serverStatus.running" t="1724251734929" class="status-icon" viewBox="0 0 1024 1024" version="1.1"
        xmlns="http://www.w3.org/2000/svg" p-id="9237" width="64" height="64">
        <path
          d="M886.784 512a25.6 25.6 0 0 1-11.4944 21.3504l-698.368 460.8a25.6512 25.6512 0 0 1-26.24 1.2032A25.6256 25.6256 0 0 1 137.216 972.8V51.2a25.5744 25.5744 0 0 1 39.7056-21.3504l698.368 460.8a25.6 25.6 0 0 1 11.4944 21.3504z"
          p-id="9238" fill="currentColor">
        </path>
      </svg>
      <svg v-else t="1724327103115" class="status-icon" viewBox="0 0 1024 1024" version="1.1"
        xmlns="http://www.w3.org/2000/svg" p-id="4220" width="64" height="64">
        <path
          d="M258.875 174.5h506.25c46.58203125 0 84.375 37.79296875 84.375 84.375v506.25c0 46.58203125-37.79296875 84.375-84.375 84.375H258.875c-46.58203125 0-84.375-37.79296875-84.375-84.375V258.875c0-46.58203125 37.79296875-84.375 84.375-84.375z"
          fill="currentColor" p-id="4221">
        </path>
      </svg>
      <div class="status">
        <h3>{{ serverStatus.running ? "运行中" : "未运行" }}</h3>
        <h4>{{ runningDesc }}</h4>
      </div>
      <el-button type="primary" :loading="isChangeState" @click="changeState()">
        {{ serverStatus.running ? "停止运行" : "启动备份" }}
      </el-button>
    </div>
    <el-tabs class="stats-section card">
      <el-tab-pane label="运行状态">
        <ProgressItem v-for="(prog) in progs" :key="prog.id" :prog="prog"></ProgressItem>
        <!-- <div class="status-card" v-for="(msg, index) in progs" :key="msg.id">
          <div class="notify-main">
            <h3>{{ msg.title }}</h3>
            <div>{{ msg.content }}</div>
            <div class="mdui-progress" v-if="msg.type != 2">
              <div v-if="msg.data" class="mdui-progress-determinate"
                :style="'width: ' + (msg.data.progress / msg.data.max * 100) + '%;'"></div>
              <div v-else class="mdui-progress-indeterminate"></div>
            </div>
          </div>
          <div class="notify-buttons" v-if="msg.type == 0 && msg.data">
            <button class="mdui-ripple notify-button" @click="jumpOverBackupSleep()">跳过倒计时</button>
          </div>
        </div> -->
      </el-tab-pane>
      <el-tab-pane :label="'异常记录 ' + exceptions.length">
        <div v-for="ex in exceptions" :key="ex.id" class="exception-card">
          <div class="exception-header"><span>{{ formatTimestamp(ex.timestamp) }}</span><button
              @click="deleteException(ex.id)">已阅</button>
          </div>
          <div class="exception-message">{{ ex.message }}</div>
          <div class="exception-content">{{ ex.exceptionString }}</div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue';
import axios from "axios";
import { ElMessage } from 'element-plus';
import { formatTimestamp } from '../util'
import ProgressItem from './ProgressItem.vue';

const serverStatus = reactive({
  running: false,
  startDate: 0
})

const runningDesc = ref("备份姬未运行");
const isChangeState = ref(true);

const exceptions = reactive([])

var runTimeTimer = null;

const progs = reactive([])

const source = new EventSource("/api/operational/progress");

source.onmessage = function (event) {
  console.log(event.data);
  let msg = JSON.parse(event.data);
  if (msg.type == -1) {
    for (let i = progs.length - 1; i >= 0; i--) { // 从末尾遍历，避免索引混乱
      if (progs[i].id == msg.id) {
        progs.splice(i, 1); // 移除当前元素
      }
    }
    console.log(progs)
  } else {
    const result = progs.find(item => item.id === msg.id);
    if (result) {
      Object.assign(result, msg);
    } else {
      progs.push(msg);
    }
  }
};

source.onerror = function () {
  console.error("Connection error");
  source.close();
}

const exSource = new EventSource("/api/operational/exceptions");
exSource.onmessage = event => {
  console.log(event.data);
  let msg = JSON.parse(event.data);
  exceptions.unshift(msg);
}

exSource.onerror = () => {
  console.error("Connection error");
  exSource.close();
}

axios.get("/api/backup/state/run-at")
  .then(({ data }) => {
    if (data.code == 0) {
      isChangeState.value = false;
      if (data.data) {
        serverStatus.running = true;
        serverStatus.startDate = data.data;
        startOrStopTimer();
      }
    } else {
      ElMessage.error(data.code)
    }
  }).catch(e => {
    console.log(e);
    ElMessage.error("连接后端失败")
  })

function changeState() {
  if (isChangeState.value) {
    return;
  }
  isChangeState.value = true;

  if (serverStatus.running) {
    axios.get("/api/backup/state/stop")
      .then(resp => {
        isChangeState.value = false;
        if (resp.data.code == 0) {
          serverStatus.running = false;
          serverStatus.startDate = 0;
          startOrStopTimer();
        } else {
          ElMessage.error(resp.data.message)
        }
      })
  } else {
    axios.get("/api/backup/state/start")
      .then(resp => {
        isChangeState.value = false;
        if (resp.data.code == 0) {
          serverStatus.running = true;
          serverStatus.startDate = resp.data.data;
          startOrStopTimer();
        } else {
          ElMessage.error(resp.data.message)
        }
      })
  }
}

function startOrStopTimer() {
  if (serverStatus.running) {
    runningDesc.value = formatRunningTime(serverStatus.startDate);
    runTimeTimer = setInterval(() => {
      runningDesc.value = formatRunningTime(serverStatus.startDate);
    }, 1000);
  } else {
    clearInterval(runTimeTimer);//先清理掉定时器
    runningDesc.value = "备份姬未运行";
  }
}

function formatRunningTime(serverStartTime) {
  const now = Date.now();
  const elapsedTime = now - serverStartTime; // 计算运行的毫秒数

  const days = Math.floor(elapsedTime / (1000 * 60 * 60 * 24)); // 计算天数
  const hours = Math.floor((elapsedTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)); // 计算小时数
  const minutes = Math.floor((elapsedTime % (1000 * 60 * 60)) / (1000 * 60)); // 计算分钟数
  const seconds = Math.floor((elapsedTime % (1000 * 60)) / 1000); // 计算秒数

  // 格式化小时、分钟和秒数为两位数
  const formattedHours = String(hours).padStart(2, '0');
  const formattedMinutes = String(minutes).padStart(2, '0');
  const formattedSeconds = String(seconds).padStart(2, '0');

  return `已运行${days}天 ${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
}



// axios.get("/api/operational/exceptions")
//   .then(response => {
//     Object.assign(exceptions, response.data.data);
//     console.log(response.data)
//   });

function deleteException(id) {
  axios.delete("/api/operational/exceptions/" + id)
    .then(({ data }) => {
      if (data.code == 0) {
        const index = exceptions.findIndex(exception => exception.id === id);
        if (index !== -1) {
          exceptions.splice(index, 1);
        }
        ElMessage.success("异常信息已移除")
      } else {
        ElMessage.error(data.message)
      }
    })
}



onUnmounted(() => {
  source.close();
});


</script>

<style scoped>
.card {
  background-color: white;
  border-radius: 3px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin-top: 12px;
}

.status-section {
  padding-block: 20px;
  padding-right: 20px;
  padding-left: 5px;
  width: auto;
  display: flex;
  align-items: center;
}

.status-icon {
  color: var(--el-color-primary);
  height: 40px;
  width: 40px;
  margin: 15px 25px;
}

.stats-section {
  padding-bottom: 10px;
  width: auto;
  margin-bottom: 10px;
  min-height: calc(100vh - 220px);
}

.el-tab-pane {
  padding-top: 4px;
}

.el-tabs>* {
  padding-inline: 16px;
}


.status {
  flex: 1;
}

.status h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.status h4 {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
}

h2 {
  border-bottom: 1px solid #EEE;
  padding-bottom: 15px;
  padding-top: 18px;
  padding-left: 18px;
  margin: 0;
  font-weight: 400;
  font-size: 16px;
  color: #666;
}

.stats-content {
  height: 500px;
  padding: 10px;
  font-size: 32px;
}

.icon {
  width: 24px;
  height: 24px;
  vertical-align: middle;
  margin-right: 10px;
}

p {
  display: flex;
  align-items: center;
}

.log-area {
  height: 200px;
  overflow-y: auto;
  background: #e9ecef;
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.user-avatar {
  border-radius: 50%;
}

.exception-card {
  border-radius: 2px;
  padding: 8px;
  background-color: #2B2B2B;
  margin-bottom: 10px;
}

.exception-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  font-size: 18px;
  color: #BBBBBB;
  padding-bottom: 4px;
  margin-bottom: 6px;
  border-bottom: #BBBBBB solid 1px;
}

.exception-header button {
  font-size: 16px;
  color: #BBBBBB;
  background-color: #2B2B2B;
}

.exception-message {
  color: #BBBBBB;
}

.exception-content {
  margin: 0;
  padding-top: 6px;
  color: #FF6B68;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: keep-all;
  max-height: 300px;
  /* 设置最大高度 */
  overflow-y: auto;
  /* 超出时启用垂直滚动条 */
}
</style>