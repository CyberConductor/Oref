# AlertsApp – Real-Time Rocket and Safety Alerts

**Purpose:**
AlertsApp was created to provide Israeli citizens with **real-time information about rocket and missile alerts**, including critical updates during security escalations such as recent operations against Iran and associated threats. The app helps users understand where threats may be coming from, their severity, and when it is safe to exit protected areas.


## Features

1. **Live Alerts Feed**

   * Displays incoming alerts by city and category.
   * Differentiates between **dangerous events** (active threats) and **clear notifications** (events that have ended, safe to resume normal activity).
   * Alerts are colour-coded:

     * Red for danger
     * Green for cleared events
     * Orange for intermediate / low-risk alerts

2. **Analysis and Statistics**

   * Tracks alerts over time and highlights **peak hours of risk**.
   * Shows **top 5 most dangerous hours** and **safest hours** for planning daily activities.
   * Provides a **risk summary** for quick understanding.

3. **Bar Chart Visualization**

   * Displays alert frequency by hour.
   * High-risk hours in **red**, cleared periods in **green**, moderate hours in **orange**.
   * Helps users quickly identify trends and patterns.

4. **Customizable Timeframes**

   * Fetch alerts for the **last day, week, month, or custom date ranges**.
   * Flexible search by city.

5. **User-Friendly Interface**

   * Tab-based design for **Analysis** and **Alerts**.
   * Interactive charts and clean layout for quick comprehension.
   * Supports RTL (Right-to-Left) layout for Hebrew users.


## Why This App Was Created

During recent escalations involving Israeli operations against Iran, there have been:

* Rocket launches and missile threats towards northern, central, and southern Israel.
* Rapid-response alerts requiring citizens to enter protected spaces within **minutes**.
* Ongoing need for **real-time, localised information** to ensure public safety.

AlertsApp was designed to bridge the gap between raw security alerts and actionable information:

* **Shows where rockets are likely coming from** based on the alert type and city.
* **Highlights ongoing threats vs cleared threats**, reducing panic and confusion.
* Provides **historical trends** to help citizens understand patterns during escalations.

## Installation

1. Clone this repository.
2. Open in Android Studio.
3. Build and run on a device with internet access.
4. Enter a city, select a timeframe, and start receiving alerts.

## Data Sources

* Alerts are fetched from **official APIs** and verified news sources reporting rocket launches, missile threats, and civil defense warnings.
* Special focus on **recent operations against Iran** and related rocket activity affecting Israeli cities.

## Disclaimer

This app provides **informational alerts** and **statistical analysis**. It is not a replacement for official emergency systems or civil defense instructions. Always follow instructions from **Pikud HaOref (Israeli Home Front Command)** during active alerts.
