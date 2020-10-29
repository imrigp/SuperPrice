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
