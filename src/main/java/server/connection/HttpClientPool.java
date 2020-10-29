package server.connection;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

public class HttpClientPool {

    private static PoolingHttpClientConnectionManager connManager;

    // Call client.close() to release
    public static CloseableHttpClient getClient() {
        // returns singleton thread safe client
        if (connManager == null) {
            // Big hack to bypass cert validation, due to Cerberus expired cert
            SSLContext sslContext = null;
            try {
                sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                        .build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            connManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https", new SSLConnectionSocketFactory(sslContext,
                                    NoopHostnameVerifier.INSTANCE))
                            .build()
            );
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(15);
        }

        return HttpClients.custom()
                          .setConnectionManagerShared(true)
                          .setConnectionManager(connManager)
                          .build();
    }

    public static void shutdown() {
        connManager.close();
    }
}
