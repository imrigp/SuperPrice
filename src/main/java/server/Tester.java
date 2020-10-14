package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.quartz.SchedulerException;

public class Tester {

    // TODO: 14/10/2020 Change measure units to enum
    // TODO: 14/10/2020 Create daily task to delete old visited files
    // TODO: 14/10/2020 Change Consumers to CompletableFutures

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


        System.exit(4);
    }
}
