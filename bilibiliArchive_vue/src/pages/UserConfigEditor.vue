<template>
    <h3>Cookie</h3>
    <el-input class="cookie-input" v-model="cookieInput" :autosize="{ minRows: 3 }" type="textarea"
        placeholder="请输入Cookie" />
    <el-button type="primary" @click="update()" :loading="updating">更新</el-button>
    <el-button type="primary">扫码登录</el-button>
    <el-button type="primary" @click="clear()" plain>清空</el-button>
</template>

<script setup>

</script>