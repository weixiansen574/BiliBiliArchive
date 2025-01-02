<template>
    <div class="mdui-container">
        <div class="card header">
            <h2>添加新用户</h2>
        </div>
        <div class="card">
            <RouteTabs :links="links" />
            <div class="container">
                <RouterView></RouterView>
            </div>
        </div>
    </div>
</template>

<script setup>
import RouteTabs from "../components/RouterTabs.vue"
import { ref } from "vue"

const links = ref([
    { to: { name: 'add-user-from-cookie' }, name: '输入cookie' },
    { to: { name: 'add-user-from-qrcode' }, name: '扫码登录' }]
);
</script>

<style scoped>
/* .mdui-container {
    padding: 0;
  padding-bottom: 16px;
} */

.card {
    background-color: white;
    border-radius: 3px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    margin-top: 12px;
    margin-bottom: 12px;
}

.header {
    padding: 20px 10px;
}


h2 {
    margin: 0;
    font-weight: bold;
    color: #32325d;
}


.container {
    padding: 10px;
}
</style>