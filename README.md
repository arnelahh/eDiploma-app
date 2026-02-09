# 🎓 eDiploma App

A desktop application designed to streamline the Bachelor's thesis defense process for university faculties. By digitalizing the application workflow, it eliminates manual paperwork, automates document generation, and manages student records efficiently.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-purple?style=flat-square)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)
![Database](https://img.shields.io/badge/Database-MySQL-003545?style=flat-square&logo=mysql)
![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square&logo=windows)

## 📖 About

**eDiploma** resolves the administrative bottlenecks associated with the thesis defense application process. Instead of managing scattered paper trails, faculty staff can use this centralized desktop client to track student progress, generate official PDF forms, and notify parties via email.

The application features a modern, responsive UI built with **JavaFX**, styled with **BootstrapFX**, and utilizes **MySQL** for robust data persistence.

## ✨ Key Features

* **📄 Automated PDF Generation:** Instantly generate official defense documents and forms using *OpenHTMLtoPDF*.
* **👥 Student Lifecycle Management:** specialized workflows for Bachelor's thesis applicants.
* **📧 Integrated Email System:** Send notifications and documents directly from the app via *JavaMail*.
* **🔐 Admin Security:** Secure login and role-based access for faculty staff.
* **💾 Robust Database:** High-performance connection pooling via *HikariCP* connecting to *MySQL*.
* **🎨 Modern UI/UX:** utilizing *ControlsFX* and *BootstrapFX* for a clean, professional look.
* **📦 Native Installer:** Builds as a native Windows MSI installer for easy deployment.

## 🛠️ Tech Stack

| Component | Technology Used |
|-----------|----------------|
| **Core Language** | Java 21 (OpenJDK) |
| **GUI Framework** | JavaFX 21 |
| **UI Libraries** | ControlsFX, BootstrapFX, FormsFX, TilesFX, Ikonli |
| **Database** | MySQL 8.0, HikariCP (Pooling) |
| **Document Engine** | OpenHTMLtoPDF (PDFBox) |
| **Utilities** | JavaMail, Lombok |
| **Build & Deploy** | Maven, jPackage (MSI Installer) |

## 📦 Prerequisites

To build and run this project from source, you need:

* **JDK 21** (Ensure `JAVA_HOME` is set)
* **Maven 3.8+**
* **MySQL Server** (Running locally or remotely)

## 📥 Download & Demo

1.  **Download:** Get the latest Windows Installer (.msi) from the [Releases Page](../../releases).
2.  **Requirements:**
    * **Internet Connection:** Required (The app connects to a cloud demonstration database).
3.  **Demo Credentials:**
    Admin
    * **Username:** studentska@unze.ba
    * **Password:** studentska123
    Secretary
    * **Username:** sekretar@unze.ba
    * **Password:** sekretar123
    * *Note: This connects to a shared demo database. Please do not enter sensitive personal information.*
