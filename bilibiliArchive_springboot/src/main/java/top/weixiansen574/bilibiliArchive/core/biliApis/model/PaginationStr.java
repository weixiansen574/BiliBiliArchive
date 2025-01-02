package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import com.alibaba.fastjson2.JSON;

import java.util.Objects;

public class PaginationStr {
        public static final String INITIAL = "{\"offset\":\"\"}";
        public PaginationStr(String offset) {
            this.offset = offset;
        }

        public PaginationStr() {
        }

        public String offset;

        public String toJson() {
            Objects.requireNonNull(offset);
            return JSON.toJSONString(this);
        }
    }