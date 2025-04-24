# 🌱 SmartPlantApp

This is a final-year university project titled **"IoT-Based Smart Plant Monitoring System"**, designed to help users track and manage the soil moisture levels of indoor plants using low-cost IoT components. The system consists of:

- A **D1 Mini ESP8266 microcontroller** with a capacitive soil moisture sensor
- A **Google Sheets backend** via Apps Script for data logging and retrieval
- A **Java-based Android app** that displays real-time soil moisture data and sends push notifications when watering is needed

---

## 📁 Repository Structure

SmartPlantApp/
├── android-app/            # Android mobile app (Java)
│   ├── app/
│   ├── res/
│   ├── manifest/
│   └── build.gradle
│
├── ESP8266+Apps Script/       # Arduino code for the ESP8266 microcontroller
│   └── ESP8266_Firmware
│   └── Google_Apps_Script
│   └── README.md

---

## ⚙️ System Components & Technologies

- **ESP8266 D1 Mini**: Reads soil moisture from the DFRobot SEN0193 sensor and sends data over Wi-Fi.
- **Google Sheets + Apps Script**: Logs data and serves it in JSON format to the app.
- **Android App**: Built in Java using Android Studio; fetches moisture data, allows threshold setting, and triggers notifications.

---

## 🚀 Getting Started

### 📲 Android App
1. Open `android-app` in Android Studio.
2. Update the API URL in the Java code (`MainActivity.java`) to match your deployed Google Apps Script Web App URL.
3. Build and run the app on a physical or virtual device (API 26+ recommended).

### 🔌 ESP8266 Firmware
1. Open the `.ino` file from `ESP8266_Firmware` in the Arduino IDE.
2. Install the **ESP8266 board package** via Board Manager.
3. Set your Wi-Fi credentials and Google Apps Script endpoint in the `setup()` function.
4. Upload the sketch to your D1 Mini board.

### ☁️ Google Apps Script
1. Create a new Google Apps Script project and paste the contents of `Google_Apps_Script/Code.gs`.
2. Link it to a Google Sheet and deploy as a Web App.
3. Copy the deployment URL and use it in both the ESP8266 and Android app code.

---

## 🔔 Features

- Real-time soil moisture updates (every 15 minutes)
- Custom notification thresholds (0–100%)
- Push notifications on low moisture
- Manual and automatic data refresh
- Light/Dark theme toggle
- Offline recovery support

---

## 🧪 Calibration Notes

The sensor values are calibrated using:
- 653 → 0% (dry)
- 570 → 100% (wet)

Use the formula in code:
```cpp
int moisture = map(raw, 653, 570, 0, 100);
