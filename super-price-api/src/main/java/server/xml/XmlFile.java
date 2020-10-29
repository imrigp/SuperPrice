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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;

public class XmlFile implements Comparable<XmlFile> {
    // List every Type enum value, delimited by '|'
    // todo find a better way of doing it
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^(" + Arrays.stream(Type.values()).map(Type::toString).collect(Collectors.joining("|"))
                    + ")\\d", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILE_DATE_PATTERN = Pattern.compile("-(\\d+)\\.");

    public enum Type {
        // The order is important - we want to parse in this order
        STORES("Stores"),
        PRICEFULL("PriceFull"),
        PRICE("Price"),
        PROMOFULL("PromoFull"),
        PROMO("Promo");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private final String name;
    private final long fileDate;
    private String xml;
    private final Type type;
    private final boolean poisonPill;
    public CloseableHttpResponse response;
    private int zIndex;
    private InputStream inputStream;

    private XmlFile() {
        this.name = null;
        this.fileDate = 0;
        this.xml = null;
        this.type = null;
        this.poisonPill = true;
    }

    public XmlFile(String fname, String xml) {
        this.name = fname;
        this.xml = xml;
        this.fileDate = getFileDate(fname);
        this.type = getFileType(fname);
        this.poisonPill = false;
        if (this.fileDate == -1 || this.type == null) {
            throw new IllegalArgumentException("Invalid xml file name:" + fname);
        }
    }

    public XmlFile(String fname) {
        this(fname, null);
    }

    public String getXml() {
        return xml;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Type getType() {
        return type;
    }

    public long getFileDate() {
        return fileDate;
    }

    public boolean isPoisoned() {
        return poisonPill;
    }

    public static XmlFile createPoison() {
        return new XmlFile();
    }

    public void setResponse(CloseableHttpResponse response) {
        this.response = response;
    }

    public InputStream getInputStream() {
        /*String ext = FilenameUtils.getExtension(name);
        if (!ext.equals("xml")) {
            try {
                inputStream = new GzipCompressorInputStream(inputStream);
            } catch (IOException e) {
                System.out.println("ext: " + ext);
                e.printStackTrace();
            }
        }*/
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Optional<InputStream> getResponseInputStream() {
        return Optional.ofNullable(inputStream);
    }

    public static Type getFileType(String fname) {
        Type type = null;
        try {
            Matcher m = FILE_NAME_PATTERN.matcher(fname);
            if (m.find()) {
                type = Type.valueOf(m.group(1).toUpperCase());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // We just return null
        }
        return type;
    }

    // Returns the xml file date, extracted from its name (-1 if not found)
    public static long getFileDate(String fname) {
        long res = -1;
        try {
            Matcher m = FILE_DATE_PATTERN.matcher(fname);
            if (m.find()) {
                res = Long.parseLong(m.group(1));
            }
        } catch (NumberFormatException ignored) {
        } // We just return -1
        return res;
    }

    @Override
    public int compareTo(XmlFile o) {
        // Compare by file type, then date, then upload date
        int res = this.getType().compareTo(o.getType());
        if (res != 0) {
            return res;
        }

        res = Long.compare(this.getFileDate(), o.getFileDate());
        if (res != 0) {
            return res;
        }

        return Integer.compare(this.getzIndex(), o.getzIndex());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        XmlFile other = (XmlFile) o;

        if (zIndex != other.zIndex)
            return false;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + zIndex;
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
