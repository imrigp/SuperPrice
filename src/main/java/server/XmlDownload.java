package server;

import org.apache.any23.encoding.TikaEncodingDetector;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class XmlDownload implements Comparable<XmlDownload>, Downloadable<XmlFile> {
    private static final String LEGAL_EXTENSIONS = "gz|gzip|xml";

    private URI uri;
    private String ext;
    private XmlFile xmlFile;
    private CloseableHttpClient client;
    private boolean poisonPill;

    private XmlDownload() {
        this.poisonPill = true;
    }

    public XmlDownload(CloseableHttpClient client, String url, XmlFile xmlFile) {
        try {
            this.poisonPill = false;
            this.client = client;
            this.uri = new URI(url);
            this.ext = FilenameUtils.getExtension(uri.getPath());
            if (!this.ext.matches(LEGAL_EXTENSIONS)) {
                throw new IllegalArgumentException("Bad extension: " + this.ext);
            }
            this.xmlFile = xmlFile;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad url: " + url);
        }
    }

    private XmlDownload(XmlFile xmlFile) {
        this.poisonPill = false;
        this.client = null;
        this.uri = null;
        this.ext = null;
        this.xmlFile = xmlFile;
    }

    public XmlFile execute() {
        if (true) {
            return xmlFile;
        }
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = client.execute(request)) {
            InputStream in = response.getEntity().getContent();
            /* So far I've found TivTaam violates encoding regulations of UTF-8, luckily only with one file (Stores)
             * where they use UTF-16 for it. The good news is, they don't compress it (also a violation...)
             * so I deduced they'll use UTF-16 encoding in any file they wouldn't compress.
             * If additional non-determinant violations will be found, an encoding detection should be used. */
            Charset cs;
            // Decompress if needed and set encoding.
            if (!ext.equals("xml")) {
                in = new GZIPInputStream(in);
                cs = StandardCharsets.UTF_8;
            } else {
                cs = StandardCharsets.UTF_16;
            }

            String res = IOUtils.toString(in, cs);
            xmlFile.setXml(res);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
        }
        return xmlFile;
    }

    public boolean isPoisoned() {
        return poisonPill;
    }

    public static XmlDownload createPoison() {
        return new XmlDownload();
    }

    /* Creates a dummy object with minimum date, so it'd always be the first item after sorting.
     * This is used to insert the stores file in it's place, thus avoiding the shifting which occurs when inserting
     * an element to the first index of an ArrayList
     */
    public static XmlDownload createSentinel() {
        return new XmlDownload(new XmlFile("Stores0-0-0.gz"));
    }

    public static Charset guessCharset(InputStream is) throws IOException {
        return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
    }

    public XmlFile getXmlFile() {
        return xmlFile;
    }

    @Override
    public int compareTo(XmlDownload o) {
        return this.xmlFile.compareTo(o.xmlFile);
    }

    @Override
    public String toString() {
        return xmlFile.toString();
    }
}
