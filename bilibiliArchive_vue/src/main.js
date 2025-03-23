import { createApp } from 'vue'
import './style.css'
import 'mdui/dist/css/mdui.css'
import App from './App.vue'
import 'default-passive-events'
import router from './router'
import ElementPlus from "element-plus"
import "element-plus/dist/index.css"
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { createPinia } from 'pinia'
import hevueImgPreview from 'hevue-img-preview'

import mdui from 'mdui'
window.mdui = mdui;

// 手动触发 MDUI 初始化
document.addEventListener('DOMContentLoaded', () => {
  mdui.mutation();
});
// Vuetify
//import 'vuetify/styles'
// import { createVuetify } from 'vuetify'
// import * as components from 'vuetify/components'
// import * as directives from 'vuetify/directives'

//去除谷歌浏览器的scroll、wheel等事件警告
(function () {
  if (typeof EventTarget !== "undefined") {
      let func = EventTarget.prototype.addEventListener;
      EventTarget.prototype.addEventListener = function (type, fn, capture) {
          this.func = func;
          if (typeof capture !== "boolean") {
              capture = capture || {};
              capture.passive = false;
          }
          this.func(type, fn, capture);
      };
  }
}())

// const vuetify = createVuetify({
//   components,
//   directives,
// })

var pinia = createPinia();

var app = createApp(App);

app.use(ElementPlus, {
    locale: zhCn,
})


app.use(pinia);
app.use(router);
//app.use(vuetify);
app.use(hevueImgPreview);
app.mount('#app')
