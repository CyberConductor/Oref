package com.alertsapp.app;

import java.util.*;

public class AlertAnalyzer {

    public static class HourStat {
        public int hour;
        public int count;
        public double percentage;
        public String label;

        public HourStat(int hour, int count, double percentage) {
            this.hour = hour;
            this.count = count;
            this.percentage = percentage;
            this.label = String.format("%02d:00", hour);
        }
    }

    public static class AnalysisResult {
        public List<HourStat> hourStats;
        public List<HourStat> top5DangerHours;
        public List<HourStat> top5SafeHours;
        public int totalAlerts;
        public int peakHour;
        public String riskSummary;
    }

    /**
     * Analyzes a list of alerts and returns statistics per hour of day.
     */
    public static AnalysisResult analyze(List<Alert> alerts) {
        int[] hourCounts = new int[24];
        int total = 0;

        for (Alert alert : alerts) {

            if (alert.getAlertType() != Alert.AlertType.DANGER) {
                continue;
            }

            int hour = alert.getHour();
            if (hour >= 0 && hour < 24) {
                hourCounts[hour]++;
                total++;
            }
        }

        List<HourStat> allStats = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            double pct = total > 0 ? (hourCounts[h] * 100.0 / total) : 0;
            allStats.add(new HourStat(h, hourCounts[h], pct));
        }
        List<HourStat> sorted = new ArrayList<>(allStats);
        sorted.sort((a, b) -> b.count - a.count);

        int peakHour = sorted.isEmpty() ? -1 : sorted.get(0).hour;
        List<HourStat> top5Danger = sorted.subList(0, Math.min(5, sorted.size()));
        List<HourStat> sortedAsc = new ArrayList<>(allStats);
        sortedAsc.sort(Comparator.comparingInt(a -> a.count));
        List<HourStat> top5Safe = sortedAsc.subList(0, Math.min(5, sortedAsc.size()));
        String riskSummary = buildRiskSummary(top5Danger, total, peakHour);

        AnalysisResult result = new AnalysisResult();
        result.hourStats = allStats;
        result.top5DangerHours = top5Danger;
        result.top5SafeHours = top5Safe;
        result.totalAlerts = total;
        result.peakHour = peakHour;
        result.riskSummary = riskSummary;

        return result;
    }

    private static String buildRiskSummary(List<HourStat> top5, int total, int peakHour) {
        if (total == 0) return "אין נתוני התראות לניתוח";

        StringBuilder sb = new StringBuilder();
        sb.append("סה\"כ ").append(total).append(" התראות נותחו.\n\n");

        if (peakHour >= 0) {
            sb.append("⚠️ שעת השיא המסוכנת ביותר: ").append(String.format("%02d:00", peakHour)).append("\n\n");
        }

        sb.append("5 השעות המסוכנות ביותר:\n");
        for (int i = 0; i < top5.size(); i++) {
            HourStat s = top5.get(i);
            if (s.count == 0) break;
            sb.append(String.format("%d. %02d:00 — %d התראות (%.1f%%)\n", i + 1, s.hour, s.count, s.percentage));
        }

        return sb.toString();
    }
    public static List<EventDuration> calculateDurations(List<Alert> alerts) {
        alerts.sort((a, b) -> {
            String da = a.getAlertDate() != null ? a.getAlertDate() : "";
            String db = b.getAlertDate() != null ? b.getAlertDate() : "";
            int cmp = da.compareTo(db);
            if (cmp != 0) return cmp;

            String ta = a.getTime() != null ? a.getTime() : "";
            String tb = b.getTime() != null ? b.getTime() : "";
            return ta.compareTo(tb);
        });
        List<EventDuration> result = new ArrayList<>();
        Map<String, Alert> openEvents = new HashMap<>();

        for (Alert alert : alerts) {

            String key = alert.getBaseCategory() + "_" + alert.getCity();

            if (alert.getAlertType() == Alert.AlertType.DANGER) {
                openEvents.put(key, alert);
            }

            else if (alert.getAlertType() == Alert.AlertType.CLEAR) {

                if (openEvents.containsKey(key)) {

                    Alert startAlert = openEvents.get(key);

                    long duration = calculateMinutesDifference(
                            startAlert.getTime(),
                            alert.getTime()
                    );

                    result.add(new EventDuration(
                            alert.getBaseCategory(),
                            alert.getCity(),
                            startAlert.getTime(),
                            alert.getTime(),
                            duration
                    ));

                    openEvents.remove(key);
                }
            }
        }

        return result;
    }

    private static long calculateMinutesDifference(String start, String end) {

        try {
            int startHour = Integer.parseInt(start.substring(0, 2));
            int startMin = Integer.parseInt(start.substring(3, 5));

            int endHour = Integer.parseInt(end.substring(0, 2));
            int endMin = Integer.parseInt(end.substring(3, 5));

            int startTotal = startHour * 60 + startMin;
            int endTotal = endHour * 60 + endMin;

            if (endTotal < startTotal) {
                endTotal += 24 * 60;
            }

            return endTotal - startTotal;

        } catch (Exception e) {
            return 0;
        }
    }
}
