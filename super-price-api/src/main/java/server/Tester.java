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
