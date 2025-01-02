package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;

import java.util.concurrent.Exchanger;

public class UploaderDeleteThread extends Thread {
    private final Exchanger<Integer> exchanger;
    private final long upUid;
    private final ArchiveDeleteService archiveDeleteService;

    public UploaderDeleteThread(Exchanger<Integer> exchanger, long upUid, ArchiveDeleteService archiveDeleteService) {
        this.exchanger = exchanger;
        this.upUid = upUid;
        this.archiveDeleteService = archiveDeleteService;
    }

    @Override
    public void run() {
        try {
            exchanger.exchange(PG.titleInit("UP主删除"));
        } catch (InterruptedException e) {
            PG.remove();
        }
        archiveDeleteService.deleteUploaderAndArchives(upUid);
        PG.remove();
    }
}
