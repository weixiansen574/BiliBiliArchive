<template>
  <a :title="video.title" :href="'/video/' + video.bvid" target="_blank" class="video-card mdui-ripple">
    <div class="video-thumbnail-wrapper">
      <img :src="getCoverUrl(video.bvid)" :alt="video.title" class="video-thumbnail" />
      <div class="float-info">
        <span class="duration">{{ formatDuration(video.duration) }}</span>
      </div>
    </div>

    <div class="video-info">
      <div class="video-title" v-bind:class="getStateClass(video.state)">{{ video.title }}</div>
      <div>
        <div class="meta">
          <slot name="meta1"></slot>
        </div>
        <div class="meta">
          <slot name="meta2"></slot>
        </div>
        <div class="meta">
          <slot name="meta3"></slot>
        </div>
      </div>
    </div>

    <el-dropdown class="dropdown-container" trigger="click" v-if="actions != null && actions.length > 0">
      <span class="dropdown-trigger mdui-ripple" @click.prevent="">⋮</span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="act in actions" @click="act.onClick(video)">{{ act.title }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <!-- 右下角的 ellipsis 按钮 -->
    <!-- <div class="dropdown-container">
      <el-dropdown trigger="click">
        <span class="dropdown-trigger">
          ⋮
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>选项1</el-dropdown-item>
          <el-dropdown-item>选项2</el-dropdown-item>
          <el-dropdown-item>选项3</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div> -->
  </a>
</template>

<script setup>
import { defineProps } from 'vue';
import { ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus';

const props = defineProps({
  video: { type: Object, required: true },
  actions: { type: Array, required: false }
});

function getCoverUrl(bvid) {
  return `/files/archives/videos/${bvid}/cover`;
}

function getStateClass(state) {
  if (state == "FAILED_AND_NO_BACKUP") {
    return "no-backup";
  }
  if (state == "FAILED_UP_DELETE") {
    return "hexie up-deleted";
  }
  if (state == "SHADOW_BAN") {
    return "shadow-ban";
  }
  if (state == "PRIVATE") {
    return "private";
  }
  if (state == "SEARCH_BAN"){
    return "search_ban";
  }
  if (state != "NORMAL") {
    return "hexie";
  }
}

function formatDuration(seconds) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  const formattedMinutes = minutes < 10 ? '0' + minutes : minutes;
  const formattedSecs = secs < 10 ? '0' + secs : secs;

  return hours > 0
    ? `${hours}:${formattedMinutes}:${formattedSecs}`
    : `${formattedMinutes}:${formattedSecs}`;
}
</script>

<style scoped>
.video-card {
  display: flex;
  height: 84px;
}

.video-thumbnail-wrapper {
  aspect-ratio: 16 / 10;
  height: 100%;
  position: relative;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  background-color: #C9CCD0;
  object-fit: cover;
  position: absolute;
}

.float-info {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  position: absolute;
  overflow: hidden;
}

.duration {
  position: absolute;
  bottom: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 3px 0 0 0;
  padding: 2px 5px;
  color: #fff;
  font-size: 12px;
}

.video-info {
  margin-left: 8px;
  display: flex;
  height: 100%;
  align-content: space-around;
  flex-direction: column;
  justify-content: space-between;
}

.video-title {
  font-weight: 400;
  color: black;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  font-size: 14px;
  line-height: 1.2em;
  max-height: 2.4em;
  text-decoration: none;
}

.meta {
  display: flex;
  align-items: center;
  height: 16px;
  font-size: 12px;
  color: gray;
}

.hexie {
  text-decoration: line-through;
  color: #BDB76B;
}

.up-deleted {
  font-style: italic;
}

.no-backup {
  text-decoration: line-through;
  color: #999;
}

.shadow-ban {
  text-decoration: line-through;
  color: #FF669A;
}

.private{
  color: red;
}

.search_ban{
  color: purple;
}

a {
  text-decoration: none;
}

/* 添加右下角的 dropdown */
.dropdown-container {
  position: absolute;
  bottom: 0px;
  right: 0px;
}

.dropdown-trigger {
  font-size: 18px;
  cursor: pointer;
  color: #888;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
}

@media only screen and (min-width:1024px) {
  .video-card {
    display: flex;
    height: 120px;
  }

  .meta {
    height: 18px;
    font-size: 14px;
  }

  .video-title {
    font-size: 16px;
  }

  .dropdown-trigger {
    font-size: 22px;
    width: 26px;
    height: 26px;
  }
}

/* .options-icon {
  font-size: 24px;
  width: 24px;
  height: 24px;
  color: #888;
  cursor: pointer;
  border-radius: 50%;
  text-align: center;
} */
</style>
