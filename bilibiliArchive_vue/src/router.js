import { createRouter, createWebHistory } from "vue-router";

const routes = [{
  path: "/",
  redirect: "/home"
}, {
  path: "/test",
  component: () =>
    import("./pages/Test.vue")
},
{
  path: "/home",
  name: "home",
  component: () =>
    import("./components/Home.vue"),
  meta: { title: '首页' }
},
{
  path: "/video/:bvid",
  meta:{title:"视频"},
  component: () =>
    import("./components/VideoPlay.vue"), //import("./components/VideoPlay.vue")
  props: true,
},
{
  path: "/video-backup-config",
  meta: { title: '视频备份配置' },
  component: () =>
    import("./pages/BackupConfigManage.vue"),
  children: [{
    path: "",
    name: "video-backup-configs",
    component: () =>
      import("./pages/VideoBackupConfigs.vue"),
  }, {
    path: ":id",
    name: "video-backup-config",
    props: true,
    component: () =>
      import("./pages/VideoBackupConfigEditor.vue")
  }]
},
{
  path: "/update-plans",
  name: "update-plans",
  meta: { title: '更新计划' },
  component: () => import("./pages/VideoUpdatePlans.vue")
}, {
  path: "/emote-manger",
  name: "emote-manger",
  meta: { title: '评论表情管理' },
  component: () => import("./pages/EmoteManger.vue")
}, {
  path: "/video-search",
  name: "video-search",
  meta: { title: "视频搜索" },
  component: () => import("./pages/VideoSearch.vue")
},
{
  path: "/settings",
  name: "settings",
  meta: { title: '备份设置' },
  component: () =>
    import("./pages/Settings.vue")
},
{
  path: '/user/:uid',
  prpos: true,
  component: () =>
    import('./components/User.vue'),
  meta: { title: '用户详情' },
  children: [{
    path: "",
    name: "user",
    redirect: { name: "favorites" }
  },
  {
    path: 'favorites',
    component: () =>
      import('./pages/RView.vue'),
    children: [{
      path: '',
      name: "favorites",
      component: () =>
        import("./pages/FavList.vue"),
    },
    {
      path: ':favId',
      name: "fav-videos",
      component: () =>
        import('./components/FavoriteVideoList.vue'),
    }
    ]
  },
  {
    path: 'history',
    name: "history",
    component: () =>
      import('./pages/HistoryVideoList.vue'),
  },
  {
    path: 'uploader', //:upId
    component: () =>
      import('./pages/RView.vue'),
    children: [{
      path: "",
      name: "uploader",
      component: () =>
        import("./pages/UploaderList.vue")
    }, {
      path: ":upUid",
      props: true,
      component: () =>
        import('./pages/RView.vue'),
      children: [{
        path: "",
        redirect: { name: "up-video" }
      }, {
        path: "video",
        name: "up-video",
        component: () =>
          import("./pages/UpVideoList.vue")
      }, {
        path: "dynamic",
        name: "up-dynamic",
        component: () =>
          import("./pages/UpDynamic.vue")
      }, {
        path: "article",
        name: "up-article",
        component: () =>
          import("./pages/UpArticle.vue")
      }, {
        path: "live-record",
        name: "up-live-record",
        component: () =>
          import("./pages/UpLiveRecord.vue")
      }]
    }]
  }, {
    path: "config",
    name: "user-config",
    component: () =>
      import('./components/UserConfig.vue')
  }
  ]
}, {
  path: "/user/add",
  component: () =>
    import("./pages/UserAdd.vue"),
  meta: { title: '添加用户' },
  children: [{
    path: "",
    name: "add-user",
    redirect: { name: "add-user-from-cookie" }
  },
  {
    path: "cookie",
    name: "add-user-from-cookie",
    component: () =>
      import("./pages/UserAddFromCookie.vue")
  },
  {
    path: "qrcode",
    name: "add-user-from-qrcode",
    component: () =>
      import("./pages/UserAddFromQRCode.vue")
  }
  ]
}
];


const router = createRouter({
  history: createWebHistory(),
  routes: routes
});

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title+"_哔哩存档姬"
  } else {
    document.title = "哔哩存档姬"
  }
  next()
})

export default router;