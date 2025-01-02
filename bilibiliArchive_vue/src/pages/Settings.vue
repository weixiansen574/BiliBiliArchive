<template>
  <div class="mdui-container">
    <div class="card header">
      <h2>备份设置</h2>
    </div>
    <div class="card container" v-loading="loading">
      <el-form :model="form" label-width="150px" label-position="top">
        <!-- Task Pool Size -->
        <el-form-item label="任务池大小" prop="taskPoolSize">
          <el-input-number v-model="form.taskPoolSize" :min="1"></el-input-number>
        </el-form-item>

        <el-form-item label="备份循环间隔（分钟）" prop="intervalMinuteOfLoop">
          <el-input-number v-model="form.intervalMinuteOfLoop" :min="1"></el-input-number>
        </el-form-item>

        <!-- Public VIP User ID -->
        <el-form-item label="公共大会员账号UID" prop="publicVipUserId">
          <el-input v-model="form.publicVipUserId" placeholder="输入用户UID（可空）"></el-input>
        </el-form-item>

        <!-- Global UP Blacklist -->
        <el-form-item label="全局UP主黑名单（历史记录备份）">
          <UpBlackList v-model="form.globalUpBlackList"></UpBlackList>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="onSubmit" :loading="saving">保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import UpBlackList from '../components/UpBlackList.vue';

const loading = ref(true);
const saving = ref(false);

const form = reactive({});

axios.get("/api/backup/settings")
  .then(({ data }) => {
    if (data.code == 0) {
      loading.value = false;
      Object.assign(form, data.data);
    } else {
      ElMessage.error(data.message);
    }
  })

const onSubmit = () => {
  saving.value = true;
  console.log('Submitted Data:', form);
  axios.put("/api/backup/settings", form)
    .then(({ data }) => {
      saving.value = false;
      if (data.code == 0) {
        Object.assign(form, data.data);
        ElMessage.success("保存成功！");
      } else {
        ElMessage.error(data.message)
      }
    })
};

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
</style>