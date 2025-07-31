# 🚁 Java Drone Delivery System

This is a Java-based drone delivery simulation with multithreading support. It demonstrates a complete logistics system where drones autonomously deliver packages using Dijkstra's algorithm for optimal routing, with real-time status tracking through an Eclipse SWT/JFace GUI.

A **desktop GUI** provides visualization of drone movements, package statuses, and delivery progress.

---

## 📌 Features

- Package management with weight, status, and location tracking
- Drone fleet with payload capacity and availability monitoring
- Dijkstra's algorithm for optimal route calculation
- Multithreaded delivery simulation with realistic timing
- Real-time GUI updates using SWT/JFace
- Thread-safe operations with `ReentrantLock`
- Delivery progress simulation based on distance

---

## 🏗 Technologies Used

- Java 8+
- Eclipse SWT for native GUI components
- JFace for advanced table viewers
- Dijkstra's algorithm implementation
- `ExecutorService` for thread pooling
- `ReentrantLock` for concurrent access control

---

## 📁 Project Structure

| File               | Description                                |
|--------------------|--------------------------------------------|
| `Drone.java`       | Drone entity with payload and location logic |
| `Package.java`     | Package entity with delivery status        |
| `DeliverySystem.java` | Core logistics and threading system      |
| `DroneDeliverySystemGUI.java` | Main GUI application class       |
| `.gitignore`       | Standard Java/Eclipse ignores             |

---

## 🖥️ Graphical Interface

The SWT/JFace GUI provides real-time visualization of:

- Drone status (available/delivering)
- Package tracking (awaiting/on route/delivered)
- System logs and delivery progress
- Interactive package creation panel

To run:
1. Import into Eclipse with SWT/JFace support
2. Run `DroneDeliverySystemGUI.java` as Java Application

---

## 🤝 Collaborators

This project was developed by **[Aleeza Rizwan](https://github.com/its-aleezA)** as a demonstration of:
- Java multithreading
- Graph algorithms in logistics
- Desktop GUI development
- Concurrent system design

---

## ✅ License

This project is licensed under the **MIT License**.  
See the [LICENSE](LICENSE) file for details.
