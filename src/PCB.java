import java.time.Instant;
import java.util.LinkedList;

public class PCB {
    private static final int NUM_PAGES = 100; // Total number of virtual pages
    private static int nextPid = 1; // Static nextPid that increments for each process
    private int pid; // Process ID
    private UserlandProcess userlandProcess;
    private Instant wakeUpTime;  // Instant to track when the process should wake up
    private OS.Priority priority;  // Field to track the priority of the process
    private int[] deviceIds = new int[10];
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<>();
    private VirtualToPhysicalMapping[] pageTable = new VirtualToPhysicalMapping[NUM_PAGES]; // Updated to use the new class

    // Constructor to create a PCB instance, set pid, and associate with the userland process
    // Default priority is set to INTERACTIVE if not provided
    public PCB(UserlandProcess up) {
        this.pid = nextPid++;
        this.userlandProcess = up;
        this.priority = OS.Priority.INTERACTIVE;  // Default priority
        this.name = up.getClass().getSimpleName();

        for (int i = 0; i < deviceIds.length; i++) { // Initialize as -1 to show that no devices are opened at the beginning
            this.deviceIds[i] = -1;
        }

        for (int i = 0; i < pageTable.length; i++) { // Initialize pageTable entries
            this.pageTable[i] = new VirtualToPhysicalMapping(); // Default to -1 for physical and disk pages
        }
    }

    // Overloaded constructor to set a custom priority
    public PCB(UserlandProcess up, OS.Priority priority) {
        this.pid = nextPid++;
        this.userlandProcess = up;
        this.priority = priority;  // Use the passed priority
        this.name = up.getClass().getSimpleName();

        for (int i = 0; i < deviceIds.length; i++) { // Initialize as -1 to show that no devices are opened at the beginning
            this.deviceIds[i] = -1;
        }

        for (int i = 0; i < pageTable.length; i++) { // Initialize pageTable entries
            this.pageTable[i] = new VirtualToPhysicalMapping(); // Default to -1 for physical and disk pages
        }
    }

    public int[] getDeviceID() { // Accessor to return the deviceIds
        return deviceIds;
    }

    // Stop method which will stop the userland process and loop until the process is stopped
    public void stop() {
        userlandProcess.stop(); // Stop the userland process
        while (!userlandProcess.isStopped()) { // Wait until the process is completely stopped
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while stopping the process.");
            }
        }
    }

    // Method to check if the process is done by calling userlandProcess' isDone method
    public boolean isDone() {
        return userlandProcess.isDone();
    }

    // Start method which calls the userland process start method
    public void start() {
        userlandProcess.start();
    }

    // Getter for the process ID
    public int getPid() {
        return pid;
    }

    // Get the wake-up time for sleeping processes
    public Instant getWakeUpTime() {
        return wakeUpTime;
    }

    // Set the wake-up time for sleeping processes
    public void setWakeUpTime(Instant wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    // Getter for the priority
    public OS.Priority getPriority() {
        return priority;
    }

    // Setter for the priority
    public void setPriority(OS.Priority priority) {
        this.priority = priority;
    }

    // Accessor for the message queue
    public LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }

    // Accessor for the process name
    public String getName() {
        return name;
    }

    // Getter for the page table
    public VirtualToPhysicalMapping[] getPageTable() {
        return pageTable;
    }

    // Free all memory mappings
    public void freeAllMemory() {
        for (VirtualToPhysicalMapping mapping : pageTable) {
            if (mapping.physicalPageNumber != -1) {
                Hardware.FreeMemory(mapping.physicalPageNumber * Hardware.pageLength, Hardware.pageLength);
                mapping.physicalPageNumber = -1;
            }
            if (mapping.diskPageNumber != -1) {
                mapping.diskPageNumber = -1; // Clear disk mappings as well
            }
        }
    }
}
