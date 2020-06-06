package server;


import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class DownloaderThread<F, E extends Downloadable<F>> implements Runnable {

    private BlockingQueue<E> queue;
    private Consumer<F> consumer;

    public DownloaderThread(BlockingQueue<E> queue, Consumer<F> consumer) {
        this.queue = queue;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        int i = 0;
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
            F f = el.execute(); // download file
            //System.out.println(Thread.currentThread().getName() + " downloaded: " + el);
            consumer.accept(f);
        }
    }
}
