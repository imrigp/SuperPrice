package server.plans;

import org.apache.http.impl.client.CloseableHttpClient;
import server.HttpClientPool;
import server.XmlDownload;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public abstract class Plan {
    CloseableHttpClient client;
    private BlockingQueue<XmlDownload> downloadQueue;
    protected long lastUpdated;

    protected Plan() {
        lastUpdated = -1;
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

    abstract public void scanForFiles();
}
