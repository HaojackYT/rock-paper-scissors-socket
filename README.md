# Rock-Paper-Scissors Socket

![Java](https://img.shields.io/badge/Java-24%2B-orange?style=flat-square&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=flat-square&logo=apache-maven)
![Python](https://img.shields.io/badge/Python-3.x-3776AB?style=flat-square&logo=python)
![Sockets](https://img.shields.io/badge/Network-Sockets-informational?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

> **Real-time Multiplayer Rock-Paper-Scissors with AI and Hand Gesture Recognition**  

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [How to Run](#-how-to-run)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [Build & Run](#-build--run)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)
- [Support](#-support)

---

## 🎯 Overview

**Rock-Paper-Scissors Socket** is a real-time multiplayer desktop game built with **Java** (server & client), plus an optional **Python hand-gesture service** that allows players to use hand gesture recognition (via images) as input.

The system consists of:

- 🧠 **Java Game Server** – handles connections, game logic, and player matching  
- 💻 **Java Clients** – CLI client, AI client, and UI client  
- ✋ **Python Hand-Gesture Service** – HTTP service that predicts *Rock / Paper / Scissors* from an uploaded image  

---

## ✨ Key Features

### Gameplay

- 🎮 1v1 real-time Rock-Paper-Scissors  
- ⚡ Low-latency socket-based communication  
- 🔁 Rematch support  
- 🏆 Win / lose / draw stats  

### Clients
| Client Type                          |         Status        | Description                                              |
| ------------------------------------ | :-------------------: | -------------------------------------------------------- |
| Console Client (`NioClient`)         |    🟢 **Available**   | Basic terminal-based client for testing gameplay         |
| AI Client (`ai-client-runnable.jar`) |     🔄 **Planned**    | Automatic gameplay using simple rule-based AI            |
| UI Client (`GameClientUI`)           | 🟢 **Available** | Java Swing graphical client for user-friendly experience |

### Hand Gesture Integration (Python)

- ✋ Send an image to the `/predict` endpoint (e.g., hand photo)  
- 🧾 Receive prediction: **Rock / Paper / Scissors**  
- 🌐 Exposed as an HTTP service:

  - URL: `http://localhost:8000/predict`  
  - Form-Data: `key = file`, `value = file.jpg` (or other supported image formats)

---

## 🛠 Technology Stack

### Core

| Technology | Version | Role                          |
|-----------|---------|-------------------------------|
| Java      | 24+     | Game server & clients         |
| Maven     | 3.x     | Java build & dependencies     |
| Python    | 3.x     | Hand gesture prediction       |

### Networking

| Component | Description                          |
|----------|--------------------------------------|
| Java NIO | Non-blocking socket server (`NioServer`) |
| TCP      | Client–server communication          |

### Hand-Gesture Service

| Technology     | Role                                      |
|----------------|-------------------------------------------|
| Python venv    | Isolated environment for the service      |
| Docker (opt.)  | Containerized deployment of gesture API   |
| HTTP API       | `/predict` endpoint for image-based moves |

---

## 🏗 Architecture

```text
                    ┌───────────────────────────────┐
                    │       Java Game Server        │
                    │         com.example.*         │
                    └─────────────────┬─────────────┘
                                      │ TCP Socket :5000
             ┌────────────────────────┼─────────────────────────┐
             │                        │                         │
     ┌───────▼─────────┐      ┌───────▼─────────┐       ┌───────▼─────────┐
     │ Java CLI Client │      │   AI Client     │       │   UI Client     │
     │   (Available)   │      │   (Planned)     │       │  (Available)    │
     └─────────────────┘      └─────────────────┘       └─────────────────┘


Future Feature:
       ┌───────────────────────────────────────────────┐
       │   Python FastAPI Hand Gesture Service (Local) │
       │              (Not Yet Integrated)             │
       └───────────────────────────────────────────────┘


```

---

## 📦 Prerequisites

Before running the project, make sure you have:

### Java & Build Tools

- ✅ **JDK 24 or higher**  
- ✅ **Apache Maven 3.x**  

### Python / Hand Gesture Service

- ✅ **Python 3.x**  
- ✅ **pip** (Python package manager)  
- ✅ **Docker** (optional, if you want to run via Docker)  

### Ports

- Java server: **5000**  
- Hand gesture service: **8000**  

---

## 🚀 Installation

Clone the repository and build the Java project:

```bash
git clone <repository-url>
cd rock-paper-scissors-socket

# Build Java project
mvn clean install
```

After building:

- Compiled classes: `target/classes`  
- JARs: `target/*.jar`  

---

## ⚙️ Configuration

### Java Server / Client

If needed, adjust host/port in your Java classes, for example:

```java
// Example – adjust to your own package / classes
String host = "localhost";
int port = 5000;
```

### Hand Gesture Service

The Python service listens on:

```text
http://localhost:8000/predict
```

It expects a **POST** request with:

- `key = file`  
- `value = your image file` (e.g., `file.jpg`)

---

## ▶️ How to Run

This section follows the multi-step flow to run server, clients, and Python service.

### ✅ Step 1: Start the Java Server

In a terminal, start the game server first so clients can connect.

**Option 1 – using `Main` class:**

```bash
java -cp target/classes com.example.Main server 5000
```

**Option 2 – using `NioServer` class:**

```bash
java -cp target/classes com.example.server.NioServer server 5000
```

> Make sure the package names (`com.example...`) match your actual project structure.

---

### ✅ Step 2: Start the Java Clients

You can run multiple clients to simulate multiple players.

#### 2.1 Start the AI Client

In a **new terminal window**:

```bash
java -jar target/ai-client-runnable.jar
```

This will start an automated AI player that connects to the server and plays automatically.

#### 2.2 Start a Console Client

In another **new terminal window**:

```bash
java -cp target/classes com.example.client.NioClient localhost 5000
```

You can open multiple terminals and run this command multiple times to simulate multiple human players.

#### 2.3 Start the UI Client

To start the Swing-based graphical client:

```bash
java -cp target/classes com.example.GameClientUI localhost 5000
```

Again, ensure the class `com.example.GameClientUI` exists and matches your package structure.

---

### ✅ Step 3: Start the Python Hand-Gesture Service

You can run the hand-gesture service **with Docker** or **without Docker**.

#### Option A – With Docker

From the project root or `hand-gesture-service` directory:

```bash
cd hand-gesture-service

# 1. Build the Docker image
docker build -t rock-paper-scissors .

# 2. Run the container
docker run -p 8000:8000 rock-paper-scissors
```

The service will now be available at:

```text
http://localhost:8000/predict
```

Send a **POST** request with:

- `key = file`  
- `value = file.jpg` (or another image extension you support)

#### Option B – Without Docker (Local Python environment)

From `hand-gesture-service` directory:

```bash
cd hand-gesture-service

# 1️⃣ Create a virtual environment
py -m venv venv

# 2️⃣ Activate the virtual environment (Windows)
venv\Scripts\Activate

# 3️⃣ Upgrade pip and install dependencies
python.exe -m pip install --upgrade pip
pip install -r requirements.txt

# 4️⃣ Run the Python service
python main.py
```

The service will expose:

```text
http://localhost:8000/predict
```

Call it with a `file` field:

- `key = file`  
- `value = file.jpg` (or other supported image files)

---

## 🎮 Usage

Once everything is running:

- **Server** is listening on `localhost:5000`  
- **Clients** connect as:
  - AI Client: `ai-client-runnable.jar`
  - Console Client: `NioClient`
  - UI Client: `GameClientUI`
- **Python service** is ready at `http://localhost:8000/predict`  

In the UI or AI logic, you can:

1. Capture or choose an image of a hand  
2. Send it to the `/predict` endpoint  
3. Use the returned label (*Rock*, *Paper*, or *Scissors*) as the player’s move  

> The exact integration depends on how you wired the Java client to call the Python HTTP endpoint.

---

## 📁 Project Structure

Example layout (adapt as needed):

```text
rock-paper-scissors-socket/
│
├── src/main/java/com/example/
│   ├── server/
│   │   └── NioServer.java           # Java NIO server
│   ├── client/
│   │   └── NioClient.java           # Console client
│   |
│   │── GameClientUI.java        # Swing UI client
│   ├── Main.java                    # Entry wrapper for server/client
│   └── ...                          # Models, utils, etc.
│
├── hand-gesture-service/            # Python service
│   ├── main.py                      # Python entry point
│   ├── requirements.txt             # Python dependencies
│   ├── Dockerfile                   # Docker build file
│   └── ...                          # Model, utils, etc.
│
├── target/                          # Build output (generated by Maven)
├── pom.xml                          # Maven config
└── README.md                        # This file
```

---

## 🔨 Build & Run (Java Only)

Build the Java project:

```bash
mvn clean package
```

Run server or clients using the built artifacts as shown in the [How to Run](#▶️-how-to-run) section.

---

## 🐛 Troubleshooting

### Cannot connect to server

- Make sure the Java server is running on `localhost` and port `5000`  
- Check firewall rules  
- Confirm that the client connects to the correct host & port  
### Hand-gesture service issues

**Docker:**

- Ensure the container is running: `docker ps`  
- Confirm port mapping: `-p 8000:8000`  

**Local Python:**

- Ensure `venv` is activated  
- Make sure `requirements.txt` installed successfully  
- Check which URL it logs when starting (usually `http://127.0.0.1:8000` or similar)  

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository  
2. Create a feature branch:

   ```bash
   git checkout -b feature/my-feature
   ```

3. Commit your changes:

   ```bash
   git commit -m "Add: my feature"
   ```

4. Push and open a Pull Request:

   ```bash
   git push origin feature/my-feature
   ```

---

## 📄 License

This project is licensed under the **MIT License**.  
See the `LICENSE` file for details.

---

## 📞 Support

- 🐛 Issues: *https://github.com/HaojackYT/rock-paper-scissors-socket/issues*  
- ✉️ Email: `rps-support@example.com`  

<br>

<div align="center">
Made with ❤️ for Rock–Paper–Scissors  
<br/>
<strong><a href="#rock-paper-scissors-socket">⬆ Back to top</a></strong>
</div>
