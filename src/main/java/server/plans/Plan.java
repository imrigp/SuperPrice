package server.plans;

import org.apache.http.impl.client.CloseableHttpClient;
import server.HttpClientPool;
import server.XmlDownload;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class Plan {
    CloseableHttpClient client;
    private BlockingQueue<XmlDownload> downloadQueue;
    protected long lastUpdated;

    protected Plan() {
        // Give 2 grace hours, the pricefull file should be updated every 24 hours
        lastUpdated = Utils.convertUnixTo24H(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(26));
    }

    public BlockingQueue<XmlDownload> getDownloadQueue() {
        return downloadQueue;
    }

    public void setDownloadQueue(BlockingQueue<XmlDownload> downloadQueue) {
        this.downloadQueue = downloadQueue;
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

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    protected void addToQueue(XmlDownload file) {
        try {
            downloadQueue.put(file);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void scanForFiles() {
        List<XmlDownload> list = getSortedFileList();
        if (list == null) {
            return;
        }
        // update latest file date
        this.lastUpdated = list.get(list.size() - 1).getXmlFile().getDate();
        // Add files to queue, let downloader threads take it from here
        list.forEach(this::addToQueue);

        // Add poisons to queue to notify threads they are done
        /*for (int i = 0; i < getThreadNumber(); i++) {
            addToQueue(XmlDownload.createPoison());
        }*/
    }

    // It's important that the first file is the Stores
    abstract protected ArrayList<XmlDownload> getSortedFileList();
}
