package server.plans;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.DatabaseState;
import server.connection.HttpClientPool;
import server.xml.XmlDownload;
import server.xml.XmlFile;
import utils.Utils;

import static server.xml.XmlFile.Type.PRICE;
import static server.xml.XmlFile.Type.PRICEFULL;
import static server.xml.XmlFile.Type.STORES;

public abstract class Plan {
    private static final Logger log = LoggerFactory.getLogger(Plan.class);
    private static final Set<XmlFile.Type> SUPPORTED_FILES =
            Collections.unmodifiableSet(EnumSet.of(STORES, PRICEFULL, PRICE));

    protected final String name;
    private CloseableHttpClient client;
    private BlockingQueue<XmlDownload> downloadQueue;
    private final Set<XmlDownload> readyDownloads;
    private Consumer<XmlFile> xmlConsumer;
    private final DatabaseState state;

    protected Plan(String name) {
        this.name = name;
        this.state = DatabaseState.getInstance();
        this.readyDownloads = ConcurrentHashMap.newKeySet();
    }

    public BlockingQueue<XmlDownload> getDownloadQueue() {
        return downloadQueue;
    }

    public void setDownloadQueue(BlockingQueue<XmlDownload> downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    public void setXmlConsumer(Consumer<XmlFile> xmlConsumer) {
        this.xmlConsumer = xmlConsumer;
    }

    public void createClient() {
        this.client = HttpClientPool.getClient();
    }

    public void closeClient() {
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    protected void addToQueue(XmlDownload file) {
        try {
            downloadQueue.put(file);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void scanForFiles() {
        // Give 2 hour grace, the pricefull file should be updated every 24 hours
        long fromTime = Utils.convertUnixTo24H(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(26));
        List<XmlDownload> list = getSortedFileList(fromTime);
        if (list == null) {
            return;
        }
        log.info("{}: Job started", name);
        List<XmlDownload> downloadList = list.stream().filter(this::shouldDownload).collect(Collectors.toList());
        // Add new files to queue, so download threads can start
        downloadList.forEach(this::addToQueue);
        log.info(name + " Scan done. " + downloadList.size() + " new files to download.");
        waitForDownloads(downloadList);
        log.info("{}: Job done", name);
        readyDownloads.clear();
    }

    private void waitForDownloads(List<XmlDownload> downloadList) {
        // We want to parse the files sequentially, so we wait for each file in order
        for (XmlDownload xmlDownload : downloadList) {
            log.info("{}: waiting for {}", name, xmlDownload);

            synchronized (readyDownloads) {
                while (!readyDownloads.contains(xmlDownload)) {
                    try {
                        readyDownloads.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // consume (parse) the xmlFile
            xmlConsumer.accept(xmlDownload.getXmlFile());
        }
    }

    public void addDownload(XmlDownload xmlDownload) {
        readyDownloads.add(xmlDownload);
        synchronized (readyDownloads) {
            readyDownloads.notifyAll();
        }
    }

    private boolean shouldDownload(XmlDownload file) {
        return (state.isNewFile(file) && SUPPORTED_FILES.contains(file.getXmlFile().getType()));
    }

    abstract protected List<XmlDownload> getSortedFileList(long fromTime);
}
