package server;

import java.util.HashSet;
import java.util.Set;

import database.DbQuery;

public final class Utils {
    private Utils() {
        // restrict instantiation
    }

    private static final DbQuery db = DbQuery.getInstance();
    private static final Set<String> measures = new HashSet<>();
    private static boolean changed;

    public static void addMeasure(String str) {
        if (measures.add(str)) {
            changed = true;
        }
    }

    public static void updateMeasures() {
        if (changed) {
            db.updateMeasures(measures);
            changed = false;
        }
    }
}
