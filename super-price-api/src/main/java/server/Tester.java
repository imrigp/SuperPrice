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

package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.connection.HttpClientPool;

public class Tester {

    // TODO: 14/10/2020 Create daily task to delete old visited files
    // TODO: 14/10/2020 Pre-compile necessary regex

    private static final Logger log = LoggerFactory.getLogger(Tester.class);

    public static void main(final String[] args) throws SQLException, IOException, XMLStreamException, InterruptedException, SchedulerException {

        if (true) {
            //test();
            Server server = new Server();
            return;
        }
        long startTime = System.nanoTime();
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Time Elapsed in seconds: " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime));
        System.out.println("Time Elapsed in milliseconds: " + TimeUnit.NANOSECONDS.toMillis(estimatedTime));
        System.exit(6);

        HttpClientPool.shutdown();
    }

    private static void test() {
        long startTime = System.nanoTime();

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Time Elapsed in milliseconds: " + TimeUnit.NANOSECONDS.toMillis(estimatedTime));
        System.out.println("Time Elapsed in seconds: " + TimeUnit.NANOSECONDS.toMillis(estimatedTime) / 1000.0);
    }
}
