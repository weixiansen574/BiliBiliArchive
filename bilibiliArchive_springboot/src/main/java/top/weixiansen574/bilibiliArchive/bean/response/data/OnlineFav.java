package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteListResponse;

public class OnlineFav {
    public boolean added;
    public FavoriteListResponse.Info info;

    public OnlineFav(boolean added, FavoriteListResponse.Info info) {
        this.added = added;
        this.info = info;
    }

    public OnlineFav() {
    }
}
