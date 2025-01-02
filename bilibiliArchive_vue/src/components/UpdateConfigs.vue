<template>
  <div v-for="(update, index) in configs" :key="index" class="box-card">
    <el-card :header="'更新配置 ' + (index + 1) + '（共计' + formatMinutesToDHMS(update.loopCount * update.interval) + '）'">
      <el-form-item label="循环次数">
        <el-input-number v-model="update.loopCount" :min="1" />
      </el-form-item>
      <el-form-item label="间隔时间 (分钟)">
        <el-input-number v-model="update.interval" :min="1" />
      </el-form-item>

      <el-form-item label="更新视频和弹幕">
        <el-switch v-model="update.updateVideoAndDanmaku" />
      </el-form-item>

      <!-- 评论1配置开关与内容 -->
      <comment-config class="box-card" v-model="update.comment1" title="评论更新"></comment-config>

      <!-- 评论2配置开关与内容 -->
      <comment-config v-if="update.comment1" class="box-card" v-model="update.comment2"
        :title="'评论二次更新（间隔' + formatMinutesToDHMS(update.interval * (update.comment2Spacing + 1)) + '）'">
        <el-form-item v-if="update.comment2" label="二次更新间隙">
          <el-input-number v-model="update.comment2Spacing" :min="0" />
        </el-form-item>
      </comment-config>

      <!-- 评论3配置开关与内容 -->
      <comment-config v-if="update.comment1" class="box-card" v-model="update.comment3"
        :title="'评论三次更新（间隔' + formatMinutesToDHMS(update.interval * (update.comment3Spacing + 1)) + '）'">
        <el-form-item v-if="update.comment3" label="三次更新间隙">
          <el-input-number v-model="update.comment3Spacing" :min="0" />
        </el-form-item>
      </comment-config>

      <el-button type="danger" @click="removeUpdate(index)">删除更新配置</el-button>

    </el-card>
  </div>
  <el-button class="add-new-update" type="info" @click="addUpdate" plain>+ 添加新的更新配置</el-button>
</template>

<script setup>
import CommentConfig from './CommentConfig.vue'
const configs = defineModel();

function formatMinutesToDHMS(minutes) {
  const days = Math.floor(minutes / 1440); // 1天 = 1440分钟
  const hours = Math.floor((minutes % 1440) / 60); // 1小时 = 60分钟
  const mins = minutes % 60;

  return `${days}天${hours}时${mins}分`;
}

const removeUpdate = (index) => {
  configs.value.splice(index, 1)
}

function addUpdate() {
  configs.value.push({
    loopCount: 1,
    interval: 1,
    updateVideoAndDanmaku: true,
    comment1: null,
    comment2: null,
    comment2Spacing: 0,
    comment3: null,
    comment3Spacing: 0
  })
}

</script>

<style scoped>
.add-new-update {
    display: block;
    width: 100%;
    height: 60px;
}
</style>