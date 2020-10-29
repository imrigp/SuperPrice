package server.connection;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class DownloaderThread<E extends Downloadable<E>> implements Runnable {

    private final BlockingQueue<E> queue;
    private final Consumer<E> consumer;

    public DownloaderThread(BlockingQueue<E> queue, Consumer<E> consumer) {
        this.queue = queue;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        E el = null;
        while (true) {
            try {
                el = queue.take();
                if (el.isPoisoned()) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assert el != null;
            E f = el.download(); // download file
            consumer.accept(f);
        }
    }
}
