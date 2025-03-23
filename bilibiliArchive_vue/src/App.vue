<template>
  <div class="mdui-appbar mdui-appbar-fixed">
    <div class="mdui-toolbar  mdui-color-pink-accent">
      <a href="javascript:" class="mdui-btn mdui-btn-icon" mdui-drawer="{target: '#drawer'}">
        <i class="mdui-icon material-icons">menu</i>
      </a>
      <a href="javascript:;" class="mdui-typo-headline mdui-hidden-xs">哔哩存档姬</a>
      <a href="javascript:;" class="mdui-typo-title">{{$route.meta.title}}</a>
      <div class="mdui-toolbar-spacer"></div>
    </div>
  </div>
  <Drawer></Drawer>
  <router-view :key="$route.params.uid"></router-view>
</template>

<script setup>
import Drawer from "./components/Drawer.vue"
</script>
<style>
body{
  background-color: #f4f4f4;
}
</style>
