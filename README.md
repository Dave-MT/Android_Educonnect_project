#1 EduConnect Android App

`<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue.svg" alt="Min SDK">
  <img src="https://img.shields.io/badge/Target%20SDK-34-blue.svg" alt="Target SDK">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</div>`## 📱 Overview

#2 🎯 Key Features

- **🔐 Secure Authentication** - JWT-based login with role-based access control
- **📝 Assignment Management** - Create, submit, and track assignments with file attachments
- **📚 Resource Sharing** - Upload and download educational materials
- **📊 Grade Tracking** - View grades and feedback in real-time
- **💬 Discussion Forums** - Class-based messaging and communication
- **👤 Profile Management** - User profile viewing and editing
- **📱 Role-Based UI** - Adaptive interface for teachers and students


#3 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or later
- Android SDK API 26 or higher
- Java Development Kit (JDK) 11
- Active internet connection for API communication
-  Gradel version--distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip
  


### Installation

1. **Clone the repository**

```shellscript
git clone https://github.com/Dave-MT/Android_Educonnect_project.git
cd educonnect-android
```


2. **Open in Android Studio**

1. Launch Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the cloned directory and select it



3. **Configure API Base URL**

```java
// In app/src/main/java/com/educonnect/utils/UrlConfig.java
private static final String DEFAULT_BASE_URL = "https://your-api-domain.com/educonnect/api/";
if you use linux use command= ngrok http 80 ,and  copy the Forwarding url  and paste  to private static final String DEFAULT_BASE_URL = "https://paste here /educonnect/api/"
```


4. **Build and Run**

1. Build and Sync project with Gradle files
2. Connect an Android device or start an emulator 
3. Click "Run" or press `Ctrl+R`





### 📋 Demo Credentials

**Teacher Account:**

- Email: `dagi@gmail.com`
- Password: `pass12345`


**Student Account:**

- Email: `yoni@gmail.com`
- Password: `pass123`


## 🏗️ Architecture

### Tech Stack

- **Language:** Java
- **UI Framework:** Android SDK with Material Design
- **Networking:** Retrofit 2.x + OkHttp
- **JSON Parsing:** Gson
- **Authentication:** JWT Tokens
- **File Handling:** Android File Provider


### Project Structure

```plaintext
app/
├── src/main/java/com/educonnect/
│   ├── adapter/              # RecyclerView adapters
│   ├── api/                  # API service interfaces
│   ├── model/                # Data models and POJOs
│   ├── utils/                # Utility classes
│   ├── MainActivity.java     # App entry point
│   ├── LoginActivity.java    # Authentication
│   ├── BaseActivity.java     # Common functionality
│   ├── Student*.java         # Student-specific activities
│   └── Teacher*.java         # Teacher-specific activities
├── src/main/res/
│   ├── layout/               # XML layout files
│   ├── values/               # Colors, strings, styles
│   └── drawable/             # Icons and graphics
└── AndroidManifest.xml       # App configuration
```

## 👥 User Roles & Features

### 👨‍🏫 Teacher Features

| Feature | Description
|-----|-----
| **Assignment Management** | Create, edit, delete assignments with file attachments
| **Resource Management** | Upload, organize, and manage educational resources
| **Grade Management** | Grade student submissions and provide feedback
| **Discussion Moderation** | Participate in and moderate class discussions
| **Student Progress** | View submission status and student performance


### 👨‍🎓 Student Features

| Feature | Description
|-----|-----
| **Assignment Submission** | View assignments and submit work with files
| **Resource Access** | Download and view educational materials
| **Grade Viewing** | Check grades and read teacher feedback
| **Discussion Participation** | Engage in class discussions
| **Progress Tracking** | Monitor submission status and deadlines


## 🔧 Configuration

### API Configuration

Update the base URL in `UrlConfig.java`:

```java
public class UrlConfig {
    private static final String DEFAULT_BASE_URL = "https://your-domain.com/educonnect/api/";
    
    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        return prefs.getString("base_url", DEFAULT_BASE_URL);
    }
}
```

### Network Security

Add network security configuration in `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">your-api-domain.com</domain>
    </domain-config>
</network-security-config>
```


