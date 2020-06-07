package server;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XmlFile implements Comparable<XmlFile> {
    // List every Type enum value, delimited by '|'
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^(" + Arrays.stream(Type.values()).map(Type::toString).collect(Collectors.joining("|"))
                    + ")\\d", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILE_DATE_PATTERN = Pattern.compile("-(\\d+)\\.");


    public enum Type {
        PRICE("Price"),
        PRICEFULL("PriceFull"),
        PROMO("Promo"),
        PROMOFULL("PromoFull"),
        STORES("Stores");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private String name;
    private long date;
    private String xml;
    private Type type;
    private boolean poisonPill;

    private XmlFile() {
        this.name = null;
        this.date = 0;
        this.xml = null;
        this.type = null;
        this.poisonPill = true;
    }

    public XmlFile(String fname, String xml) {
        this.name = fname;
        this.xml = xml;
        this.date = getFileDate(fname);
        this.type = getFileType(fname);
        this.poisonPill = false;
        if (this.date == -1 || this.type == null) {
            throw new IllegalArgumentException("Invalid xml file name:" + fname);
        }
    }

    public XmlFile(String fname) {
        this(fname, null);
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Type getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public boolean isPoisoned() {
        return poisonPill;
    }

    public static XmlFile createPoison() {
        return new XmlFile();
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
        } catch (NumberFormatException ignored) {} // We just return -1
        return res;
    }

    @Override
    public int compareTo(XmlFile o) {
        return Long.compare(this.getDate(), o.getDate());
    }

    @Override
    public String toString() {
        return name;
    }
}
