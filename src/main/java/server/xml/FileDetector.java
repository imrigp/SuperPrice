package server.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;

public class FileDetector {
    private static final Detector detector = new DefaultDetector();
    private static final Metadata metaData = new Metadata();

    private FileDetector() {
    }

    // Wraps the InputStream with decompressing one if needed
    public static InputStream detectCompressed(InputStream is) throws IOException {
        is = TikaInputStream.get(is);
        String type = detector.detect(is, metaData).toString();
        return switch (type) {
            case "application/gzip" -> new GzipCompressorInputStream(is);
            case "application/zip" -> {
                ZipInputStream zis = new ZipInputStream(is);
                zis.getNextEntry();
                yield zis;
            }
            case "text/plain" -> is;
            default -> throw new IOException("Couldn't detect file type.");
        };
    }
}
