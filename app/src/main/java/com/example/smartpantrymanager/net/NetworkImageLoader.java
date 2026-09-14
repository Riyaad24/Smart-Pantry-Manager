package com.example.smartpantrymanager.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Standard zero-dependency high-fidelity asynchronous network image loading utility.
 * Downloads food graphics byte streams via standard libraries, converting them to clean Bitmaps
 * and updating targeted layouts seamlessly on the main UI dispatcher thread.
 */
public class NetworkImageLoader {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void displayImage(final String urlString, final ImageView imageView) {
        if (urlString == null || urlString.trim().isEmpty() || imageView == null) {
            return;
        }

        executor.execute(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        mainHandler.post(() -> imageView.setImageBitmap(bitmap));
                    }
                }
            } catch (Exception e) {
                // Fail gracefully without interrupting local container execution workflows
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception ignored) {}
            }
        });
    }
}