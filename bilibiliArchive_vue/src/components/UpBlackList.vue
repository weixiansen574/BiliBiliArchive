<template>
    <el-scrollbar max-height="210px" style="width: 100%;">
        <el-table :data="blackList">
            <el-table-column prop="uid" label="用户ID" />
            <el-table-column prop="name" label="名称" />
            <el-table-column label="操作" width="80px">
                <template #default="scope">
                    <el-button type="danger" size="small" @click="removeUploader(scope.$index)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </el-scrollbar>
    <el-row :gutter="10" style="margin-top: 10px;">
        <el-col :span="8">
            <el-input v-model="newUploader.uid" size="small" placeholder="输入UP主UID" />
        </el-col>
        <el-col :span="8">
            <el-input v-model="newUploader.name" size="small" placeholder="输入UP主名称" />
        </el-col>
        <el-col :span="8">
            <el-button type="primary" size="small" @click="addUploader">
                添加UP主
            </el-button>
        </el-col>
    </el-row>
</template>

<script setup>
import { reactive } from 'vue';
const blackList = defineModel();
const newUploader = reactive({
  uid: '',
  name: '',
});

const addUploader = () => {
  if (!newUploader.uid || !newUploader.name) {
    alert('请填写UP主UID和名称');
    return;
  }
  blackList.value.push({ uid: newUploader.uid, name: newUploader.name });
  newUploader.uid = '';
  newUploader.name = '';
};

const removeUploader = (index) => {
    blackList.value.splice(index, 1);
};

</script>