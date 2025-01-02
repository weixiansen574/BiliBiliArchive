package top.weixiansen574.bilibiliArchive.core.task;

import java.util.concurrent.Callable;

public interface KeyedCall<T> extends Callable<T> {
    /**
     * 获取标识，标识一致的将会排队执行
     */
    String getKey();
}
