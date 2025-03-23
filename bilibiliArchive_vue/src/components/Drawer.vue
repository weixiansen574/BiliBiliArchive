<template>
  <div class="mdui-drawer mdui-drawer-close" id="drawer">
    <div class="drawer-header">
      <img src="/logo.png" class="logo">
      <div class="app-name">哔哩存档姬</div>
    </div>
    <img src="/logo.png" class="logo-pc">
    <ul class="mdui-list">
      <RouterLink to="/home">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">home</i>
          <div class="mdui-list-item-content">主页</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'update-plans' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">&#xe8df;</i>
          <div class="mdui-list-item-content">更新任务</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'settings' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">settings</i>
          <div class="mdui-list-item-content">备份设置</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'video-backup-configs' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">list</i>
          <div class="mdui-list-item-content">视频备份配置管理</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'emote-manger' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">&#xe24e;</i>
          <div class="mdui-list-item-content">评论表情管理</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'video-search' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">&#xe8b6;</i>
          <div class="mdui-list-item-content">视频搜索</div>
        </li>
      </RouterLink>
      <li class="mdui-subheader">用户列表</li>
      <RouterLink v-for="user in userStore.userList" :key="user.uid" :to="{ name: 'user', params: { uid: user.uid } }">
        <li class="mdui-list-item mdui-ripple">
          <img :src="getUserAvatarUrl(user.avatarUrl)" class="mdui-list-item-icon user-avatar">
          <div class="mdui-list-item-content">{{ user.name }}</div>
        </li>
      </RouterLink>
      <RouterLink :to="{ name: 'add-user' }">
        <li class="mdui-list-item mdui-ripple">
          <i class="mdui-list-item-icon mdui-icon material-icons">person_add</i>
          <div class="mdui-list-item-content">添加用户</div>
        </li>
      </RouterLink>
    </ul>
  </div>
</template>

<script setup>
import { getFileName } from "../util";
import { useUserStore } from "../stores/userStore";

const userStore = useUserStore();

userStore.getOrAsyncLoad();


function getUserAvatarUrl(url) {
  return `/files/backup-config-avatars/user/${getFileName(url)}`;
}


</script>

<style>
#drawer {
  background-color: white;
}

.user-avatar {
  border-radius: 50%;
}

/* 顶部 LOGO 容器 */
.drawer-header {
  background-color: #FF4081;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 10px;
  position: relative;
}

/* 移动端 LOGO */
.logo {
  width: 100px;
  height: 100px;
  border-radius: 10px;
}

/* 应用名称 */
.app-name {
  font-weight: 400;
  font-size: 18px;
  color: white;
  margin-top: 10px;
}

.logo-pc {
  width: 100%;
  display: none;
}

@media only screen and (min-width: 1024px){
  .drawer-header{
    display: none;
  }
  .logo-pc {
    display: block;
  }
}

</style>

