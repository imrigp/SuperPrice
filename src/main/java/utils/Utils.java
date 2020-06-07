package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class Utils {
    private static final DateFormat DATE_FORMAT_24H = new SimpleDateFormat("yyyyMMddHH24");

    public static long convertUnixTo24H(long unix) {
        return Long.parseLong(DATE_FORMAT_24H.format(unix));
    }
}
