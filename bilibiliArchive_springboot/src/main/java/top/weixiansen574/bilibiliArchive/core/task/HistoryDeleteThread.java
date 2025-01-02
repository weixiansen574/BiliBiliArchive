package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;

import java.util.concurrent.Exchanger;

public class HistoryDeleteThread extends Thread{
    private final Exchanger<Integer> exchanger;
    private final long uid;
    private final ArchiveDeleteService archiveDeleteService;

    public HistoryDeleteThread(Exchanger<Integer> exchanger, long uid, ArchiveDeleteService archiveDeleteService) {
        this.exchanger = exchanger;
        this.uid = uid;
        this.archiveDeleteService = archiveDeleteService;
    }

    @Override
    public void run() {
        try {
            exchanger.exchange(PG.titleInit("历史记录删除"));
        } catch (InterruptedException e) {
            PG.remove();
        }
        archiveDeleteService.deleteHistoryAndVideos(uid);
        PG.remove();
    }
}
