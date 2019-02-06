package com.ludei.devapplib.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by imanolmartin on 21/03/14.
 */
public class NetworkUtils {

    public static class MyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    public static boolean isHostAvailable(String url) {
        try {
            URI uri = new URI(url);
            int port = uri.getPort() == -1 ? 80 : uri.getPort();
            SocketAddress sockAddr = new InetSocketAddress(uri.getHost(), port);
            Socket sock = new Socket();
            int timeoutMs = 2000;
            sock.connect(sockAddr, timeoutMs);
            return true;

        } catch(Exception e){
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
