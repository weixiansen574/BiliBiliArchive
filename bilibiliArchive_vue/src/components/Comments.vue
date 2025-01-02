<template>
  <div class="video-comments">
    <div class="order-by">
      <span class="order-by-status">{{ sortDesc }}</span>
      <el-dropdown trigger="click">
        <span class="el-dropdown-link">
          排序方式
          <el-icon class="el-icon--right">
            <arrow-down />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="sortByLikes()">按点赞</el-dropdown-item>
            <el-dropdown-item @click="sortByCtimeDesc()">按时间</el-dropdown-item>
            <!-- <el-dropdown-item>按回复</el-dropdown-item> -->
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div v-infinite-scroll="load" :infinite-scroll-disabled="loading" infinite-scroll-immediate="false"
      infinite-scroll-distance="2160" class="comment-list">
      <comment-item v-for="comment in comments" :key="comment.rpid" :comment="comment" :isUp="upUid == comment.mid"
        :renderer="renderCommentWithEmojis" @showMermaidTreeReplies="openDialog" @showReplies="repliesRpid = comment.rpid"
        class="border-comment-item" />
    </div>
    <div v-if="loading" class="mdui-progress">
      <div class="mdui-progress-indeterminate"></div>
    </div>
    <div v-if="noMore" class="list-end">╮(╯3╰)╭再怎么找也没有啦</div>
    <div v-if="dialogFormVisible" id="largeDialog" class="dialog">
      <div class="dialog-content">
        <div class="dialog-title">
          <h2>Mermaid 评论回复树</h2>
          <span class="close-btn" @click="closeDialog()">&times;</span>
        </div>
        <div v-if="!commentTreeData" class="mdui-progress">
          <div class="mdui-progress-indeterminate"></div>
        </div>
        <mermaid-comment-tree v-if="commentTreeData" :reply-page="commentTreeData" />
      </div>
    </div>
    <div v-if="repliesRpid != 0" class="dialog">
      <div class="replies-dialog">
        <div class="dialog-title">
          <h2>评论详情</h2>
          <span class="close-btn" @click="repliesRpid = 0">&times;</span>
        </div>
        <el-scrollbar>
          <div v-if="commentRepliesData">
            <comment-item style="margin-top: 8px; padding-inline: 10px;" :comment="commentRepliesData.root"
              :renderer="renderCommentWithEmojis" :isUp="upUid == commentRepliesData.root.mid" />
            <div style="border-top: 8px solid #F4F4F4;margin-bottom: 8px;"></div>
            <comment-item class="border-comment-item" style="padding-inline: 10px;"
              v-for="comment in commentRepliesData.replies" :key="comment.rpid" :comment="comment"
              :renderer="renderCommentWithEmojis" :isUp="upUid == comment.mid" />
          </div>
          <div v-else class="mdui-progress">
            <div class="mdui-progress-indeterminate"></div>
          </div>
          <!-- <comments :oid="oid" :type="type" :up-uid="upUid"/> -->
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'
import { computed, ref, defineProps, watch } from 'vue'
import { reactive } from 'vue';
import CommentItem from './CommentItem.vue';
import MermaidCommentTree from "./MermaidCommentTree.vue"
import axios from 'axios';
import { escapeHtml } from '../util'
const dialogFormVisible = ref(false)

const props = defineProps({
  oid: Number,
  type: Number,
  upUid: Number
})

const sortDesc = ref("点赞降序")
const oid = props.oid;
const type = props.type;
const upUid = props.upUid;

const repliesRpid = ref(0);
const commentRepliesData = ref(null);

var pn = 1;
var sort = 1;

const loading = ref(false)
const noMore = ref(false)

const commentTreeData = ref(null);

const isLoading = computed(() => loading.value || noMore.value);

const comments = ref([]);

let emoteMap = null;

function splitString(input) {
  // 使用正则表达式拆分字符串，将[]里的内容当作一个单独的部分
  const regex = /(\[.*?\])/g;
  const parts = [];
  let lastIndex = 0;

  // 使用replace进行匹配和拆分
  input.replace(regex, (match, p1, offset) => {
    // 将前面的部分添加到结果中
    if (offset > lastIndex) {
      parts.push(input.slice(lastIndex, offset));
    }
    // 将匹配到的[]部分添加到结果中
    parts.push(p1);
    lastIndex = offset + match.length;
    return '';
  });

  // 添加剩余的部分
  if (lastIndex < input.length) {
    parts.push(input.slice(lastIndex));
  }

  return parts;
}

// 渲染评论和表情
function renderCommentWithEmojis(commentText) {
  // 先对输入的评论文本进行HTML转义，防止XSS注入
  let safeText = escapeHtml(commentText);
  let splitList = splitString(safeText);
  for (let i in splitList) {
    splitList[i] = match(splitList[i]);
  }
  return splitList.join('');
}

function match(li) {
  if (li.startsWith("[")) {
    var fileName = emoteMap.get(li);
    if (fileName) {
      return `<img style="width:1.4em;height:1.4em;vertical-align:text-bottom;" src="/files/emote/${fileName}" alt="${li}" />`;
    }
  }
  return `<span>${li}</span>`
}

async function load() {
  if (isLoading.value) {
    return;
  }
  //console.log("触发加载", pn, loading.value, isLoading.value)
  loading.value = true;
  if (!emoteMap) {
    let emotes = (await axios.get("/api/emote")).data.data;
    emoteMap = new Map(emotes.map(emote => [emote.text, emote.fileName]));
    console.log("已加载"+emotes.length+"个表情")
  }
  let data = (await axios.get("/api/comments/main", { params: { type, oid, sort, pn } })).data.data;
  console.log("push", data.comments)
  if (data.comments.length == 0) {
    noMore.value = true;
    loading.value = false;
    return;
  }
  comments.value.push(...data.comments)
  pn++;
  loading.value = false;
}

function sortByLikes() {
  comments.value = [];
  pn = 1;
  sort = 1;
  sortDesc.value = "点赞降序"
  load();
}

function sortByCtimeDesc() {
  comments.value = [];
  pn = 1;
  sort = 0;
  sortDesc.value = "时间降序"
  load();
}

function openDialog(comment) {
  dialogFormVisible.value = true;
  document.body.style.overflow = "hidden"; // 禁用滚动
  axios.get(`/api/comments/reply`, {
    params: { type, oid, ps: -1, root: comment.rpid }
  })
    .then(response => {
      commentTreeData.value = response.data.data;
    })
}

function closeDialog() {
  dialogFormVisible.value = false;
  commentTreeData.value = null;
  document.body.style.overflow = ""; // 恢复滚动
}

watch(repliesRpid, (newValue) => {
  if (newValue != 0) {
    document.body.style.overflow = "hidden";
    commentRepliesData.value = null;
    axios.get(`/api/comments/reply`, {
      params: { type, oid, ps: -1, root: newValue }
    })
      .then(response => {
        commentRepliesData.value = response.data.data;
      })
  } else {
    document.body.style.overflow = "";
  }
})

load();

// 将 emoji 列表转换为一个 Map，方便 O(1) 查找
//var emoteMap = new Map(emotes.map(emoji => [emoji.text, emoji.fileName]));



</script>

<style scoped>
.el-dropdown-link {
  display: flex;
}

.el-dropdown {
  cursor: pointer;
}

.order-by {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.list-end {
  display: flex;
  height: 50px;
  width: 100%;
  align-items: center;
  justify-content: center;
}

.border-comment-item {
  border-bottom: 0.5px solid #ddd;
  padding-bottom: 5px;
  margin-bottom: 10px;
  /* height: 0.5px;
    background-color: #ddd;
    margin-top: 10px;
    margin-bottom: 10px; */
}



.dialog {
  display: block;
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 999;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.4);
}

.dialog-content {
  background-color: #fefefe;
  position: fixed;
  overflow: hidden;
  top: 80px;
  right: 80px;
  bottom: 40px;
  left: 80px;
  border-radius: 3px;
  display: flex;
  flex-direction: column;
}

@media only screen and (max-width: 768px) {
  .dialog-content {
    top: 80px;
    right: 10px;
    bottom: 30px;
    left: 10px;
  }
}

.replies-dialog {
  box-sizing: border-box;
  width: 96%;
  top: 80px;
  bottom: 40px;
  max-width: 1280px;
  left: 50%;
  transform: translate(-50%);
  background-color: white;
  /* background-color: #F4F4F4; */
  position: fixed;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 3px;
}

@media (min-width: 600px) {
  .replies-dialog {
    width: 94%;
  }
}

@media (min-width: 1024px) {
  .replies-dialog {
    width: 92%;
  }
}


.dialog-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px;
  padding-left: 10px;
  border-bottom: 1px solid #EEEEEE;
}

.dialog-title>h2 {
  margin: 0;
}

.close-btn {
  color: #aaa;
  float: right;
  font-size: 28px;
  font-weight: bold;
  margin-right: 8px;
}

.close-btn:hover,
.close-btn:focus {
  color: black;
  text-decoration: none;
  cursor: pointer;
}

.mdui-progress {
  max-width: 300px;
  margin-top: 10px;
  margin-inline: auto;
}
</style>