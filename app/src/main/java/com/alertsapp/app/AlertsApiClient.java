package com.alertsapp.app;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlertsApiClient {

    private static final String BASE_URL = "https://alerts-history.oref.org.il/Shared/Ajax/GetAlarmsHistory.aspx";
    private final OkHttpClient client;
    private final Gson gson;

    public interface AlertsCallback {
        void onSuccess(List<Alert> alerts);
        void onError(String message);
    }

    public AlertsApiClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public void fetchAlerts(String city, int mode, String fromDate, String toDate, AlertsCallback callback) {
        new Thread(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                        .append("?lang=he")
                        .append("&city_0=").append(java.net.URLEncoder.encode(city, "UTF-8"));

                if (mode == 3) {
                    urlBuilder.append("&mode=0");
                    urlBuilder.append("&fromDate=").append(java.net.URLEncoder.encode(fromDate, "UTF-8"));
                    urlBuilder.append("&toDate=").append(java.net.URLEncoder.encode(toDate, "UTF-8"));
                } else {
                    urlBuilder.append("&mode=").append(mode);
                }

                Request request = new Request.Builder()
                        .url(urlBuilder.toString())
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .addHeader("Referer", "https://alerts-history.oref.org.il/")
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    callback.onError("שגיאת שרת: " + response.code());
                    return;
                }

                String body = response.body().string();

                if (body == null || body.trim().isEmpty() || body.trim().equals("null")) {
                    callback.onSuccess(new java.util.ArrayList<>());
                    return;
                }

                Type listType = new TypeToken<List<Alert>>() {}.getType();
                List<Alert> alerts = gson.fromJson(body, listType);
                callback.onSuccess(alerts != null ? alerts : new java.util.ArrayList<>());

            } catch (IOException e) {
                callback.onError("שגיאת רשת: " + e.getMessage());
            } catch (Exception e) {
                callback.onError("שגיאה: " + e.getMessage());
            }
        }).start();
    }
}
