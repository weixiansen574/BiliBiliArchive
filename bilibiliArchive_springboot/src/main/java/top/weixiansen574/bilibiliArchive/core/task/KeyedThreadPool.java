package top.weixiansen574.bilibiliArchive.core.task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyedThreadPool {
    private final ConcurrentHashMap<String, TaskQueue> taskMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public KeyedThreadPool(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    // 提交任务
    public void submit(String key, Runnable task) {
        taskMap.computeIfAbsent(key, k -> new TaskQueue(key)).addTask(task);
    }


    // 内部任务队列类
    private class TaskQueue {
        private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        private final AtomicBoolean isProcessing = new AtomicBoolean(false);
        private final String key;

        public TaskQueue(String key) {
            this.key = key;
        }

        // 添加任务
        public void addTask(Runnable task) {
            queue.offer(task);
            processTasks();
        }

        // 处理任务
        private void processTasks() {
            if (isProcessing.compareAndSet(false, true)) {
                executorService.submit(() -> {
                    try {
                        Runnable nextTask;
                        while ((nextTask = queue.poll()) != null) {
                            try {
                                nextTask.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        isProcessing.set(false);
                        if (!queue.isEmpty()) {
                            processTasks(); // 再次触发，防止任务丢失
                        } else {
                            taskMap.remove(key); // 队列为空时移除
                        }
                    }
                });
            }
        }
    }

    // 关闭线程池
    public void shutdown() {
        executorService.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        KeyedThreadPool pool = new KeyedThreadPool(5);

        // 示例：任务提交
        pool.submit("A", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task A1 by " + Thread.currentThread().getName());
        });
        pool.submit("B", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task B1 by " + Thread.currentThread().getName());
        });
        pool.submit("A", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task A2 by " + Thread.currentThread().getName());
        });
        pool.submit("A", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task A2 by " + Thread.currentThread().getName());
        });
        pool.submit("B", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task B2 by " + Thread.currentThread().getName());
        });
        pool.submit("C", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Task C1 by " + Thread.currentThread().getName());
        });

        Thread.sleep(5000); // 等待任务完成
        pool.shutdown();
    }
}
