# 📞 VoIP Communication System (Java Socket-Based)

A basic **Voice over IP (VoIP) Communication System** developed using **Java Socket Programming**. This project demonstrates real-time audio and text communication over a network. The application supports live voice calls, text messaging, and recorded audio messaging between multiple clients connected to a server. This project was developed as part of the Computer Networking Lab  (CSE 312) course at Green University of Bangladesh.

---

## 📋 Table of Contents

- [🔍 Project Overview](#-project-overview)
- [🎯 Features](#-features)
- [⚙️ Technologies Used](#️-technologies-used)
- [🧠 Core Components](#-core-components)
- [📊 Performance Evaluation](#-performance-evaluation)
- [🧪 Limitations](#-limitations)
- [📈 Future Enhancements](#-future-enhancements)
- [👨‍💻 Authors](#-authors)
- [📎 References](#-references)

---

## 🔍 Project Overview

This **VoIP Communication System** is a Java-based networked application that simulates real-time voice and text communication using:

- Java Socket Programming  
- Audio recording and playback libraries  
- A multithreaded server to handle multiple clients  
- A GUI-based interface using **Java Swing**

This system aims to replicate a simplified version of common VoIP software like Skype or Zoom, focusing on core communication principles like latency management, client handling, and data streaming.

---

## 🎯 Features

- 📡 Real-time voice communication between clients  
- 💬 Text-based messaging  
- 🎙️ Record and send audio clips  
- 🎧 Playback received audio files  
- 👥 Supports multiple users using threads  
- 🖥️ Graphical user interface (GUI) for both client and server  
- 🔄 Bidirectional communication with live socket streaming  

---

## ⚙️ Technologies Used

- **Java** – Core language for client and server logic  
- **Java Socket API** – Enables TCP/UDP-based communication  
- **javax.sound.sampled** – For audio recording and playback  
- **Swing** – GUI creation for server/client interaction  
- **Multithreading** – Manages multiple clients simultaneously  

---

## 🧠 Core Components

### 🖥 Server

- Manages client connections via port 8080  
- Uses a synchronized list to store client handlers  
- Handles both text and audio transmission  
- Broadcasts messages/audio to all connected clients  
- GUI includes server status and control panel  

### 👤 Client

- Connects to server using hostname and port  
- Sends and receives messages or audio files  
- Records audio using `TargetDataLine`  
- Plays audio using `SourceDataLine`  
- GUI includes message area, audio buttons, and status prompts  
- Supports live voice call simulation via piped input/output streams  

---

## 📊 Performance Evaluation

### 🧪 Testing Setup

- Server and multiple clients run on **localhost**
- Tested text messaging, audio playback, and real-time voice streaming

### 🔎 Results

| Metric          | Outcome                                   |
|-----------------|-------------------------------------------|
| **Latency**     | Minimal delay in LAN environment          |
| **Audio Quality** | Acceptable for normal conversations     |
| **Scalability** | Stable for small number of concurrent users |
| **Text Messaging** | Real-time updates with low CPU usage  |
| **Live Audio Call** | Working, with slight distortion under load |

---

## 🧪 Limitations

- ❌ Not designed for large-scale deployment  
- ❌ Lacks encryption or secure transmission  
- ❌ Audio quality can degrade under high network load  
- ❌ Requires Java to be installed on all machines  
- ❌ No mobile or web client version  

---

## 📈 Future Enhancements

- 🔒 Implement end-to-end encryption for secure communication  
- 🗣️ Add better audio codecs for improved clarity  
- 🌐 Deploy on cloud to support remote access  
- 🎥 Add support for video conferencing  
- 🧩 Modularize the codebase for easier feature expansion  
- 📱 Build mobile clients for Android and iOS  

---

## 👨‍💻 Authors

**Md Syful Islam**  (Student ID: 222002111)   
**Tasdid Rahman Khan**  (Student ID : 222002029) 

🎓 B.Sc. in CSE (Day), Green University of Bangladesh  
🧪 **Course:** Computer Networking Lab (CSE 312)  
👨‍🏫 **Instructor:** Muhaimen Khan  
📚 **Section:** 222-D5  
📅 **Submission Date:** 24 December 2024  

---

## 📎 References

1. Khasnabish, B. (2003). *Implementing Voice over IP*. Wiley-IEEE Press.  
2. [Java Sockets Documentation – Oracle](https://docs.oracle.com/javase/tutorial/networking/sockets/)  
3. [Java Sound API Guide](https://docs.oracle.com/javase/tutorial/sound/)  
