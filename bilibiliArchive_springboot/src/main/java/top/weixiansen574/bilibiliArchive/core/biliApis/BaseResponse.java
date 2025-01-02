package top.weixiansen574.bilibiliArchive.core.biliApis;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;


public abstract class BaseResponse {

    public int code;

    @Nullable
    public String message;

    public int ttl;

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public boolean isSuccess() {
        return this.code == 0;
    }


}