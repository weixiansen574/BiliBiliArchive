<template>
    <UploaderIcon class="first-instruct-icon" />{{ video.ownerName }}
</template>

<script setup>
import UploaderIcon from './icons/UploaderIcon.vue';
import {defineProps} from 'vue';
const props = defineProps({
    video:Object
})
</script>

<style scoped>
.first-instruct-icon {
    height: 100%;
    width: auto;
    aspect-ratio: 1/1;
    margin-right: 2px;
}
</style>