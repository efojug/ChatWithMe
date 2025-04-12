package com.efojug.chatwithme;

import android.annotation.SuppressLint;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * @author efojug
 */
public class HttpManager {
    private static volatile OkHttpClient okHttpClient;
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool();

    private static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    @SuppressLint("CustomX509TrustManager")
    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        }};
    }

    private static SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, getTrustManager(), new SecureRandom());
        return sslContext.getSocketFactory();
    }

    private static X509TrustManager getX509TrustManager() throws KeyStoreException, NoSuchAlgorithmException {
        X509TrustManager x509TrustManager;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers: " + Arrays.toString(trustManagers));
        }
        x509TrustManager = (X509TrustManager) trustManagers[0];
        return x509TrustManager;
    }

    public static OkHttpClient getInstance() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        if (okHttpClient == null) {
            synchronized (OkHttpClient.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder().connectionPool(CONNECTION_POOL).sslSocketFactory(getSSLSocketFactory(), getX509TrustManager()).hostnameVerifier(getHostnameVerifier()).connectTimeout(15, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
                    okHttpClient.dispatcher().setMaxRequestsPerHost(200);
                    okHttpClient.dispatcher().setMaxRequests(200);
                }
            }
        }
        return okHttpClient;
    }
}
