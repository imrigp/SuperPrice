/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
