package server.plans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Xml.XmlDownload;
import server.Xml.XmlFile;

//todo Add a head request to check connection to speed things up

//todo Cerberus allows zipping with: https://url.publishedprices.co.il/file/ajax_download_zip/archive.zip
// need to pass file names with parameter name "ID" (ID=fname1&ID=fname2...). Test if better (use several threads)

public class CerberusPlan extends Plan {
    private static final Logger log = LoggerFactory.getLogger(CerberusPlan.class);
    private static final String HANDSHAKE_URL = "https://url.publishedprices.co.il";
    private static final String LOGIN_URL = "https://url.publishedprices.co.il/login/user";
    private static final String FILES_URL = "https://url.publishedprices.co.il/file/ajax_dir";
    private static final String BASE_FILE_URL = "https://url.publishedprices.co.il/file/d/";

    private final String username;

    public CerberusPlan(String name, String username) {
        super(name);
        this.username = username;
    }

    public CerberusPlan(String username) {
        super(username);
        this.username = username;
    }

    public boolean connect() {
        HttpGet req = new HttpGet(HANDSHAKE_URL);
        try (CloseableHttpResponse response = getClient().execute(req)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error("connection error: {}", response.getStatusLine());
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

    // Returns a list of files added after our last updated date, ordered by ascending date
    protected List<XmlDownload> getSortedFileList(long fromTime) {
        HttpPost ajax = getHttpPost();
        JsonNode arrayNode;

        try (CloseableHttpResponse res = getClient().execute(ajax)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                // reconnect and try again
                if (connect()) {
                    return getSortedFileList(fromTime);
                } else {
                    System.err.println("ERROR in connection");
                    return null;
                }
            }

            // Convert json response string to map
            ObjectMapper objectMapper = new ObjectMapper();
            arrayNode = objectMapper.readTree(res.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Files reside under this node, make sure it's an array
        JsonNode arr = arrayNode.get("aaData");
        assert arr.isArray() : "aaData is not an array in response:\n" + ajax.getURI();

        // Create a list containing new files to process
        List<XmlDownload> xmlList = new ArrayList<>(arr.size());
        int zIndex = 0;
        for (JsonNode jsonNode : arr) {
            String fname = jsonNode.get("fname").textValue();
            XmlFile xmlFile;
            try {
                xmlFile = new XmlFile(fname);
            } catch (IllegalArgumentException e) {
                log.error("", e);
                continue; // Not valid xml file name
            }

            // Don't include old files, but always include stores
            if (xmlFile.getFileDate() < fromTime && xmlFile.getType() != XmlFile.Type.STORES) {
                continue;
            }

            String url = BASE_FILE_URL + fname;
            xmlFile.setzIndex(zIndex++);
            xmlList.add(new XmlDownload(getClient(), url, xmlFile, this::addDownload));
        }

        Collections.sort(xmlList);
        return xmlList.subList(0, xmlList.size());
    }

    private HttpPost getHttpPost() {
        HttpPost ajax = new HttpPost(FILES_URL);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("sEcho", "1"));
        params.add(new BasicNameValuePair("iColumns", "5"));
        params.add(new BasicNameValuePair("iDisplayStart", "0"));
        params.add(new BasicNameValuePair("iDisplayLength", "100000"));
        params.add(new BasicNameValuePair("iSortCol_0", "3")); // date column
        params.add(new BasicNameValuePair("sSortDir_0", "asc"));
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
