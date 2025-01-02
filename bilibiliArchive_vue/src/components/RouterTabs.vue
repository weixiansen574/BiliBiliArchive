<template>
    <div class="tabs">
        <RouterLink v-for="item in links" :to="item.to" class="mdui-ripple">{{ item.name }}</RouterLink>
    </div>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps(["links"])
</script>

<style scoped>
.tabs{
    display: flex;
    border-bottom: solid 1px var(--el-menu-border-color);
}

a{
    text-decoration: none;
    padding: 12px;
    color: #303133;
    font-weight: 500;
    font-size: var(--el-font-size-base);
}

a:hover{
    color: var(--el-color-primary);
}

.router-link-active{
    color: var(--el-color-primary);
    border-bottom: solid 2px var(--el-color-primary);
}
</style>