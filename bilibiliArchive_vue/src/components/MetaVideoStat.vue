<template>
  <ViewsIcon class="first-instruct-icon" />{{ formatToQW(video.view) }}
  <DanmakusIcon class="instruct-icon" />{{ formatToQW(video.danmaku) }}
  <LikesIcon class="instruct-icon" />{{ formatToQW(video.like) }}
</template>

<script setup>
import ViewsIcon from "./icons/ViewsIcon.vue";
import DanmakusIcon from "./icons/DanmakusIcon.vue"
import LikesIcon from "./icons/LikesIcon.vue";
import { defineProps } from "vue";
import { formatToQW } from "../util"

const props = defineProps({
  video: Object
})


</script>

<style scoped>
.instruct-icon {
  height: 100%;
  width: auto;
  aspect-ratio: 1/1;
  margin-inline: 2px;
}

.first-instruct-icon {
  height: 100%;
  width: auto;
  aspect-ratio: 1/1;
  margin-right: 2px;
}
</style>