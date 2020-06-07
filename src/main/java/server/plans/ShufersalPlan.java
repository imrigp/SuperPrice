package server.plans;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import server.XmlDownload;
import server.XmlFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShufersalPlan extends Plan {
    private static final String STORES_FILE_URL = "http://prices.shufersal.co.il/FileObject/UpdateCategory?catID=5&storeId=0";
    private static final String PRICEFULL_FILES_URL = "http://prices.shufersal.co.il/FileObject/UpdateCategory?catID=2&storeId=0&sort=Time&sortdir=DESC";


    @Override
    protected ArrayList<XmlDownload> getSortedFileList() {
        XmlDownload storeFile = null;
        HttpGet ajax = new HttpGet(STORES_FILE_URL);
        String htmlRes;
        try (CloseableHttpResponse res = getClient().execute(ajax)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                System.out.println("Shufersal request failed:" + res.getStatusLine().getStatusCode());
            }
            htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("<a href=\"http://pricesprodpublic.blob.core.windows.net/stores/(?<url>(?<fname>.+?\\.gz).+)\" target=");
            Matcher m = p.matcher(htmlRes);
            if (m.find()) {
                storeFile = new XmlDownload(getClient(), m.group("url"), new XmlFile(m.group("fname")));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        ArrayList<XmlDownload> xmlList = new ArrayList<>();
        xmlList.add(storeFile);


        int page = 1;
        boolean stop = false;
        do {
            ajax = new HttpGet(PRICEFULL_FILES_URL + "&page=" + page++);
            try (CloseableHttpResponse res = getClient().execute(ajax)) {
                if (res.getStatusLine().getStatusCode() != 200) {
                    System.out.println("Shufersal request failed:" + res.getStatusLine().getStatusCode());
                }
                htmlRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
                if (!htmlRes.contains("page=" + page)) {
                    stop = true;
                    continue;
                }
                Pattern p = Pattern.compile("<a href=\"http://pricesprodpublic.blob.core.windows.net/pricefull/(?<url>(?<fname>.+?\\.gz).+)\" target=");
                Matcher m = p.matcher(htmlRes);
                // Each match is a file, add it to the list
                while (m.find()) {
                    XmlFile file = new XmlFile(m.group("fname"));
                    // Don't include files we already processed
                    if (file.getDate() < lastUpdated) {
                        stop = true;
                        break;
                    }
                    xmlList.add(new XmlDownload(getClient(), m.group("url"), file));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } while (!stop); // While valid page number and current oldest file is newer than lastUpdated

        return xmlList;
    }
}
