<template>
    <el-card :header="title ? title : '评论配置'">
        <el-form-item label="启用" >
            <el-switch v-model="isCommentEnabled" />
        </el-form-item>
        <template v-if="config">
            <el-form-item label="下载模式">
                <el-select v-model="config.mode" placeholder="选择评论模式">
                  <el-option label="所有评论" value="ALL"/>
                  <el-option label="热门评论" value="HOT"/>
                  <el-option label="增量更新" value="LATEST_FIRST"/>
                  <el-option v-if="showFloorSniffer" label="楼层嗅探" value="FLOOR_SNIFFER"/>
                </el-select>
            </el-form-item>
            <el-form-item v-if="config.mode === 'HOT'" label="根评论限制">
                <el-input-number v-model="config.rootLimit" :min="0" />
            </el-form-item>
            <el-form-item label="回复下载模式" v-if="config.mode == 'HOT'">
                <el-select v-model="config.replyMode" placeholder="选择评论回复模式">
                    <el-option label="下载全部" value="ALL" />
                    <el-option label="增量更新" value="INCREMENT" />
                    <el-option label="带限制的从旧到新" value="COUNT_LIMIT" />
                </el-select>
            </el-form-item>
            <el-form-item v-if="config.mode === 'HOT' && config.replyMode === 'COUNT_LIMIT'" label="回复评论限制">
                <el-input-number v-model="config.replyLimit" :min="0" />
            </el-form-item>
            <slot></slot>
        </template>
    </el-card>
</template>

<script setup>
import {ElCard, ElFormItem, ElSelect, ElOption, ElInputNumber, ElSwitch} from 'element-plus'
import {computed,ref} from 'vue'
const config = defineModel();
const props = defineProps({
    title:String
});

const showFloorSniffer = ref(localStorage.getItem('enable_hidden_feature') == 'true')

// 控制主评论开关状态
const isCommentEnabled = computed({
    get: () => config.value !== null,
    set: (value) => {
        config.value = value ? { mode: 'ALL',rootLimit: 200, replyMode: "ALL",replyLimit:50} : null
    },
})
</script>