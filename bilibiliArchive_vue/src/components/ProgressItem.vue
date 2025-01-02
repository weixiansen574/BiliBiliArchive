<template>
    <div class="status-card" >
        <div class="notify-main">
            <h3>{{ prog.title }}</h3>
            <div>{{ prog.content }}</div>
            <div class="mdui-progress" v-if="prog.data != null || prog.type != 2">
                <div v-if="prog.data" class="mdui-progress-determinate"
                    :style="'width: ' + (prog.data.progress / prog.data.max * 100) + '%;'"></div>
                <div v-else class="mdui-progress-indeterminate"></div>
            </div>
        </div>
        <div class="notify-buttons" v-if="prog.type == 0 && prog.data">
            <button class="mdui-ripple notify-button" @click="jumpOverBackupSleep()">跳过倒计时</button>
        </div>
    </div>
</template>

<script setup>
import { defineProps } from 'vue';
import axios from 'axios';

const props = defineProps(['prog']);

function jumpOverBackupSleep() {
    axios.get("/api/backup/state/jump-over-backup-sleep")
        .then(resp => {
            if (!resp.data.data) {
                ElMessage.error("跳过失败，备份器没有在运行")
            }
        })
}


</script>

<style scoped>
.status-card {
  border-radius: 3px;
  padding: 8px;
  /* background-color: #F7EBEC; */
  box-shadow: 0 0 4px 0 rgba(0, 0, 0, 0.20);
  margin-bottom: 10px;
}

.status-card h3 {
  margin-top: 5px;
  margin-bottom: 10px;
}

.mdui-progress {
  margin-top: 10px;
  margin-bottom: 5px;
}

.notify-main {
  padding-inline: 6px;
}

.notify-buttons button {
  border: 0;
  padding: 6px;
  font-size: 16px;
  background: transparent;
  color: #4B5E9D;
}
</style>