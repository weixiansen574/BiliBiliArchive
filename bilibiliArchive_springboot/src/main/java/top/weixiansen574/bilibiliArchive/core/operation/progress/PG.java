package top.weixiansen574.bilibiliArchive.core.operation.progress;

import top.weixiansen574.bilibiliArchive.core.ContentBackupLoopThread;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;

import java.util.*;
import java.util.concurrent.FutureTask;

public class PG {
    public static final int TYPE_CANCEL = -1;
    public static final int TYPE_BACKUP_LOOP = 0;
    public static final int TYPE_UPDATE = 1;
    public static final int TYPE_TASK = 2;

    public static int incrementId = 0;

    private static final List<ProgressObserver> observers = new LinkedList<>();
    private static final ThreadLocal<Integer> local = new ThreadLocal<>();
    private static final SortedMap<Integer, Progress> viewMap = new TreeMap<>();

/*    public static synchronized void send(String title, String content, Object data) {
        if (local.get() == null) {
            local.set(incrementId++);
        }
        Thread thread = Thread.currentThread();
        int type = TYPE_TASK;
        if (thread instanceof ContentBackupLoopThread) {
            type = TYPE_BACKUP_LOOP;
        } else if (thread instanceof ContentUpdateThread) {
            type = TYPE_UPDATE;
        }

        Integer id = local.get();
        Progress progress = new Progress(id, type, title, content, data);
        viewMap.put(id, progress);
        observerSend(progress);
    }*/

    public static synchronized int titleInit(String title){
        if (local.get() == null) {
            local.set(incrementId++);
        }
        Thread thread = Thread.currentThread();
        int type = TYPE_TASK;
        if (thread instanceof ContentBackupLoopThread) {
            type = TYPE_BACKUP_LOOP;
        } else if (thread instanceof ContentUpdateThread) {
            type = TYPE_UPDATE;
        }
        Integer id = local.get();
        Progress progress = new Progress(id, type, title, null, null);
        viewMap.put(id, progress);
        return id;
    }

    public static synchronized void content(String content) {
        contentAndData(content, null);
    }

    public static synchronized void content(String format, Object... args){
        content(String.format(format, args));
    }

    public static synchronized void contentAndPrintf(String format, Object... args){
        Progress progress = getViewProgress();
        String content = String.format(format, args);
        System.out.println("["+progress.title+"]"+content);
        contentAndData(progress,content, null);
    }

    public static synchronized void contentAndData(String content, Object data) {
        contentAndData(getViewProgress(),content,data);
    }

    public static synchronized void contentAndData(String format, Object data, Object... args) {
        contentAndData(String.format(format, args),data);
    }

    private static synchronized void contentAndData(Progress progress,String content, Object data){
        progress.content = content;
        progress.data = data;
        observerSend(progress);
    }

    public static Progress getViewProgress(){
        Integer id = local.get();
        if (id == null){
            throw new IllegalStateException("Uninitialized. Before that, place invoke titleInit(String title)");
        }
        return viewMap.get(id);
    }


    public static synchronized void remove() {
        Integer id = local.get();
        if (id != null) {
            viewMap.remove(id);
            local.remove();
            observerSend(new Progress(id, TYPE_CANCEL, null, null, null));
        }
    }

    public static void observerSend(Progress progress) {
        observers.removeIf(observer -> !observer.send(progress));
    }

    public static synchronized void addObserver(ProgressObserver observer) {
        if (observer.idFilter != null){
            for (Map.Entry<Integer, Progress> entry : viewMap.entrySet()) {
                if (Objects.equals(entry.getKey(), observer.idFilter)){
                    observer.send(entry.getValue());
                }
            }
        } else {
            for (Map.Entry<Integer, Progress> entry : viewMap.entrySet()) {
                observer.send(entry.getValue());
            }
        }
        observers.add(observer);
    }
}
