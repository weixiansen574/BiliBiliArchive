<template>
    <!-- <div>{{ config }}</div> -->
    <el-skeleton v-if="!config" :rows="5" animated />
    <el-form v-else :model="config" label-width="auto" :label-position="labelPosition">
        <!-- 视频配置 -->
        <el-card class="box-card" header="配置信息">
            <el-form-item label="配置文件名">
                <el-input v-model="config.name" style="width: 240px" />
            </el-form-item>
        </el-card>

        <VideoConfig v-model="config.video"/>

        <!-- 评论配置 -->
        <comment-config class="box-card" title="评论下载" v-model="config.comment"></comment-config>

        <!-- 更新配置 -->
        <div v-for="(update, index) in config.update" :key="index" class="box-card">
            <el-card
                :header="'更新配置 ' + (index + 1) + '（共计' + formatMinutesToDHMS(update.loopCount * update.interval) + '）'">
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

        <pre v-if="jsonPreviewVisible" class="json-preview">{{ JSON.stringify(config,null, 2) }}</pre>
        <div class="button-group">
            <el-button @click="save()" type="primary">保存</el-button>
            <el-button @click="jsonPreviewVisible = !jsonPreviewVisible" type="success">{{ jsonPreviewVisible ? "隐藏JSON预览" : "预览JSON"}}</el-button>
            <el-button @click="giveUp()">返回</el-button>
        </div>

    </el-form>
</template>

<script setup>
import { ref, computed, reactive, defineProps,onMounted, onBeforeUnmount} from 'vue'
import { ElForm, ElCard, ElFormItem, ElSelect, ElOption, ElInputNumber, ElSwitch, ElButton, ElMessage } from 'element-plus'
import CommentConfig from '../components/CommentConfig.vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import VideoConfig from "../components/VideoConfig.vue"

const router = useRouter();
const props = defineProps(["id"]);

const id = props.id;
const jsonPreviewVisible = ref(false)

const windowWidth = ref(window.innerWidth);

// 响应式计算 label-position
const labelPosition = computed(() => (windowWidth.value > 600 ? 'right' : 'top'));

// 监听窗口变化
const updateWindowWidth = () => {
    windowWidth.value = window.innerWidth;
};

onMounted(() => {
    window.addEventListener('resize', updateWindowWidth);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateWindowWidth);
});

const config = ref(null);

const addUpdate = () => {
    config.value.update.push({
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

const removeUpdate = (index) => {
    config.value.update.splice(index, 1)
}

function giveUp() {
    router.back();
}

function save() {
    axios.put("/api/backup/video-backup-configs", config.value)
        .then(response => {
            let data = response.data;
            if (data.code == 0) {
                ElMessage({
                    type: "success",
                    message: "修改成功"
                })
            } else {
                ElMessage.error(data.message)
            }
        })
        .catch(error => {
            console.log(error)
        })
    console.log(config.value)
}

axios.get("/api/backup/video-backup-configs/" + id)
    .then(response => {
        let data = response.data;
        if (data.code == 0) {
            config.value = data.data;
        } else {
            ElMessage({
                type: "error",
                message: data.message
            })
        }
        console.log(response.data)
    })

function formatMinutesToDHMS(minutes) {
    const days = Math.floor(minutes / 1440); // 1天 = 1440分钟
    const hours = Math.floor((minutes % 1440) / 60); // 1小时 = 60分钟
    const mins = minutes % 60;

    return `${days}天${hours}时${mins}分`;
}


</script>

<style scoped>
.box-card {
    margin-bottom: 20px;
}

.add-new-update {
    display: block;
    width: 100%;
    height: 60px;
}

.button-group {
    margin-top: 10px;
}

.json-preview{
    border-radius: 4px;
    background-color: #eee;
    padding: 4px
}
</style>