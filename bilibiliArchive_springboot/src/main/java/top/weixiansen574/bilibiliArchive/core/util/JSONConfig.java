package top.weixiansen574.bilibiliArchive.core.util;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class JSONConfig {

    public static String toFormattedJSONString(Object object){
        return JSON.toJSONString(object,JSONWriter.Feature.PrettyFormat,JSONWriter.Feature.WriteNulls);
    }

    public static <T> T readFromFile(File file,Class<T> tClass) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Object o = JSON.parseObject(inputStream.readAllBytes(), tClass);
        inputStream.close();
        return tClass.cast(o);
    }

    public static void writeToFile(File file,Object o) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(JSON.toJSONBytes(o, JSONWriter.Feature.PrettyFormat,JSONWriter.Feature.WriteNulls));
        outputStream.close();
    }

}
