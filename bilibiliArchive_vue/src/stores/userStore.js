import { defineStore } from 'pinia';
import axios from 'axios';

export const useUserStore = defineStore('user', {
    state: () => ({
        userList: [], // 用户列表
        isLoading: false // 新增标志位，用于记录是否正在加载用户数据
    }),
    actions: {
        addUser(user) {
            this.userList.push(user); // 本地更新
        },
        removeUser(uid) {
            const index = this.userList.findIndex((user) => user.uid == uid);
            console.log(uid,index)
            if (index !== -1) {
                this.userList.splice(index, 1); // 从数组中移除用户
            }
        },
        getOrAsyncLoad(uid, onLoad) {
            let found = this.getByUid(uid);
            if (found) {
                onLoad(found);
            } else {
                if (this.isLoading) {
                    // 如果正在加载中，等待当前加载完成再处理后续逻辑
                    const checkAgain = setInterval(() => {
                        const result = this.getByUid(uid);
                        if (result) {
                            clearInterval(checkAgain);
                            onLoad(result);
                        }
                    }, 100);
                    return;
                }
                this.isLoading = true; // 设置正在加载标志位为true
                axios.get("/api/users")
                    .then(response => {
                        this.userList = response.data.data;
                        this.isLoading = false; // 请求完成后重置标志位
                        if (onLoad) {
                            onLoad(this.getByUid(uid));
                        }
                    })
                    .catch(error => {
                        this.isLoading = false; // 出现错误也重置标志位
                        console.error("Error fetching user data:", error);
                    });
            }
        },
        getByUid(uid) {
            return this.userList.find((user) => user.uid == uid);
        }
    },
});
