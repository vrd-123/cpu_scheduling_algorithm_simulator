package com.cpuscheduler;

public class Process {
    private String processId;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int completionTime;
    private int waitingTime;
    private int turnaroundTime;
    private int startTime;

    public Process(String processId, int arrivalTime, int burstTime) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.startTime = -1;
    }

    // Getters and setters
    public String getProcessId() { return processId; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getCompletionTime() { return completionTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getStartTime() { return startTime; }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
        this.turnaroundTime = completionTime - arrivalTime;
        this.waitingTime = turnaroundTime - burstTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setStartTime(int startTime) {
        if (this.startTime == -1) {
            this.startTime = startTime;
        }
    }

    @Override
    public String toString() {
        return String.format("Process[ID=%s, Arrival=%d, Burst=%d]",
                processId, arrivalTime, burstTime);
    }
} 