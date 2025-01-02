<template>
    <div class="mdui-container">
        <div class="card header">
            <h2>视频备份配置管理</h2>
        </div>
        <div class="card">
            <div class="config">
                <RouterView></RouterView>
            </div>
        </div>
    </div>
</template>

<script setup>

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


.config{
    padding: 10px;
}
</style>