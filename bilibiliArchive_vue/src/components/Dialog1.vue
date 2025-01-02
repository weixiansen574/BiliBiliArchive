<template>
    <div v-if="isVisible" class="dialog-overlay">
        <div class="dialog">
            <slot></slot> <!-- 插槽用于动态插入内容 -->
            <button @click="close">关闭</button>
        </div>
    </div>
</template>
  
<script>
export default {
    data() {
        return {
            isVisible: true // 控制 Dialog 显示与隐藏
        };
    },
    methods: {
        close() {
            this.isVisible = false;
            this.$emit('close'); // 触发关闭事件
        }
    }
};
</script>
  
<style scoped>
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
}

.dialog {
    background: white;
    padding: 20px;
    border-radius: 5px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>
  