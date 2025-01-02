package top.weixiansen574.bilibiliArchive.core.http;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MEMHttpLogger {
    final int LIMIT = 1024;
    HttpLog[] logs = new HttpLog[LIMIT];
    int footIndex = 0;
    int size = 0;





    public synchronized void log(String message) {
        if (size < LIMIT){
            logs[size] = new HttpLog(footIndex+size,new Date(),message);
            size ++;
        } else {
            int arrayIndex = footIndex % LIMIT;
            logs[arrayIndex] = new HttpLog(footIndex+size,new Date(),message);
            footIndex++;
        }
    }

    public synchronized List<HttpLog> getPage(int index,int pageSize){
        if (pageSize > LIMIT){
            throw new IllegalArgumentException();
        }
        ArrayList<HttpLog> list = new ArrayList<>(pageSize);
        if (size < LIMIT){

        } else {

        }
        return list;
    }

    private synchronized void getAllLog(){

    }


    public void saveToFile(File file){

    }

    public void addObserver(){

    }

    public void removeObserver(){

    }

    public void clearObserver(){

    }


}
