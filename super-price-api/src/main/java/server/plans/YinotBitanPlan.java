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

package server.plans;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import server.xml.XmlDownload;
import server.xml.XmlFile;

public class YinotBitanPlan extends Plan {

    private static final String BASE_URL = "http://publishprice.ybitan.co.il/";
    private static final String ALL_FILES_URL = BASE_URL + "/%s/?C=M;O=A"; // template: yyyymmdd

    public YinotBitanPlan(String name) {
        super(name);
    }

    @Override
    protected List<XmlDownload> getSortedFileList(long fromTime) {
        final List<XmlDownload> xmlList = new ArrayList<>();
        final LinkedHashSet<XmlDownload> xmlSet = new LinkedHashSet<>();

        String date = getNowDateFormat();
        String url = String.format(ALL_FILES_URL, date);
        HttpGet ajax = new HttpGet(url);
        String htmlRes;

        try (CloseableHttpResponse res = getClient().execute(ajax)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                System.out.println(name + " request failed:" + res.getStatusLine().getStatusCode());
                return xmlList;
            }

            htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("<a href=\"(?<fname>.+?\\.(?:gz|xml))\"");
            Matcher m = p.matcher(htmlRes);
            int zIndex = 0;
            String urlTemplate = BASE_URL + date + "/";
            // Each match is a file, add it to the list
            while (m.find()) {
                String fname = m.group("fname");
                XmlFile file = new XmlFile(fname);
                // Don't include old file
                if (file.getFileDate() < fromTime) {
                    break;
                }
                String fileUrl = urlTemplate + fname;
                file.setzIndex(zIndex++);
                xmlSet.add(new XmlDownload(getClient(), fileUrl, file, this::addDownload));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        xmlList.addAll(xmlSet);
        Collections.sort(xmlList);
        return xmlList;
    }

    // Returns date in yyyymmdd format
    private static String getNowDateFormat() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(today.getTime());
    }
}
