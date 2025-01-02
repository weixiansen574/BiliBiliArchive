<template>
    <el-dialog :model-value="prog != null" :title="prog ? prog.title : ''" :show-close="false"
        :close-on-press-escape="false" :close-on-click-modal="false" style="min-width: 300px;width: 85%; max-width: 600px;" align-center>
        <div v-if="prog">{{ prog.content }}</div>
        <div class="mdui-progress" v-if="prog">
            <div v-if="prog.data" class="mdui-progress-determinate"
                :style="'width: ' + (prog.data.progress / prog.data.max * 100) + '%;'"></div>
            <div v-else class="mdui-progress-indeterminate"></div>
        </div>
    </el-dialog>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps(['prog']);

</script>

<style scoped>
.mdui-progress {
  margin-top: 10px;
  margin-bottom: 5px;
}
</style>