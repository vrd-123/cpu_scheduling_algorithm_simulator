# CPU Scheduling Algorithm Simulator

A Java application that simulates various CPU scheduling algorithms with an interactive GUI built using JavaFX.

## Features

- Supports multiple CPU scheduling algorithms:
  - First Come First Serve (FCFS)
  - Shortest Job First (SJF)
  - Shortest Remaining Time First (SRTF)
  - Round Robin (with configurable time quantum)
- Interactive process input with validation
- Real-time visualization using Gantt charts
- Comprehensive metrics including:
  - Average Waiting Time
  - Average Turnaround Time
  - CPU Utilization
  - Throughput

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Maven 3.6 or later

## Building and Running

1. Clone the repository.
2. Navigate to the project directory.
3. Build the project:
   ```bash
   mvn clean package
   ```
4. Run the application:
   ```bash
   mvn javafx:run
   ```

## How to Use

1. Select a scheduling algorithm from the dropdown menu.
2. For Round Robin, enter the desired time quantum.
3. Add processes by entering:
   - Process ID (any string identifier)
   - Arrival Time (non-negative integer)
   - Burst Time (positive integer)
4. Click "Add Process" to add each process to the simulation.
5. Click "Run Simulation" to execute the selected algorithm.
6. View the results in the Gantt chart and metrics section.
7. Use "Clear All" to reset the simulation.

## Metrics Explained

- **Average Waiting Time**: Average time processes spend waiting in the ready queue.
- **Average Turnaround Time**: Average time taken to complete a process.
- **Throughput**: Number of processes completed per unit time.
- **CPU Utilization**: Percentage of time the CPU is actively processing.

## Implementation Details

The simulator implements four classic CPU scheduling algorithms:

1. **First Come First Serve (FCFS)**
   - Non-preemptive
   - Processes are executed in the order they arrive.

2. **Shortest Job First (SJF)**
   - Non-preemptive
   - Selects the process with the shortest burst time.

3. **Shortest Remaining Time First (SRTF)**
   - Preemptive version of SJF
   - Switches to shorter processes when they arrive.

4. **Round Robin**
   - Preemptive
   - Each process gets a fixed time quantum.
   - Processes are executed in a circular manner.
  
# Demo:- 
![image](https://github.com/user-attachments/assets/04d2fe3f-5a9d-4b8e-92f8-f624012bb5c2)
