import java.util.Arrays;
import java.util.Random;

public class Hardware {
    static final int pageLength = 1024;
    private static final int numPages = 1024;
    private static byte[] memory = new byte[pageLength * numPages];
    private static boolean[] pageUsage = new boolean[numPages];
    private static int[][] TLB = new int[2][2];
    public static Kernel kernel; // static TLB, virtual mappings

    // Static initializer to set initial states
    static {
        for (int i = 0; i < numPages; i++) { // Initialize all pages as free
            pageUsage[i] = true;
        }
        for (int i = 0; i < 2; i++) { // Initialize TLB with no mappings (-1)
            TLB[0][i] = -1;
            TLB[1][i] = -1;
        }
    }

    private Hardware() {} // Prevent instantiation

    public static byte ReadMemory(int address) {
        int physicalAddress = translateAddress(address);

        if (physicalAddress < 0 || physicalAddress >= memory.length) { // Check for invalid memory access
            throw new IndexOutOfBoundsException("Invalid memory access at address: " + address);
        }

        return memory[physicalAddress]; // Return the value at the physical address
    }

    public static void WriteMemory(int address, byte value) {
        int physicalAddress = translateAddress(address);

        if (physicalAddress < 0 || physicalAddress >= memory.length) { // Check for out-of-bounds
            throw new IndexOutOfBoundsException("Invalid memory access at address: " + address);
        }

        memory[physicalAddress] = value; // Write the value to the memory
    }

    private static int translateAddress(int virtualAddress) {
        int virtualPage = virtualAddress / pageLength;
        int pageOffset = virtualAddress % pageLength;

        // Check the TLB for a mapping
        for (int i = 0; i < 2; i++) {
            if (TLB[0][i] == virtualPage) { // TLB hit
                int physicalPage = TLB[1][i];
                return (physicalPage * pageLength) + pageOffset;
            }
        }

        // No TLB hit, retrieve mapping from page table
        int physicalPage = GetMapping(virtualPage);

        // Update the TLB
        TLB[0][0] = TLB[0][1];
        TLB[1][0] = TLB[1][1];
        TLB[0][1] = virtualPage;
        TLB[1][1] = physicalPage;

        return (physicalPage * pageLength) + pageOffset;
    }

    public static int GetMapping(int virtualPage) {
        PCB currentPCB = kernel.getScheduler().getCurrentlyRunningProcess();
        VirtualToPhysicalMapping[] pageTable = currentPCB.getPageTable();

        if (virtualPage < 0 || virtualPage >= pageTable.length) { // Check for out-of-bounds virtual page
            throw new IndexOutOfBoundsException("Virtual page number out of bounds: " + virtualPage);
        }

        VirtualToPhysicalMapping mapping = pageTable[virtualPage];
        if (mapping != null && mapping.physicalPageNumber != -1) {
            return mapping.physicalPageNumber;
        }

        int freePage = findFreePage();
        if (freePage == -1) {
            freePage = performPageSwap(currentPCB);
        }

        if (mapping == null) {
            mapping = new VirtualToPhysicalMapping();
            pageTable[virtualPage] = mapping;
        }
        mapping.physicalPageNumber = freePage;
        initializePhysicalPage(freePage);
        return freePage;
    }

    public static int AllocateMemory(int size) {
        if (size % pageLength != 0) { // Ensure size is a multiple of page length
            return -1;
        }

        int numPagesRequired = size / pageLength;
        int startPage = -1;
        int availablePageCount = 0;

        for (int i = 0; i < numPages; i++) {
            if (pageUsage[i]) { // Page is free
                if (startPage == -1) {
                    startPage = i; // Mark the start of a possible block
                }
                availablePageCount++;
                if (availablePageCount == numPagesRequired) {
                    break;
                }
            } else {
                startPage = -1; // Reset search
                availablePageCount = 0;
            }
        }

        if (availablePageCount < numPagesRequired) {
            return -1;
        }

        // Mark the pages as allocated
        for (int i = startPage; i < startPage + numPagesRequired; i++) {
            pageUsage[i] = false;
        }

        // Return the starting address of the allocated block
        return startPage * pageLength;
    }

    public static boolean FreeMemory(int pointer, int size) {
        if (pointer % pageLength != 0 || size % pageLength != 0) { // Ensure alignment to page size
            return false;
        }

        int startPage = pointer / pageLength;
        int numPagesToFree = size / pageLength;

        PCB currentPCB = kernel.getScheduler().getCurrentlyRunningProcess();
        VirtualToPhysicalMapping[] pageTable = currentPCB.getPageTable();

        for (int i = startPage; i < startPage + numPagesToFree; i++) {
            if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
                pageUsage[pageTable[i].physicalPageNumber] = true; // Mark page as free
                pageTable[i] = null; // Clear mapping
            }
        }
        return true;
    }

    private static int findFreePage() {
        for (int i = 0; i < pageUsage.length; i++) {
            if (pageUsage[i]) {
                pageUsage[i] = false;
                return i;
            }
        }
        return -1; // No free page found
    }

    private static int performPageSwap(PCB victimPCB) {
        VirtualToPhysicalMapping victimPage = null;
        for (VirtualToPhysicalMapping mapping : victimPCB.getPageTable()) {
            if (mapping != null && mapping.physicalPageNumber != -1) {
                victimPage = mapping;
                break;
            }
        }

        if (victimPage == null) {
            throw new RuntimeException("No victim page found for swapping.");
        }

        // Evict the victim page
        int evictedPage = victimPage.physicalPageNumber;
        victimPage.physicalPageNumber = -1;
        pageUsage[evictedPage] = true;

        return evictedPage;
    }

    public static void initializePhysicalPage(int physicalPage) {
        Arrays.fill(memory, physicalPage * pageLength, (physicalPage + 1) * pageLength, (byte) 0);
    }

//    public static void updateTLB(int virtualPage, int physicalPage) {
//        int randomIndex = (int) (Math.random() * TLB.length);
//        TLB[randomIndex][0] = virtualPage;
//        TLB[randomIndex][1] = physicalPage;
//    }

    public static void clearTLB() {
        for (int i = 0; i < 2; i++) { // Clear TLB mappings
            TLB[0][i] = -1; // Clear virtual pages
            TLB[1][i] = -1; // Clear physical pages
        }
    }
}
