import java.util.HashMap;

public class Kernel extends Process implements Device {
    private Scheduler scheduler = new Scheduler();
    private VFS vfs = new VFS();
    private PCB pcb = new PCB(new IdleProcess());
    private HashMap<Integer, PCB> processMap = new HashMap<>();

    @Override
    public void main() {}

    public Kernel() {
        this.start();
    }

    public void run() {
        while (true) {
            stop();
            if (OS.currentCall != null) {
                switch (OS.currentCall) {
                    case CREATE_PROCESS:
                        int pid = handleCreateProcess();
                        OS.returnValue = pid;
                        break;

                    case SWITCH_PROCESS:
                        handleSwitchProcess();
                        break;

                    case SLEEP:
                        break;

                    case EXIT:
                        boolean freedStatus = handleExit();
                        OS.returnValue = freedStatus;
                        break;

                    case ALLOCATE_MEMORY:
                        int allocatedAddress = handleAllocateMemory();
                        OS.returnValue = allocatedAddress;
                        break;

                    case FREE_MEMORY:
                        freedStatus = handleFreeMemory();
                        OS.returnValue = freedStatus;
                        break;

                    default:
                        break;
                }
                OS.currentCall = null;
            }

            if (scheduler.getCurrentlyRunning() != null) {
                System.out.println("Starting currently running process with PID: " +
                        scheduler.getCurrentlyRunningProcess().getPid());
                scheduler.getCurrentlyRunning().start();
            } else {
                System.out.println("No currently running process. Switching to IdleProcess...");
                scheduler.switchProcess();
            }
        }
    }

    private int handleCreateProcess() {
        int pid;
        if (OS.parameters.size() == 2 && OS.parameters.get(1) instanceof OS.Priority) {
            pid = scheduler.CreateProcess((UserlandProcess) OS.parameters.get(0), (OS.Priority) OS.parameters.get(1));
        } else {
            pid = scheduler.CreateProcess((UserlandProcess) OS.parameters.get(0));
        }

        PCB createdProcess = scheduler.getCurrentlyRunningProcess();
        if (createdProcess != null) {
            processMap.put(createdProcess.getPid(), createdProcess);
            System.out.println("Process created with PID: " + pid + " and added to processMap");
        }
        return pid;
    }

    private void handleSwitchProcess() {
        scheduler.switchProcess();
        pcb = scheduler.getCurrentlyRunningProcess();
        if (pcb != null) {
            Hardware.clearTLB();
            System.out.println("KERNEL: Updated PCB after context switch to PID " + pcb.getPid());
        } else {
            System.out.println("KERNEL: No process running after context switch.");
        }
    }

    private boolean handleExit() {
        int pointer = (int) (Integer) OS.parameters.get(0);
        int size = (int) (Integer) OS.parameters.get(1);
        return Hardware.FreeMemory(pointer, size);
    }

    private int handleAllocateMemory() {
        int size = (int) (Integer) OS.parameters.get(0);
        return (int) Hardware.AllocateMemory(size);
    }

    private boolean handleFreeMemory() {
        int pointer = (int) (Integer) OS.parameters.get(0);
        int size = (int) (Integer) OS.parameters.get(1);
        return Hardware.FreeMemory(pointer, size);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public int Open(String s) {
        pcb = scheduler.getCurrentlyRunningProcess();
        if (pcb == null) {
            //System.out.println("KERNEL: No processes are currently running.");
            return -1;
        }

        int[] deviceIds = pcb.getDeviceID();

        for (int i = 0; i < deviceIds.length; i++) {
            if (deviceIds[i] == -1) {
                int vfsId = vfs.Open(s);

                if (vfsId != -1) {
                    deviceIds[i] = vfsId;
                    //System.out.println("KERNEL: Opened device with VFS id " + vfsId + " and assigned PCB slot " + i);
                    return i;
                } else {
                    //System.out.println("KERNEL: Failed to open device.");
                    return -1;
                }
            }
        }
        System.out.println("KERNEL: No available slots to open the device.");
        return -1;
    }

    @Override
    public void Close(int id) {
        pcb = scheduler.getCurrentlyRunningProcess();
        int[] deviceIds = pcb.getDeviceID();

        if (id >= 0 && id < deviceIds.length && deviceIds[id] != -1) {
            //System.out.println("KERNEL: Closing device with VFS id " + deviceIds[id] + " and PCB slot " + id);
            vfs.Close(deviceIds[id]);
            deviceIds[id] = -1;
        } else {
            //System.out.println("KERNEL: Failed to close device.");
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        pcb = scheduler.getCurrentlyRunningProcess();
        int[] deviceIds = pcb.getDeviceID();

        if (id >= 0 && id < deviceIds.length && deviceIds[id] != -1) {
            //System.out.println("KERNEL: Reading from device with VFS id " + deviceIds[id] + " and PCB slot " + id);
            return vfs.Read(deviceIds[id], size);
        }
        //System.out.println("KERNEL: Error occurred while reading from the device.");
        return new byte[0];
    }

    @Override
    public void Seek(int id, int to) {
        pcb = scheduler.getCurrentlyRunningProcess();
        int[] deviceIds = pcb.getDeviceID();

        if (id >= 0 && id < deviceIds.length && deviceIds[id] != -1) {
            //System.out.println("KERNEL: Seeking in device with VFS id " + deviceIds[id] + " and PCB slot " + id + " to position " + to);
            vfs.Seek(deviceIds[id], to);
        } else {
            //System.out.println("KERNEL: Error occurred while attempting to seek.");
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        pcb = scheduler.getCurrentlyRunningProcess();
        int[] deviceIds = pcb.getDeviceID();

        if (id >= 0 && id < deviceIds.length && deviceIds[id] != -1) {
            //System.out.println("KERNEL: Writing to device with VFS id " + deviceIds[id] + " and PCB slot " + id);
            int result = vfs.Write(deviceIds[id], data);
            //System.out.println("KERNEL: Write completed, bytes written: " + result);
            return result;
        }
        //System.out.println("KERNEL: Failed to write to device.");
        return 0;
    }

    public int GetPid() {
        return scheduler.getCurrentlyRunningProcess().getPid();
    }

    public int GetPidByName(String name) {
        return processMap.values().stream()
                .filter(pcb -> pcb.getName().equals(name))
                .map(PCB::getPid)
                .findFirst()
                .orElse(-1);
    }

    public void SendMessage(KernelMessage km) {
        PCB targetPCB = processMap.get(km.getTargetPid());
        if (targetPCB != null) {
            KernelMessage messageCopy = new KernelMessage(km);
            targetPCB.getMessageQueue().add(messageCopy);
            System.out.println("Message sent from PID " + km.getSenderPid() + " to PID " + km.getTargetPid());
            if (scheduler.isWaiting(targetPCB)) {
                scheduler.restoreToQueue(targetPCB);
            }
        }
    }

    public KernelMessage WaitForMessage() {
        PCB currentPCB = scheduler.getCurrentlyRunningProcess();
        if (!currentPCB.getMessageQueue().isEmpty()) {
            System.out.println("Message received by PID " + currentPCB.getPid());
            return currentPCB.getMessageQueue().poll();
        }
        scheduler.deschedule(currentPCB);
        return null;
    }
}
