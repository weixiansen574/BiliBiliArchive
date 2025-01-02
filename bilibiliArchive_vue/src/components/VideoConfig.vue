<template>
  <el-card class="box-card" :header="title ? title : '视频下载'">
    <el-form-item label="最高清晰度">
      <el-select v-model="config.clarity" placeholder="选择清晰度">
        <el-option label="240P" :value="6" />
        <el-option label="360P" :value="16" />
        <el-option label="480P" :value="32" />
        <el-option label="720P" :value="64" />
        <el-option label="720P 60帧" :value="74" />
        <el-option label="1080P" :value="80" />
        <el-option label="1080P 高码率（大会员）" :value="112" />
        <el-option label="1080P 60帧（大会员）" :value="116" />
        <el-option label="4K（大会员）" :value="120" />
        <el-option label="HDR（大会员）" :value="125" />
        <el-option label="杜比视界（大会员）" :value="126" />
        <el-option label="8K（大会员）" :value="127" />
      </el-select>
    </el-form-item>

    <el-form-item label="视频编码ID">
      <el-select v-model="config.codecId" placeholder="选择编码">
        <el-option label="H264" :value="7" />
        <el-option label="H265" :value="12" />
        <el-option label="AV01" :value="13" />
      </el-select>
    </el-form-item>

  </el-card>
</template>
<script setup>
const config = defineModel();
const props = defineProps(["title"]);
</script>