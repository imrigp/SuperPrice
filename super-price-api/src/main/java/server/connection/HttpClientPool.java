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
