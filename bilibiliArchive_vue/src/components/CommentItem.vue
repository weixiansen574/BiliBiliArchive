<template>
  <div :data-rpid="comment.rpid" :data-mid="comment.mid">
    <div class="owner">
      <div class="avatar-area">
        <img :src="getCommentAvatarUrl(comment.avatarUrl)" class="avatar">
        <img v-if="comment.vipType == 2" src="/src/assets/dahuiyuan.png" class="dhy">
      </div>
      <div class="owner-info">
        <div class="name-and-level">
          <span class="owner-name">{{ comment.uname }}</span>
          <bili-level :level="comment.currentLevel" class="level-icon" />
          <svg v-if="isUp" class="up-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="6" width="18" height="11.5" rx="2" fill="#FF6699" />
            <path
              d="M5.7 8.36V12.79C5.7 13.72 5.96 14.43 6.49 14.93C6.99 15.4 7.72 15.64 8.67 15.64C9.61 15.64 10.34 15.4 10.86 14.92C11.38 14.43 11.64 13.72 11.64 12.79V8.36H10.47V12.81C10.47 13.43 10.32 13.88 10.04 14.18C9.75 14.47 9.29 14.62 8.67 14.62C8.04 14.62 7.58 14.47 7.3 14.18C7.01 13.88 6.87 13.43 6.87 12.81V8.36H5.7ZM13.0438 8.36V15.5H14.2138V12.76H15.9838C17.7238 12.76 18.5938 12.02 18.5938 10.55C18.5938 9.09 17.7238 8.36 16.0038 8.36H13.0438ZM14.2138 9.36H15.9138C16.4238 9.36 16.8038 9.45 17.0438 9.64C17.2838 9.82 17.4138 10.12 17.4138 10.55C17.4138 10.98 17.2938 11.29 17.0538 11.48C16.8138 11.66 16.4338 11.76 15.9138 11.76H14.2138V9.36Z"
              fill="white" />
          </svg>
          <!-- <span>LV{{ comment.owner.level }}</span> -->
        </div>
        <div class="date-and-location">
          <span v-if="comment.floor">#{{ comment.floor }}</span><span>{{ formatTimestamp(comment.ctime * 1000)
          }}</span><span>{{ comment.location }}</span>
        </div>
      </div>
    </div>
    <div class="comment-content" v-html="renderer(comment.message)">

    </div>
    <div v-if="comment.pictures && comment.pictures.length > 0"
      :class='comment.pictures.length > 1 ? "comment-pictures-multi" : "comment-pictures"'>
      <img v-for="(pic, index) in comment.pictures" :key="index" :src="getPictureUrl(pic.img_src)" alt=""
        @click="showImg(index,comment.pictures)">
    </div>
    <div @click="emit('showReplies', comment)" v-if="comment.previewReplies && comment.previewReplies.length > 0"
      class="reply-preview">
      <div v-for="(reply, index) in comment.previewReplies" :key="index" class="mdui-ripple">
        <p>
          <span style="color: #008AC5;">{{ reply.uname }}</span>：<span v-html="renderer(reply.message)"></span>
        </p>
      </div>
      <div v-if="comment.previewReplies.length < comment.replyCount" style="color: #008AC5;" class="mdui-ripple">
        <p>
          共{{ comment.replyCount }}条回复＞
        </p>
      </div>
    </div>
    <div class="stat">
      <svg class="icon" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="16"
        height="16" viewBox="0 0 16 16">
        <path
          d="M9.283433333333333 2.0303066666666663C9.095466666666667 2.0083933333333333 8.921333333333333 2.09014 8.828166666666666 2.1991199999999997C8.424633333333333 2.6711333333333336 8.332133333333333 3.3649466666666665 8.029333333333334 3.9012466666666663C7.630633333333333 4.607453333333333 7.258833333333333 5.034486666666666 6.800866666666666 5.436006666666666C6.42382 5.7665733333333336 6.042199999999999 5.987959999999999 5.666666666666666 6.09112L5.666666666666666 13.1497C6.19062 13.1611 6.751966666666666 13.168333333333333 7.333333333333333 13.168333333333333C8.831233333333333 13.168333333333333 10.1019 13.120766666666665 10.958166666666665 13.076699999999999C11.565133333333332 13.045433333333332 12.091966666666666 12.7451 12.366466666666668 12.256733333333333C12.7516 11.571599999999998 13.2264 10.5669 13.514166666666664 9.3835C13.7823 8.2808 13.904599999999999 7.374333333333333 13.959466666666666 6.734999999999999C13.984933333333332 6.438646666666667 13.750433333333334 6.166686666666667 13.386666666666665 6.166686666666667L10.065133333333332 6.166686666666667C9.898433333333333 6.166686666666667 9.742666666666667 6.08362 9.649833333333333 5.945166666666666C9.536066666666667 5.775493333333333 9.560033333333333 5.5828533333333334 9.6312 5.403346666666666C9.783966666666666 5.013846666666666 9.983933333333333 4.432846666666666 10.062766666666667 3.90454C10.1406 3.3830066666666667 10.121599999999999 2.9639466666666667 9.917133333333332 2.57626C9.697399999999998 2.1596933333333332 9.448266666666665 2.0495266666666665 9.283433333333333 2.0303066666666663zM10.773433333333333 5.166686666666666L13.386666666666665 5.166686666666666C14.269133333333333 5.166686666666666 15.036999999999999 5.875273333333333 14.9558 6.8206C14.897 7.505533333333333 14.767199999999999 8.462733333333333 14.485833333333334 9.6198C14.170333333333334 10.917200000000001 13.6532 12.008466666666665 13.238166666666666 12.746766666666666C12.7729 13.574433333333333 11.910266666666667 14.029 11.009566666666666 14.075366666666667C10.14 14.120166666666666 8.851766666666666 14.168333333333333 7.333333333333333 14.168333333333333C5.862206666666666 14.168333333333333 4.51776 14.1231 3.565173333333333 14.079633333333334C2.4932333333333334 14.030733333333332 1.5939999999999999 13.234466666666666 1.4786599999999999 12.143466666666665C1.4028 11.426066666666665 1.3333333333333333 10.4978 1.3333333333333333 9.501666666666665C1.3333333333333333 8.588966666666666 1.3916466666666667 7.761233333333333 1.4598999999999998 7.104466666666667C1.5791666666666666 5.95696 2.5641 5.166686666666666 3.671693333333333 5.166686666666666L5.166666666666666 5.166686666666666C5.3793066666666665 5.166686666666666 5.709213333333333 5.063186666666667 6.141613333333333 4.68408C6.516733333333333 4.355193333333333 6.816366666666667 4.015666666666666 7.158533333333333 3.409613333333333C7.5023 2.8007333333333335 7.6041 2.0920066666666663 8.068066666666667 1.54932C8.372133333333332 1.1936466666666665 8.8718 0.9755333333333334 9.399233333333333 1.03704C9.949866666666665 1.10124 10.457733333333334 1.4577866666666666 10.801633333333331 2.109713333333333C11.148866666666665 2.767993333333333 11.143799999999999 3.4356599999999995 11.051833333333335 4.0520933333333335C10.993899999999998 4.44022 10.875366666666666 4.852359999999999 10.773433333333333 5.166686666666666zM4.666666666666666 13.122166666666667L4.666666666666666 6.166686666666667L3.671693333333333 6.166686666666667C3.029613333333333 6.166686666666667 2.5161533333333335 6.615046666666666 2.4545466666666664 7.207833333333333C2.3890599999999997 7.837933333333333 2.333333333333333 8.630433333333333 2.333333333333333 9.501666666666665C2.333333333333333 10.453433333333333 2.399833333333333 11.345266666666667 2.473113333333333 12.038333333333334C2.533993333333333 12.614133333333331 3.0083466666666667 13.053199999999999 3.6107466666666665 13.0807C3.9228066666666668 13.094899999999999 4.278173333333333 13.109333333333334 4.666666666666666 13.122166666666667z"
          fill="currentColor">
        </path>
      </svg>
      <span>{{ comment.like }}</span>
      <div class="copy mdui-ripple" @click="copyComment(comment.message)">
        <svg t="1723693657998" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
          p-id="4978" width="64" height="64">
          <path
            d="M720 192h-544A80.096 80.096 0 0 0 96 272v608C96 924.128 131.904 960 176 960h544c44.128 0 80-35.872 80-80v-608C800 227.904 764.128 192 720 192z m16 688c0 8.8-7.2 16-16 16h-544a16 16 0 0 1-16-16v-608a16 16 0 0 1 16-16h544a16 16 0 0 1 16 16v608z"
            p-id="4979" fill="currentColor"></path>
          <path
            d="M848 64h-544a32 32 0 0 0 0 64h544a16 16 0 0 1 16 16v608a32 32 0 1 0 64 0v-608C928 99.904 892.128 64 848 64z"
            p-id="4980" fill="currentColor"></path>
          <path
            d="M608 360H288a32 32 0 0 0 0 64h320a32 32 0 1 0 0-64zM608 520H288a32 32 0 1 0 0 64h320a32 32 0 1 0 0-64zM480 678.656H288a32 32 0 1 0 0 64h192a32 32 0 1 0 0-64z"
            p-id="4981" fill="currentColor"></path>
        </svg>
        <span>复制</span>
      </div>
      <div v-if="comment.replyCount > 1" @click="emit('showMermaidTreeReplies', comment)" class="reply-tree mdui-ripple">
        <svg t="1724077335952" class="icon" viewBox="0 0 1152 1024" version="1.1" p-id="25504" width="64" height="64"
          id="svg1" sodipodi:docname="tree.svg" inkscape:version="1.3.2 (091e20e, 2023-11-25, custom)"
          xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
          xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" xmlns="http://www.w3.org/2000/svg"
          xmlns:svg="http://www.w3.org/2000/svg">
          <defs id="defs1" />
          <path
            d="m 824.3505,44.5825 c -17.294,17.317 -28.189,41.673 -28.189,68.599 v 90.39 c 0,26.922 10.895,50.65 28.189,68.599 17.939,17.317 41.658,28.198 68.585,28.198 h 90.352 c 26.926,0 51.275,-10.881 68.59,-28.198 17.293,-17.95 28.182,-41.678 28.182,-68.599 v -90.39 c 0,-26.927 -10.89,-51.282 -28.182,-68.599 -17.315,-17.317 -41.665,-28.203 -68.59,-28.203 h -90.352 c -26.927,0 -50.646,10.887 -68.585,28.203 z m 158.937,29.485 c 10.895,0 20.506,4.503 27.558,11.542 7.056,7.06 11.547,16.663 11.547,27.574 v 90.39 c 0,10.91 -4.49,20.513 -11.547,27.574 -7.052,7.04 -16.663,10.89 -27.558,10.89 h -90.352 c -10.89,0 -20.502,-3.85 -27.558,-10.89 -7.051,-7.06 -10.91,-16.663 -10.91,-27.574 v -90.39 c 0,-10.91 3.86,-20.513 10.91,-27.574 7.051,-7.039 16.668,-11.542 27.558,-11.542 z m -187.127,400.676 v 90.413 c 0,26.905 10.895,51.282 28.189,68.577 17.939,17.95 41.658,28.205 68.585,28.205 h 90.352 c 26.926,0 51.275,-10.257 68.59,-28.205 17.293,-17.295 28.182,-41.678 28.182,-68.577 v -90.413 c 0,-26.266 -10.89,-50.65 -28.182,-68.593 -17.315,-17.3 -41.665,-28.209 -68.59,-28.209 h -90.352 c -26.927,0 -50.646,10.91 -68.585,28.209 -17.294,17.943 -28.189,42.327 -28.189,68.593 z m 187.127,-38.455 c 10.895,0 20.506,3.843 27.558,10.905 7.056,7.038 11.547,17.295 11.547,27.55 v 90.413 c 0,10.888 -4.49,20.513 -11.547,27.552 -7.052,7.039 -16.663,11.542 -27.558,11.542 h -90.352 c -10.89,0 -20.502,-4.502 -27.558,-11.542 -7.051,-7.039 -10.91,-16.665 -10.91,-27.552 v -90.413 c 0,-10.257 3.86,-20.513 10.91,-27.55 7.051,-7.062 16.668,-10.905 27.558,-10.905 z m -882.493,-30.138 c -17.304,17.943 -28.199,42.327 -28.199,68.593 v 90.413 c 0,26.899 10.895,51.282 28.199,68.577 17.946,17.95 42.3,28.205 68.575,28.205 h 90.363 c 26.92,0 51.275,-10.257 68.579,-28.205 17.946,-17.295 28.199,-41.678 28.199,-68.577 v -90.413 c 0,-26.266 -10.254,-50.65 -28.199,-68.593 -17.304,-17.3 -41.659,-28.209 -68.579,-28.209 h -90.363 c -26.274,0 -50.635,10.91 -68.575,28.209 z m 158.939,30.138 c 10.894,0 20.506,3.843 27.557,10.905 7.051,7.038 11.541,17.295 11.541,27.55 v 90.413 c 0,10.888 -4.49,20.513 -11.541,27.552 -7.052,7.039 -16.663,11.542 -27.557,11.542 h -90.364 c -10.253,0 -20.507,-4.502 -27.557,-11.542 -6.409,-7.039 -10.89,-16.665 -10.89,-27.552 v -90.413 c 0,-10.257 3.839,-20.513 10.89,-27.55 7.05,-7.062 17.304,-10.905 27.557,-10.905 z m 474.889,474.398 c 8.333,0 15.385,-3.217 20.506,-8.345 5.143,-5.123 8.334,-12.166 8.334,-20.508 0,-7.693 -3.192,-15.384 -8.334,-20.513 -5.12,-5.127 -12.172,-8.323 -20.5,-8.323 h -151.893 v -303.873 h 151.892 c 8.327,0 15.379,-3.217 20.5,-8.345 5.143,-5.129 8.986,-12.822 8.986,-20.513 0,-8.324 -3.843,-15.385 -8.986,-20.513 -5.12,-5.127 -12.172,-8.341 -20.5,-8.341 h -151.892 v -303.879 h 151.887 c 8.333,0 15.385,-3.195 20.506,-8.972 5.143,-5.127 8.986,-12.166 8.986,-20.512 0,-7.693 -3.843,-14.731 -8.986,-20.513 -5.12,-5.129 -12.172,-8.324 -20.506,-8.324 h -180.721 c -7.694,0 -15.385,3.195 -20.507,8.324 -5.131,5.781 -8.333,12.82 -8.333,20.513 v 332.731 h -107.026 c -7.694,0 -14.745,3.844 -20.506,8.971 -5.134,5.127 -8.335,12.189 -8.335,20.513 0,7.692 3.202,15.384 8.335,20.513 5.761,5.127 12.812,8.345 20.506,8.345 h 107.026 v 332.704 c 0,8.347 3.202,15.385 8.333,20.513 5.121,5.127 12.814,8.999 20.507,8.999 v -0.654 h 180.721 z m 61.538,-73.727 v 90.414 c 0,26.903 10.895,50.627 28.189,68.577 17.939,17.317 41.658,28.204 68.585,28.204 h 90.352 c 26.926,0 51.275,-10.888 68.59,-28.204 17.293,-17.95 28.182,-41.674 28.182,-68.577 v -90.414 c 0,-26.92 -10.89,-51.275 -28.182,-68.592 -17.315,-17.95 -41.665,-28.205 -68.59,-28.205 h -90.352 c -26.927,0 -50.646,10.257 -68.585,28.205 -17.294,17.317 -28.189,41.672 -28.189,68.592 z m 187.127,-39.087 c 10.895,0 20.506,4.473 27.558,11.513 7.056,7.066 11.547,16.691 11.547,27.573 v 90.414 c 0,10.255 -4.49,20.512 -11.547,27.55 -7.052,7.045 -16.663,10.91 -27.558,10.91 h -90.352 c -10.89,0 -20.502,-3.865 -27.558,-10.91 -7.051,-7.039 -10.91,-16.663 -10.91,-27.55 v -90.414 c 0,-10.881 3.86,-20.508 10.91,-27.573 7.051,-7.039 16.668,-11.513 27.558,-11.513 z"
            p-id="25505" fill="currentColor" id="path1" />
        </svg>
        <span>回复树</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { ElMessage } from 'element-plus'
import BiliLevel from './icons/BiliLevel.vue';
import { getCommentAvatarUrl, formatTimestamp, escapeHtml } from '../util';

import { getCurrentInstance } from 'vue';
// 获取当前实例
const { appContext } = getCurrentInstance();
const hevueImgPreview = appContext.config.globalProperties.$hevueImgPreview;
// console.log($hevueImgPreview)

const props = defineProps({
  comment: Object,
  isUp: Boolean,
  renderer: { type: Function, default: escapeHtml }
})

const emit = defineEmits(["showMermaidTreeReplies", "showReplies"]);

const comment = props.comment;
const isUp = props.isUp;

function getPictureUrl(originalUrl) {
  const fileName = originalUrl.split('/').pop();
  return `/files/archives/comment-pictures/${fileName}`;
}

function copyComment(commentText) {
  // 检查浏览器是否支持 Clipboard API
  if (navigator.clipboard) {
    navigator.clipboard.writeText(commentText).then(() => {
      ElMessage({
        message: '评论已复制到剪贴板',
        type: 'success',
      })
    }).catch((err) => {
      ElMessage.error('无法复制文本，因为: ' + err)
    });
  } else {
    // 备用方法：如果 Clipboard API 不可用，使用旧的 document.execCommand()
    console.warn("你的浏览器不支持navigator.clipboard API")
    const textArea = document.createElement("textarea");
    textArea.value = commentText;
    document.body.appendChild(textArea);
    textArea.select();
    try {
      document.execCommand('copy');
      ElMessage({
        message: '评论已复制到剪贴板',
        type: 'success',
      })
    } catch (err) {
      ElMessage.error('无法复制文本，因为: ' + err)
    }
    document.body.removeChild(textArea);
  }
}

function showImg(index,pictures) {
  console.log(index,pictures)
  let urlList = new Array(pictures.length);
  for(let i in pictures){
    urlList[i] = getPictureUrl(pictures[i].img_src)
  }
  hevueImgPreview({
    multiple: true,
    nowImgIndex: index,
    imgList: urlList
  })
}

</script>

<style scoped>
.owner {
  display: flex;
  align-items: center;
}

.avatar-area {
  position: relative;
  height: 35px;
  width: 35px;
}

.avatar {
  border-radius: 50%;
  width: 100%;
  height: 100%;
}

.dhy {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 32%;
  height: 32%;
  background-color: rgb(255, 255, 255);
  border: 2px solid rgb(255, 255, 255);
  border-radius: 50%;
}

.owner-name {
  color: #757575;
  font-size: 13px;
}

.owner-info {
  margin-left: 8px;
}

.owner-info span {
  margin-right: 6px;
}

.date-and-location {
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin-top: 10px;
  margin-left: 43px;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: keep-all;
}


.icon {
  height: 16px;
  width: 16px;
}

.name-and-level {
  display: flex;
  align-items: center;
}

.level-icon {
  height: 15px;
  width: 25px;
  margin-bottom: 2px;
}

.up-icon {
  height: 24px;
  width: 24px;
  margin-bottom: 2px;
}

.copy {
  display: flex;
  cursor: pointer;
  align-items: center;
}

.reply-tree {
  display: flex;
  cursor: pointer;
  align-items: center;
}

.stat {
  display: flex;
  align-items: center;
  margin-top: 10px;
  margin-left: 42px;
  color: #9499a0;
  font-size: 12px;
}

.stat span {
  margin-left: 5px;
  margin-right: 10px;
}

.comment-pictures {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  margin-left: 43px;
}

.comment-pictures img {
  border-radius: 4px;
  cursor: pointer;
  max-width: 100%;
  max-height: 600px;
}

.comment-pictures-multi {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  margin-top: 10px;
  margin-left: 43px;
}

.comment-pictures-multi img {
  aspect-ratio: 1/1;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  max-width: 100%;
  max-height: 100%;
}

@media only screen and (min-width: 768px) {
  .comment-pictures img {
    max-width: 50%;
  }

  .comment-pictures-multi {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

.reply-preview {
  background-color: #f4f4f4;
  border-radius: 2px;
  margin-left: 43px;
  margin-top: 10px;
  padding-block: 4px;
  padding: 6px 4px;
}

.reply-preview>div {
  padding: 4px;
}

.reply-preview p {
  margin: 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
}
</style>