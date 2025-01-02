package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;

import java.util.concurrent.Exchanger;

public class FavoriteDeleteThread extends Thread{

    private final Exchanger<Integer> exchanger;
    private final long favId;
    private final ArchiveDeleteService archiveDeleteService;

    public FavoriteDeleteThread(Exchanger<Integer> exchanger, long favId, ArchiveDeleteService archiveDeleteService) {
        this.exchanger = exchanger;
        this.favId = favId;
        this.archiveDeleteService = archiveDeleteService;
    }

    @Override
    public void run() {
        try {
            exchanger.exchange(PG.titleInit("收藏夹删除"));
        } catch (InterruptedException e) {
            PG.remove();
        }
        archiveDeleteService.deleteFavoriteAndVideos(favId);
        PG.remove();
    }
}
