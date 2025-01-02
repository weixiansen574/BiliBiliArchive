package top.weixiansen574.bilibiliArchive.core.operation.exception;

import top.weixiansen574.bilibiliArchive.core.operation.progress.Progress;

import java.util.Date;
import java.util.LinkedList;

public class ExceptionRecorder {
    private static final LinkedList<ExceptionRecord> exceptionRecords = new LinkedList<>();
    private static final LinkedList<ExceptionObserver> observers = new LinkedList<>();
    private static long autoincrementId = 1;

    public synchronized static void add(Throwable throwable,String message){
        ExceptionRecord exceptionRecord = new ExceptionRecord(autoincrementId++, throwable, new Date(), message);
        exceptionRecords.add(exceptionRecord);
        observerSend(exceptionRecord);
    }

    public synchronized static ExceptionRecord remove(long id){
        for (ExceptionRecord exceptionRecord : exceptionRecords) {
            if (exceptionRecord.id() == id){
                exceptionRecords.remove(exceptionRecord);
                return exceptionRecord;
            }
        }
        return null;
    }

    public synchronized static void addObserver(ExceptionObserver observer){
        for (ExceptionRecord exceptionRecord : exceptionRecords) {
            observer.send(exceptionRecord);
        }
        observers.add(observer);
    }

    public static void observerSend(ExceptionRecord record) {
        observers.removeIf(observer -> !observer.send(record));
    }

    public static LinkedList<ExceptionRecord> getExceptionRecords(){
        return exceptionRecords;
    }
}
