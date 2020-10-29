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

package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Utils {
    private static final DateFormat DATE_FORMAT_24H = new SimpleDateFormat("yyyyMMddHH24");

    public static long convertUnixTo24H(long unix) {
        return Long.parseLong(DATE_FORMAT_24H.format(unix));
    }
}
