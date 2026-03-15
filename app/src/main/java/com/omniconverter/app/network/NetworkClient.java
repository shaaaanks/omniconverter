package com.omniconverter.app.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkClient {

    // Lightweight GET helper using HttpURLConnection.
    // TODO: Replace with Retrofit + OkHttp once dependencies are finalized.
    public static byte[] simpleGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.connect();
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } finally {
            conn.disconnect();
        }
    }
}
