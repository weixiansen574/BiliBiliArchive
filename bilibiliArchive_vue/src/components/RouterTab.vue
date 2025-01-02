<template>
    <div class="mdui-container">
        <router-link :to="{ name: 'sub' }" class="mdui-ripple">收藏夹</router-link>
        <router-link :to="{ name: 'sub1' }" class="mdui-ripple">历史记录</router-link>
    </div>
</template>

<style scoped>
a {
    padding: 5px;
    color: black;
    text-decoration: none;
}


.router-link-active {
    color: 409eff;
    border-bottom: #409eff solid 2px;
}
</style>