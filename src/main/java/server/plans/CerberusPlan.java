package server.plans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import server.XmlDownload;
import server.XmlFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/* todo: Add a head request to check connection to speed things up

 * todo: Cerberus allows zipping with: https://url.publishedprices.co.il/file/ajax_download_zip/archive.zip
 * todo: need to pass file names with parameter name "ID" (ID=fname1&ID=fname2...). Test if better (use several threads)

 */

public class CerberusPlan extends Plan {
    private static final String HANDSHAKE_URL = "https://url.publishedprices.co.il";
    private static final String LOGIN_URL = "https://url.publishedprices.co.il/login/user";
    private static final String FILES_URL = "https://url.publishedprices.co.il/file/ajax_dir";
    private static final String BASE_FILE_URL = "https://url.publishedprices.co.il/file/d/";


    private String username;

    public CerberusPlan(String username) {
        this.username = username;
    }

    public boolean connect() {
        HttpGet req = new HttpGet(HANDSHAKE_URL);
        try (CloseableHttpResponse response = getClient().execute(req)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println(response.getStatusLine());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        HttpPost loginReq = prepareLoginRequest();
        if (loginReq == null) {
            return false;
        }
        try (CloseableHttpResponse res = getClient().execute(loginReq)) {
            // Here we want redirected return status, as it's an indication of successful login
            if (res.getStatusLine().getStatusCode() != 302) {
                System.out.println(res.getStatusLine());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private HttpPost prepareLoginRequest() {
        HttpPost loginReq = new HttpPost(LOGIN_URL);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        try {
            loginReq.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return loginReq;
    }

    @Override
    public void scanForFiles() {
        List<XmlDownload> list = getSortedFileList();
        if (list == null) {
            return;
        }
        // update latest file date
        this.lastUpdated = list.get(list.size() - 1).getXmlFile().getDate();
        System.out.println(lastUpdated);
        // Add files to queue, let downloader threads take it from here
        list.forEach(this::addToQueue);

        // Add poisons to queue to notify threads they are done
        /*for (int i = 0; i < getThreadNumber(); i++) {
            addToQueue(XmlDownload.createPoison());
        }*/
    }

    // Returns a list of files added after our last updated date, ordered by ascending date
    private ArrayList<XmlDownload> getSortedFileList() {
        HttpPost ajax = getHttpPost();
        String jsonRes;
        JsonNode arrayNode;
        try (CloseableHttpResponse res = getClient().execute(ajax)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                System.out.println("CerberusPlan.ScanForFiles: " + res.getStatusLine());
                // reconnect and try again
                if (connect()) {
                    return getSortedFileList();
                } else {
                    return null;
                }
            }
            jsonRes = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
            // Convert json response string to map
            ObjectMapper objectMapper = new ObjectMapper();
            arrayNode = objectMapper.readTree(jsonRes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Files reside under this node, make sure it's an array
        JsonNode arr = arrayNode.get("aaData");
        if (!arr.isArray()) {
            System.err.println("aaData is not an array in response:\n" + jsonRes);
            return null;
        }
        // Create a list containing new files to process
        ArrayList<XmlDownload> xmlList = new ArrayList<>(arr.size());
        xmlList.add(XmlDownload.createSentinel());
        XmlDownload storeFile = null;
        for (JsonNode jsonNode : arr) {
            String fname = jsonNode.get("fname").textValue();
            XmlFile xmlFile;
            try {
                xmlFile = new XmlFile(fname);
            } catch (IllegalArgumentException e) {
                continue; // Not valid xml file name
            }
            // Don't include files we (potentially) already processed
            if (xmlFile.getDate() < lastUpdated) {
                continue;
            }

            String url = BASE_FILE_URL + fname;
            // We want the stores file to be the first, so we add it after the sorting
            if (xmlFile.getType() == XmlFile.Type.STORES) {
                storeFile = new XmlDownload(getClient(), url, xmlFile);
            } else {
                xmlList.add(new XmlDownload(getClient(), url, xmlFile));
            }
        }
        // Sort the list (by date), then add the store file to be the first
        Collections.sort(xmlList);
        xmlList.set(0, storeFile);
        return xmlList;
    }

    private HttpPost getHttpPost() {
        HttpPost ajax = new HttpPost(FILES_URL);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("sEcho", "1"));
        params.add(new BasicNameValuePair("iColumns", "5"));
        params.add(new BasicNameValuePair("iDisplayStart", "0"));
        params.add(new BasicNameValuePair("iDisplayLength", "100000"));
        params.add(new BasicNameValuePair("iSortCol_0", "3")); // date column
        params.add(new BasicNameValuePair("sSortDir_0", "desc"));
        params.add(new BasicNameValuePair("iSortingCols", "1"));
        try {
            ajax.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return ajax;
    }
}
