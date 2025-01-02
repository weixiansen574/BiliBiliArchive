<template>
    <el-table v-loading="loading" :data="configs" style="width: 100%">
        <el-table-column label="优先级" width="80">
            <template #default="{ $index }">
                {{ $index + 1 }}
            </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作选项" align="center">
            <template #default="{ row, $index }">
                <div class="action-grid">
                    <el-button size="small" @click="moveUp($index)" :disabled="$index === 0" type="primary"
                        :icon="ArrowUp">上移</el-button>
                    <el-button size="small" @click="moveDown($index)" :disabled="$index === configs.length - 1"
                        type="primary" :icon="ArrowDown">下移</el-button>
                    <el-button size="small" @click="editConfig(row.id)" type="success" :icon="Edit">编辑</el-button>
                    <el-button size="small" @click="deleteConfig(row, $index)" type="danger" :icon="Delete">删除</el-button>
                </div>
            </template>
        </el-table-column>
    </el-table>
    <div class="actions">
        <el-button class="action" type="primary" @click="savePriority()">保存优先级顺序</el-button>
        <el-button class="action" type="success" @click="dialogAddVisible = true; closeDrawer()">新建配置</el-button>
    </div>
    <el-dialog v-model="dialogAddVisible" title="输入配置名称" style="min-width: 320px;width: 30%;" align-center>
        <el-input v-model="newConfigName" autocomplete="off" />
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="dialogAddVisible = false; newConfigName = ''">取消</el-button>
                <el-button type="primary" @click="createNew()" :loading="creating" style="margin-left: 12px;">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown, Edit, Delete } from '@element-plus/icons-vue'
import { closeDrawer } from '../util';
import axios from 'axios';

const loading = ref(true);

const router = useRouter();

const configs = ref([]);

const dialogAddVisible = ref(false);

const newConfigName = ref("");

const creating = ref(false);

const moveUp = (index) => {
    if (index > 0) {
        [configs.value[index - 1], configs.value[index]] = [configs.value[index], configs.value[index - 1]]
    }
}

const moveDown = (index) => {
    if (index < configs.value.length - 1) {
        [configs.value[index], configs.value[index + 1]] = [configs.value[index + 1], configs.value[index]]
    }
}

const editConfig = (id) => {
    // 在这里实现编辑逻辑，例如弹出编辑对话框
    router.push({ name: "video-backup-config", params: { id } })
}

const deleteConfig = (config, index) => {
    closeDrawer();
    ElMessageBox.confirm(//用此配置下载的视频，将固定优先级为FINAL，不会被任何配置覆盖
        '此操作将永久删除该配置项，是否继续？删除前需要将依赖此配置的备份项调整为其他配置，否则无法删除！使用此配置下载的视频会调整优先级为FINAL，以后将不会被任何配置覆盖。',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
        }
    ).then(() => {
        axios.delete("/api/backup/video-backup-configs/" + config.id)
            .then(({ data }) => {
                if (data.code == 0) {
                    configs.value.splice(index, 1)
                    ElMessage.success('删除成功!')
                } else if(data.code == 2004) {
                    ElMessageBox.alert(generateDetailedMessage(data.data),"删除失败")
                } else {
                    ElMessage.error(data.message)
                }
            })

    }).catch(() => {
        ElMessage({
            type: 'info',
            message: '已取消删除',
        })
    })
}

function savePriority() {
    let ids = new Array();
    for (let config of configs.value) {
        ids.push(config.id)
    }
    console.log(ids);
    axios.put("/api/backup/settings/video-backup-priority-list", ids)
        .then(response => {
            if (response.data.code == 0) {
                ElMessage({ type: "success", message: "保存成功" })
            } else {
                ElMessage({ type: "error", message: response.data.message })
            }
        })

}

function createNew() {
    creating.value = true;
    let formData = new FormData();
    formData.append("name", newConfigName.value);
    axios.post("/api/backup/video-backup-configs", formData)
        .then(resp => {
            let data = resp.data;
            if (data.code == 0) {
                ElMessage.success("添加成功");
                dialogAddVisible.value = false;
                configs.value.push(data.data);
            } else {
                ElMessage.error(data.message)
            }
            creating.value = false;
        })
}

axios.get("/api/backup/video-backup-configs")
    .then(response => {
        configs.value = response.data.data;
        loading.value = false;
    })

const generateDetailedMessage = (data) => {
    const { backupFavList, backupHistoryList, backupUploaderList } = data;

    const favMessages = backupFavList.map(
        fav => `${fav.ownerName}的收藏夹『${fav.favName}』`
    );

    const historyMessages = backupHistoryList.map(
        history => `用户UID『${history.uid}』的历史记录`
    );

    const uploaderMessages = backupUploaderList.map(
        uploader => `UP主备份『${uploader.name}』`
    );

    const allMessages = [...favMessages, ...historyMessages, ...uploaderMessages];

    return `视频备份配置被引用，无法删除，请先将引用此配置的备份项改为其他备份配置：\n${allMessages.join("；\n")}`;
};
</script>

<style scoped>
.action {
    margin-top: 10px;
    margin-right: 10px;
    margin-left: 0;
}

.action-grid>.el-button {
    margin-left: 0;
}

.action-grid {
    display: inline-grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    column-gap: 10px;
    row-gap: 4px;
}

@media only screen and (max-width: 800px) {

    /* 避免手机上第一个按钮偏左的问题 */
    .action-grid {
        column-gap: 6px;
    }
}

@media only screen and (max-width: 768px) {

    /* 避免手机上第一个按钮偏左的问题 */
    .action-grid {
        grid-template-columns: 1fr 1fr;
    }
}

@media only screen and (max-width: 500px) {

    /* 避免手机上第一个按钮偏左的问题 */
    .action-grid {
        grid-template-columns: 1fr;
    }
}
</style>