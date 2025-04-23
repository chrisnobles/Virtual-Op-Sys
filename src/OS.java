import java.util.ArrayList;

// Project 3 -- Oct 13, 2024
public class OS {

    public enum CallType {
        CREATE_PROCESS, SWITCH_PROCESS, SLEEP, EXIT, WAIT_FOR_MESSAGE, ALLOCATE_MEMORY, FREE_MEMORY
    }

    public enum Priority {
        REAL_TIME, INTERACTIVE, BACKGROUND
    }

    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;
    public static Kernel kernel;
    public static FakeFileSystem fakeFileSystem;
    public static int swapFile;
    public static int swapFilePage = 0;

    // Overloaded CreateProcess that takes a UserlandProcess and a Priority enum
    public static int CreateProcess(UserlandProcess up, Priority priority) {
        parameters.clear();
        if (up == null || priority == null) {
            throw new IllegalArgumentException("UserlandProcess or Priority cannot be null");
        }
        parameters.add(up);       // Add the process
        parameters.add(priority); // Add the priority
        currentCall = CallType.CREATE_PROCESS;
        kernelSwitch();
        return (int) returnValue;
    }

    public static int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    public static void Startup(UserlandProcess init, Priority priority) {
        swapFile = fakeFileSystem.Open("swapfile.sys");
        kernel = new Kernel();
        OS.Priority initPriority = priority != null ? priority : Priority.INTERACTIVE;

        CreateProcess(init, initPriority);
        CreateProcess(new IdleProcess(), Priority.BACKGROUND);
    }


    public static void switchProcess() {
        resetParameters();
        parameters.add(kernel.getScheduler().getCurrentlyRunning());
        currentCall = CallType.SWITCH_PROCESS;
        kernelSwitch();
    }

    private static void resetParameters() {
        parameters.clear();
    }

    private static void kernelSwitch() {
        kernel.start();

        if (kernel.getScheduler().getCurrentlyRunning() != null) {
            kernel.getScheduler().getCurrentlyRunning().stop();
        }

        while (returnValue == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void Sleep(int milliseconds) {
        parameters.clear();
        parameters.add(milliseconds);
        currentCall = CallType.SLEEP;
        kernelSwitch();

        while (returnValue == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int GetPid() {
        return kernel.GetPid();
    }

    public static int GetPidByName(String name) {
        return kernel.GetPidByName(name);
    }

    public static void SendMessage(KernelMessage km) {
        kernel.SendMessage(km);
    }

    public static KernelMessage WaitForMessage() {
        return kernel.WaitForMessage();
    }

    public static int AllocateMemory(int size) throws InterruptedException {
        if (size % 1024 != 0) {
            return -1; // Ensure size is aligned to the page size
        }

        parameters.clear();
        parameters.add(size);
        currentCall = CallType.ALLOCATE_MEMORY;

        PCB currentProcess = kernel.getScheduler().getCurrentlyRunningProcess();
        kernel.start();

        if (currentProcess != null) {
            currentProcess.stop();
        }

        while (returnValue == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException("Error waiting for memory allocation", e);
            }
        }

        // Populate the VirtualToPhysicalMapping array
        int allocatedAddress = (int) returnValue;
        int numPages = size / Hardware.pageLength;
        for (int i = 0; i < numPages; i++) {
            VirtualToPhysicalMapping mapping = new VirtualToPhysicalMapping();
            mapping.physicalPageNumber = (allocatedAddress / Hardware.pageLength) + i;
            currentProcess.getPageTable()[i] = mapping;
        }

        return allocatedAddress;
    }

    public static int FreeMemory(int pointer, int size) {
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.FREE_MEMORY;

        PCB currentProcess = kernel.getScheduler().getCurrentlyRunningProcess();
        kernelSwitch();

        // Clear the VirtualToPhysicalMapping entries
        VirtualToPhysicalMapping[] pageTable = currentProcess.getPageTable();
        int startPage = pointer / Hardware.pageLength;
        int numPages = size / Hardware.pageLength;

        for (int i = startPage; i < startPage + numPages; i++) {
            if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
                Hardware.FreeMemory(pageTable[i].physicalPageNumber * Hardware.pageLength, Hardware.pageLength);
                pageTable[i] = null; // Set the mapping to null
            }
        }

        return (int) returnValue;
    }
}
