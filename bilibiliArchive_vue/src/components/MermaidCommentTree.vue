<template>
    <div class="container">
        <!-- YouTube做不出来系列，因为油管神经病般的API，评论回复没有留下被回复评论的ID，屎倒是占了整个API响应体的90%😅 -->
        <VueMermaidRender :content="generateMermaidCode()" ref="mermaidEle" id="ele" />
        <!-- <pre class="mermaid" ref="mermaidEle" id="mermaid">{{ mermaidCode }}
        </pre> -->
    </div>
</template>
   
<script setup>
//import mermaid from "mermaid"; //引用mermaid
import { onMounted, ref, defineProps } from "vue";
import Panzoom from "@panzoom/panzoom"
import { VueMermaidRender } from 'vue-mermaid-render'
import { getCommentAvatarUrl,formatTimestamp } from "../util";

const props = defineProps({
    replyPage: Object
})

const rootComment = props.replyPage.root;
const replies = props.replyPage.replies;

const mermaidEle = ref(null);

function generateMermaidCode() {
    var buffer = ["graph LR"]
    buffer.push(toMermaidCommentItem(rootComment))
    replies.forEach(comment => {
        buffer.push(toMermaidCommentItem(comment));
    });

    var result = buffer.join('');
    console.log(result);
    return result;
}

function toMermaidCommentItem(comment) {
    var header;
    if (comment.parent === 0) {
        header = `${comment.rpid}`;
    } else {
        header = `${comment.parent} --> ${comment.rpid}`;
    }
    return `\n${header}["<div style="width:400px" class="comment-item"><div class="owner"><img src="${getCommentAvatarUrl(comment.avatarUrl)}" class="avatar"><div class="owner-info"><div class="name-and-level" style="text-align: left;"><span class="owner-name">${comment.uname} LV${comment.currentLevel}</span></div><div class="date-and-location"><span>${formatTimestamp(comment.ctime * 1000)}</span><span>${comment.location}</span></div></div></div><div class="comment-content">${escapeHtml(comment.message)}</div><div class="stat">👍<span class="like-count">${comment.like}</span></div></div>"]`
}

//防止特殊符号被解释导致显示异常与XSS攻击
function escapeHtml(str) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
        '`': '&#96;'
    };

    return str.replace(/[&<>"'`]/g, function (match) {
        return map[match];
    });
}


onMounted(() => {
    // mermaid.initialize({ startOnLoad: true });
    // mermaid.init();

    const element = document.getElementById("ele")
    const panzoom = Panzoom(element, {
        // maxScale: 100,
        // minScale: 0.1,
        // contain: 'inside'
    });

    //console.log(element)
    element.addEventListener('wheel', panzoom.zoomWithWheel, { passive: false }/*f防止控制台一堆报错 */);
});


</script>

<style>
.container {
    height: 100%;
}


.comment-item {
    text-align: left;
}

.owner {
    display: flex;
    align-items: center;
}

.comment-content {
    margin-top: 5px;
    margin-left: 43px;
    white-space: pre-line;
    word-wrap: break-word;
    word-break: keep-all;
}

.owner-info {
    margin-left: 8px;
}

.owner-info span {
    margin-right: 6px;
}

.owner-name {
    color: #757575;
    font-size: 13px;
}

.date-and-location {
    font-size: 12px;
    color: #999;
}

.avatar {
    border-radius: 50%;
    height: 35px;
    width: 35px !important;
}

.stat {
    display: flex;
    align-items: flex-end;
    margin-top: 10px;
    margin-bottom: 5px;
    margin-left: 42px;
    color: #9499a0;
}

.like-count {
    font-size: 12px;
}
</style>