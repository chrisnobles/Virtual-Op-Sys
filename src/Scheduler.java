import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<PCB> realTimeProcesses = new LinkedList<>();
    private LinkedList<PCB> interactiveProcesses = new LinkedList<>();
    private LinkedList<PCB> backgroundProcesses = new LinkedList<>();
    private LinkedList<PCB> sleepingProcesses = new LinkedList<>();
    private LinkedList<PCB> waitingProcesses = new LinkedList<>();
    private Timer timer;
    private PCB currentlyRunningProcess;

    public Scheduler() {
        timer = new Timer();

        if (currentlyRunningProcess == null) {
            currentlyRunningProcess = new PCB(new IdleProcess());
        }

        // Quantum expiration timer to stop processes
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentlyRunningProcess != null) {
                    currentlyRunningProcess.stop(); // Stop the currently running process
                }
            }
        }, 0, 250); // Run every 250ms
    }

    public int CreateProcess(UserlandProcess up, OS.Priority priority) {
        PCB pcb = new PCB(up, priority);

        switch (priority) {
            case REAL_TIME:
                realTimeProcesses.add(pcb);
                break;
            case INTERACTIVE:
                interactiveProcesses.add(pcb);
                break;
            case BACKGROUND:
                backgroundProcesses.add(pcb);
                break;
        }

        if (currentlyRunningProcess == null) {
            switchProcess();
        }

        return pcb.getPid();
    }

    public int CreateProcess(UserlandProcess up) {
        PCB pcb = new PCB(up);
        System.out.println("Scheduler: Adding process " + up.getClass().getSimpleName() + " with PID " + pcb.getPid());

        interactiveProcesses.add(pcb);

        if (currentlyRunningProcess == null) {
            switchProcess();
        }

        return pcb.getPid();
    }

    public PCB getCurrentlyRunningProcess() {
        return currentlyRunningProcess;
    }

    public void switchProcess() {
        // Clear the TLB on task switch
        Hardware.clearTLB();

        // Priority demotion: move currently running process to a lower priority queue
        if (currentlyRunningProcess != null && !currentlyRunningProcess.isDone()) {
            priorityDemotion(currentlyRunningProcess);
        }

        // Pick the next process to run based on priority
        if (!realTimeProcesses.isEmpty()) {
            currentlyRunningProcess = realTimeProcesses.pop();
        } else if (!interactiveProcesses.isEmpty()) {
            currentlyRunningProcess = interactiveProcesses.pop();
        } else if (!backgroundProcesses.isEmpty()) {
            currentlyRunningProcess = backgroundProcesses.pop();
        } else {
            currentlyRunningProcess = new PCB(new IdleProcess());
        }

        // Start the next process
        if (currentlyRunningProcess != null) {
            currentlyRunningProcess.start();
        }
    }

    private void priorityDemotion(PCB process) {
        if (realTimeProcesses.contains(process)) {
            realTimeProcesses.remove(process);
            interactiveProcesses.add(process);
        } else if (interactiveProcesses.contains(process)) {
            interactiveProcesses.remove(process);
            backgroundProcesses.add(process);
        }
    }

    public void exitCurrentProcess() {
        if (currentlyRunningProcess != null) {
            currentlyRunningProcess.freeAllMemory(); // Free all memory used by this process
            currentlyRunningProcess = null;
            switchProcess(); // Switch to the next process
        }
    }

    public void addSleepingProcess(PCB pcb) {
        sleepingProcesses.add(pcb);
    }

    public void wakeUpProcesses() {
        LinkedList<PCB> readyProcesses = new LinkedList<>();

        while (!sleepingProcesses.isEmpty()) {
            PCB pcb = sleepingProcesses.removeFirst();
            readyProcesses.add(pcb);
        }

        interactiveProcesses.addAll(readyProcesses);
    }

    public boolean isWaiting(PCB pcb) {
        return waitingProcesses.contains(pcb);
    }

    public void deschedule(PCB pcb) {
        waitingProcesses.add(pcb);
    }

    public void restoreToQueue(PCB pcb) {
        if (waitingProcesses.contains(pcb)) {
            waitingProcesses.remove(pcb);
            interactiveProcesses.add(pcb);
        }
    }

    public UserlandProcess getCurrentlyRunning() {
        if (currentlyRunningProcess != null) {
            // return currentlyRunningProcess.userlandProcess;
        }
        return null;
    }
}
