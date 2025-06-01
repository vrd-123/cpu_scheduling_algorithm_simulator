package com.cpuscheduler;

import java.util.*;

public class SchedulerSimulator {
    private List<Process> processes;
    private List<ExecutionStep> executionSteps;
    private Map<String, Double> metrics;
    
    public SchedulerSimulator() {
        this.processes = new ArrayList<>();
        this.executionSteps = new ArrayList<>();
        this.metrics = new HashMap<>();
    }

    public void addProcess(Process process) {
        processes.add(process);
    }

    public void clearProcesses() {
        processes.clear();
        executionSteps.clear();
        metrics.clear();
    }

    public List<ExecutionStep> runFCFS() {
        executionSteps.clear();
        metrics.clear();
        
        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        int currentTime = 0;
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        
        for (Process process : sortedProcesses) {
            if (currentTime < process.getArrivalTime()) {
                currentTime = process.getArrivalTime();
            }
            
            process.setStartTime(currentTime);
            executionSteps.add(new ExecutionStep(process.getProcessId(), currentTime, 
                currentTime + process.getBurstTime()));
            
            currentTime += process.getBurstTime();
            process.setCompletionTime(currentTime);
            
            totalWaitingTime += process.getWaitingTime();
            totalTurnaroundTime += process.getTurnaroundTime();
        }
        
        // Calculate metrics
        calculateMetrics(totalWaitingTime, totalTurnaroundTime, currentTime);
        
        return executionSteps;
    }

    public List<ExecutionStep> runSJF() {
        executionSteps.clear();
        metrics.clear();
        
        List<Process> remainingProcesses = new ArrayList<>(processes);
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
            Comparator.comparingInt(Process::getBurstTime));
        
        int currentTime = 0;
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        
        while (!remainingProcesses.isEmpty() || !readyQueue.isEmpty()) {
            // Add processes that have arrived to ready queue
            Iterator<Process> iterator = remainingProcesses.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }
            
            if (readyQueue.isEmpty()) {
                currentTime = remainingProcesses.get(0).getArrivalTime();
                continue;
            }
            
            Process currentProcess = readyQueue.poll();
            currentProcess.setStartTime(currentTime);
            executionSteps.add(new ExecutionStep(currentProcess.getProcessId(), currentTime, 
                currentTime + currentProcess.getBurstTime()));
            
            currentTime += currentProcess.getBurstTime();
            currentProcess.setCompletionTime(currentTime);
            
            totalWaitingTime += currentProcess.getWaitingTime();
            totalTurnaroundTime += currentProcess.getTurnaroundTime();
        }
        
        calculateMetrics(totalWaitingTime, totalTurnaroundTime, currentTime);
        
        return executionSteps;
    }

    public List<ExecutionStep> runSRTF() {
        executionSteps.clear();
        metrics.clear();
        
        List<Process> remainingProcesses = new ArrayList<>(processes);
        for (Process p : remainingProcesses) {
            p.setRemainingTime(p.getBurstTime());
        }
        
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
            Comparator.comparingInt(Process::getRemainingTime));
        
        int currentTime = 0;
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        Process currentProcess = null;
        int lastSwitchTime = 0;
        
        while (!remainingProcesses.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            // Add newly arrived processes to ready queue
            Iterator<Process> iterator = remainingProcesses.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }
            
            // Check if we need to switch processes
            boolean switchRequired = false;
            if (currentProcess == null) {
                switchRequired = true;
            } else if (!readyQueue.isEmpty() && 
                      readyQueue.peek().getRemainingTime() < currentProcess.getRemainingTime()) {
                readyQueue.add(currentProcess);
                switchRequired = true;
            }
            
            if (switchRequired) {
                if (currentProcess != null && lastSwitchTime != currentTime) {
                    executionSteps.add(new ExecutionStep(currentProcess.getProcessId(), 
                        lastSwitchTime, currentTime));
                }
                
                currentProcess = readyQueue.poll();
                if (currentProcess != null) {
                    currentProcess.setStartTime(currentTime);
                    lastSwitchTime = currentTime;
                }
            }
            
            if (currentProcess == null) {
                currentTime = remainingProcesses.isEmpty() ? currentTime : 
                    remainingProcesses.get(0).getArrivalTime();
                continue;
            }
            
            // Execute for 1 time unit
            currentTime++;
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
            
            // Check if process is completed
            if (currentProcess.getRemainingTime() == 0) {
                executionSteps.add(new ExecutionStep(currentProcess.getProcessId(), 
                    lastSwitchTime, currentTime));
                currentProcess.setCompletionTime(currentTime);
                totalWaitingTime += currentProcess.getWaitingTime();
                totalTurnaroundTime += currentProcess.getTurnaroundTime();
                currentProcess = null;
            }
        }
        
        calculateMetrics(totalWaitingTime, totalTurnaroundTime, currentTime);
        
        return executionSteps;
    }

    public List<ExecutionStep> runRoundRobin(int timeQuantum) {
        executionSteps.clear();
        metrics.clear();
        
        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> remainingProcesses = new ArrayList<>(processes);
        for (Process p : remainingProcesses) {
            p.setRemainingTime(p.getBurstTime());
        }
        
        int currentTime = 0;
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        
        while (!remainingProcesses.isEmpty() || !readyQueue.isEmpty()) {
            // Add newly arrived processes to ready queue
            Iterator<Process> iterator = remainingProcesses.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }
            
            if (readyQueue.isEmpty()) {
                currentTime = remainingProcesses.get(0).getArrivalTime();
                continue;
            }
            
            Process currentProcess = readyQueue.poll();
            currentProcess.setStartTime(currentTime);
            
            int executeTime = Math.min(timeQuantum, currentProcess.getRemainingTime());
            executionSteps.add(new ExecutionStep(currentProcess.getProcessId(), 
                currentTime, currentTime + executeTime));
            
            currentTime += executeTime;
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executeTime);
            
            // Add newly arrived processes during this time quantum
            iterator = remainingProcesses.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }
            
            if (currentProcess.getRemainingTime() > 0) {
                readyQueue.add(currentProcess);
            } else {
                currentProcess.setCompletionTime(currentTime);
                totalWaitingTime += currentProcess.getWaitingTime();
                totalTurnaroundTime += currentProcess.getTurnaroundTime();
            }
        }
        
        calculateMetrics(totalWaitingTime, totalTurnaroundTime, currentTime);
        
        return executionSteps;
    }

    private void calculateMetrics(double totalWaitingTime, double totalTurnaroundTime, int totalTime) {
        double avgWaitingTime = totalWaitingTime / processes.size();
        double avgTurnaroundTime = totalTurnaroundTime / processes.size();
        double throughput = (double) processes.size() / totalTime;
        double cpuUtilization = processes.stream()
            .mapToInt(Process::getBurstTime)
            .sum() * 100.0 / totalTime;
        
        metrics.put("Average Waiting Time", avgWaitingTime);
        metrics.put("Average Turnaround Time", avgTurnaroundTime);
        metrics.put("Throughput", throughput);
        metrics.put("CPU Utilization (%)", cpuUtilization);
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public static class ExecutionStep {
        private String processId;
        private int startTime;
        private int endTime;

        public ExecutionStep(String processId, int startTime, int endTime) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getProcessId() { return processId; }
        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }
    }
} 