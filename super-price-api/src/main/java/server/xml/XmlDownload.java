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

package server.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.any23.encoding.TikaEncodingDetector;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.connection.Downloadable;

public class XmlDownload implements Comparable<XmlDownload>, Downloadable<XmlDownload> {
    private static final Logger log = LoggerFactory.getLogger(XmlDownload.class);
    private static final String LEGAL_EXTENSIONS = "gz|gzip|xml";

    private URI uri;
    private String ext;
    private XmlFile xmlFile;
    private CloseableHttpClient client;
    private Consumer<XmlDownload> downloadConsumer;

    private XmlDownload() {
    }

    public XmlDownload(
            CloseableHttpClient client, String url, XmlFile xmlFile, Consumer<XmlDownload> downloadConsumer) {
        try {

            this.uri = new URIBuilder(url.replace("&amp;", "&")).build();
            this.ext = FilenameUtils.getExtension(uri.getPath());
            if (!this.ext.matches(LEGAL_EXTENSIONS)) {
                throw new IllegalArgumentException("Bad extension: " + this.ext);
            }

            this.downloadConsumer = downloadConsumer;
            this.client = client;
            this.xmlFile = xmlFile;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad url: " + url);
        }
    }

    private XmlDownload(XmlFile xmlFile) {
        this.client = null;
        this.uri = null;
        this.ext = null;
        this.xmlFile = xmlFile;
    }

    public URI getUri() {
        return uri;
    }

    public XmlDownload download() {
        HttpGet request = new HttpGet(uri);

        try (CloseableHttpResponse response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream is = new ByteArrayInputStream(IOUtils.toByteArray(entity.getContent()));
            is = FileDetector.detectCompressed(is);
            xmlFile.setInputStream(is);
        } catch (IOException e) {
            log.error("{}", xmlFile, e);
        }

        return this;
    }

    public void submitDownload() {
        downloadConsumer.accept(this);
    }

    @Override
    public boolean isPoisoned() {
        return false;
    }

    public static Charset guessCharset(InputStream is) throws IOException {
        return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
    }

    public XmlFile getXmlFile() {
        return xmlFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        XmlDownload that = (XmlDownload) o;
        return Objects.equals(xmlFile, that.xmlFile);
    }

    @Override
    public int hashCode() {
        return xmlFile != null ? xmlFile.hashCode() : 0;
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
