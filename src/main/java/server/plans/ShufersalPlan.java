package server.plans;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import server.Xml.XmlDownload;
import server.Xml.XmlFile;

public class ShufersalPlan extends Plan {
    private static final String STORES_FILE_URL = "http://prices.shufersal.co.il/FileObject/UpdateCategory?catID=5&storeId=0";
    private static final String PRICEFULL_FILES_URL = "http://prices.shufersal.co.il/FileObject/UpdateCategory?catID=2&sort=Time&sortdir=DESC";
    private static final String ALL_FILES_URL = "http://prices.shufersal.co.il/FileObject/UpdateCategory?sort=Time&sortdir=DESC";

    // Shufersal has over 400 stores, so unless in production, it's not feasible to parse all of them.
    // For now I'll choose a handful. if List is empty, it'll parse all
    // 623 - Yehud BE, 93 - Yehud Mall, 205 - Yehud BIG, 207 - Or Yehuda, 183 - Ashdod
    private static final List<Integer> storeIds = List.of(623, 93, 205, 207, 183);

    public ShufersalPlan(String name) {
        super(name);
    }

    @Override
    protected List<XmlDownload> getSortedFileList(long fromTime) {
        List<XmlDownload> xmlList = new ArrayList<>();
        HttpGet ajax = new HttpGet(STORES_FILE_URL);
        String htmlRes;

        // get stores file first
        try (CloseableHttpResponse res = getClient().execute(ajax)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                System.out.println("Shufersal request failed:" + res.getStatusLine().getStatusCode());
            }
            htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);

            Pattern p = Pattern.compile("<a href=\"(?<url>http://pricesprodpublic.blob.core.windows.net/stores/(?<fname>.+?\\.gz).+)\" target=");
            Matcher m = p.matcher(htmlRes);
            if (m.find()) {
                XmlDownload storeFile = new XmlDownload(
                        getClient(), m.group("url"), new XmlFile(m.group("fname")), this::addDownload);

                xmlList.add(storeFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (storeIds.isEmpty()) {
            getAllStores(xmlList, fromTime);
        } else {
            getSpecificStores(xmlList, fromTime);
        }

        Collections.sort(xmlList);
        return xmlList;
    }

    private void getSpecificStores(List<XmlDownload> xmlList, long fromTime) {
        for (Integer storeId : storeIds) {
            String url = ALL_FILES_URL + "&storeId=" + storeId + "&page=";
            int zIndex = 0;
            int page = 1;
            boolean stop = false;
            do
            {
                HttpGet ajax = new HttpGet(url + page++);
                try (CloseableHttpResponse res = getClient().execute(ajax)) {
                    if (res.getStatusLine().getStatusCode() != 200) {
                        System.out.println("Shufersal request failed:" + res.getStatusLine().getStatusCode());
                    }

                    String htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);

                    Pattern p = Pattern.compile("<a href=\"(?<url>http://pricesprodpublic.blob.core.windows.net/.+?/(?<fname>.+?\\.gz).+)\" target=");
                    Matcher m = p.matcher(htmlRes);
                    // Each match is a file, add it to the list
                    while (m.find()) {
                        XmlFile file = new XmlFile(m.group("fname"));
                        // Don't include old file
                        if (file.getFileDate() < fromTime) {
                            stop = true;
                            break;
                        }
                        file.setzIndex(zIndex++);
                        xmlList.add(new XmlDownload(getClient(), m.group("url"), file, this::addDownload));
                    }

                    if (!htmlRes.contains("page=" + page)) {
                        stop = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } while (!stop); // While valid page number and current oldest file is newer than lastUpdated
        }
    }

    private void getAllStores(List<XmlDownload> xmlList, long fromTime) {
        // get the rest of the files, ordered by date, until we reach an old enough file
        int zIndex = 0;
        int page = 1;
        boolean stop = false;
        do
        {
            HttpGet ajax = new HttpGet(PRICEFULL_FILES_URL + "&page=" + page++);
            try (CloseableHttpResponse res = getClient().execute(ajax)) {
                if (res.getStatusLine().getStatusCode() != 200) {
                    System.out.println("Shufersal request failed:" + res.getStatusLine().getStatusCode());
                }
                String htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
                if (!htmlRes.contains("page=" + page)) {
                    stop = true;
                    continue;
                }
                Pattern p = Pattern.compile("<a href=\"http://pricesprodpublic.blob.core.windows.net/pricefull/(?<url>(?<fname>.+?\\.gz).+)\" target=");
                Matcher m = p.matcher(htmlRes);
                // Each match is a file, add it to the list
                while (m.find()) {
                    XmlFile file = new XmlFile(m.group("fname"));
                    // Don't include old file
                    if (file.getFileDate() < fromTime) {
                        stop = true;
                        break;
                    }
                    file.setzIndex(zIndex);
                    xmlList.add(new XmlDownload(getClient(), m.group("url"), file, this::addDownload));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } while (!stop); // While valid page number and current oldest file is newer than lastUpdated
    }
}
