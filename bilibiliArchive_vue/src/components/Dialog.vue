<template>
    <div id="largeDialog" class="dialog">
        <div class="dialog-content">
            <div class="dialog-title">
                <h2>{{ title }}</h2>
                <span class="close-btn" @click="closeDialog()">&times;</span>
            </div>
            <slot></slot>
        </div>
    </div>
</template>
<script setup>
import {defineOptions,defineEmits,watch} from 'vue';

const props = defineOptions({
    title:String,
    open:Boolean
})

const emit = defineEmits(["close"])

function closeDialog() {
    document.body.style.overflow = ""; // 恢复滚动
    emit()
}

const open = props.open;

watch(open,newValue => {
    console.log(newValue);
})
</script>
<style scoped></style>