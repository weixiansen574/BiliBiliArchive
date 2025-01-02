<template>
    <div class="code-container">
      <pre><code ref="codeBlock" :class="language">{{ code }}</code></pre>
    </div>
  </template>
  
  <script setup>
  import { onMounted, onUpdated, ref } from 'vue';
  import hljs from 'highlight.js';
  
  const props = defineProps({
    code: {
      type: String,
      required: true
    },
    language: {
      type: String,
      default: '' // 可以设置为 'javascript', 'python' 等
    }
  });
  
  const codeBlock = ref(null);
  
  const highlightCode = () => {
    if (codeBlock.value) {
      hljs.highlightElement(codeBlock.value);
    }
  };
  
  // 在组件挂载和更新时进行高亮处理
  onMounted(highlightCode);
  onUpdated(highlightCode);
  </script>
  
  <style scoped>
  .code-container {
    background: #f4f4f4;
    padding: 1em;
    border-radius: 4px;
    overflow-x: auto;
  }
  </style>
  