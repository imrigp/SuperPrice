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
