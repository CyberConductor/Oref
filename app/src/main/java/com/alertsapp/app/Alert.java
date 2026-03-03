package com.alertsapp.app;

import com.google.gson.annotations.SerializedName;

public class Alert {
    public enum AlertType {
        DANGER,
        CLEAR
    }
    @SerializedName("alertDate")
    private String alertDate;

    @SerializedName("time")
    private String time;

    @SerializedName("data")
    private String city;

    @SerializedName("category_desc")
    private String categoryDesc;

    public String getAlertDate() { return alertDate; }
    public String getTime() { return time != null && time.length() >= 5 ? time.substring(0, 5) : time; }
    public String getCity() { return city; }
    public String getCategoryDesc() { return categoryDesc; }

    public int getHour() {
        try {
            if (time != null && time.length() >= 2) {
                return Integer.parseInt(time.substring(0, 2));
            }
        } catch (Exception ignored) {}
        return -1;
    }

    public boolean isClearEvent() {
        return categoryDesc != null &&
                categoryDesc.trim().endsWith("האירוע הסתיים");
    }

    public String getBaseCategory() {
        if (categoryDesc == null) return "";

        if (isClearEvent()) {
            return categoryDesc.replace(" - האירוע הסתיים", "").trim();
        }

        return categoryDesc.trim();
    }

    public AlertType getAlertType() {
        return isClearEvent() ? AlertType.CLEAR : AlertType.DANGER;
    }
}
