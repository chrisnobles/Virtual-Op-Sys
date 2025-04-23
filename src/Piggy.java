import java.util.Random;

public class Piggy extends UserlandProcess {
    private int piggyNumber;

    @Override
    public void main() {

        Random random = new Random();
        System.out.println("Piggy Process: Starting Piggy #" + piggyNumber);
        int memoryStartAddress;
        int executionCount = 0;

        try {
            memoryStartAddress = OS.AllocateMemory(100 * 1024);
            System.out.println("Memory Allocation: Piggy #" + piggyNumber + " allocated 100 KB of memory at starting virtual address: " + memoryStartAddress);
        } catch (InterruptedException e) {
            System.out.println("Error: Memory allocation failed for Piggy #" + piggyNumber + ": " + e.getMessage());
            e.printStackTrace();
            return; // Exit if allocation fails
        }

        while (true) {
            int offsetAddress = random.nextInt(100 * 1024);
            int randomValue = random.nextInt();

            Hardware.WriteMemory((offsetAddress + memoryStartAddress), (byte) randomValue);
            System.out.println("Memory Write: Piggy #" + piggyNumber + " wrote value " + (byte) randomValue +
                    " to virtual address " + (offsetAddress + memoryStartAddress));

            byte retrievedValue = Hardware.ReadMemory(offsetAddress + memoryStartAddress);
            System.out.println("Memory Read: Piggy #" + piggyNumber + " read value " + retrievedValue +
                    " from virtual address " + (offsetAddress + memoryStartAddress));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Error: Piggy #" + piggyNumber + " was interrupted during sleep: " + e.getMessage());
                e.printStackTrace();
                return; // Exit if thread sleep is interrupted
            }

            cooperate();

            if (executionCount++ == 20 && piggyNumber == 20) {
                OS.FreeMemory(memoryStartAddress, 100 * 1024);
                System.out.println("Memory Free: Piggy #20 has released 100 KB of allocated memory.");
                while (true) {
                    OS.Sleep(100);
                }
            }
        }
    }

    public Piggy(int piggyNumber) {
        this.piggyNumber = piggyNumber;
    }

    @Override
    public String toString() {
        return "Piggy Process: Piggy #" + piggyNumber;
    }
}
