package top.weixiansen574.bilibiliArchive.core.biliApis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动生成Wbi(w_ts=xxx&w_rid=xxx)
 * 需要返回值为ApiCall
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableWbi {
}
