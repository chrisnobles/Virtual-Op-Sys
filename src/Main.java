import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        FakeFileSystem fakeFile = new FakeFileSystem();
        OS.fakeFileSystem = fakeFile;
        Kernel kernel = new Kernel();
        Hardware.kernel = kernel;

        OS.Startup(new HelloWorld(), OS.Priority.INTERACTIVE);
        for (int i = 1; i < 20; i++) {
            OS.CreateProcess(new Piggy(i), OS.Priority.BACKGROUND);
        }
    }
}

//public class Main {
//
//    public static void main(String[] args) {
//        OS.Startup(new Init());
//
//        int memorySize = 1024;
//        int allocatedAddress = OS.AllocateMemory(memorySize);
//        if (allocatedAddress != -1) {
//            System.out.println("Memory allocated successfully at address: " + allocatedAddress);
//        } else {
//            System.out.println("Memory allocation failed.");
//        }
//
//        try {
//            byte testValue = 127;
//            int writeAddress = allocatedAddress;
//            Hardware.WriteMemory(writeAddress, testValue);
//            System.out.println("Test WriteMemory: Value written successfully.");
//        } catch (Exception e) {
//            System.out.println("Test WriteMemory: Failed to write memory. " + e.getMessage());
//        }
//
//        try {
//            byte readValue = Hardware.ReadMemory(allocatedAddress);
//            System.out.println("Test ReadMemory: Value read successfully: " + readValue);
//        } catch (Exception e) {
//            System.out.println("Test ReadMemory: Failed to read memory. " + e.getMessage());
//        }
//
//        int freedStatus = OS.FreeMemory(allocatedAddress, memorySize);
//        if (freedStatus == 1) {
//            System.out.println("Memory freed successfully.");
//        } else {
//            System.out.println("Failed to free memory.");
//        }
//    }
//}
//
//
//// project 2 -- sept 29, 2024
//
////public class Main {
////    public static void main(String[] args) {
////        OS.Startup(new Init());
////
////    }
////
////}
//
////project 3 -- Oct 13, 2024
////import java.util.Arrays;
//
////public class Main {
////    public static void main(String[] args) {
////        Kernel kernel = new Kernel();
////        int deviceID = kernel.Open("random 314159");
////        int FFS = kernel.Open("file MY_TEST_FILE.txt");
////        int err = -1;
////
////        if ((deviceID & FFS) != err) { // If the deviceID and FakeFileSystem had no errors opening (neither = -1),
////            byte[] randomData = kernel.Read(deviceID, 10);
////            byte[] fileData = kernel.Read(FFS, 10);
////            System.out.println("randomData:" + Arrays.toString(randomData));
////            System.out.println("FakeFileSystem:" + Arrays.toString(fileData));
////        }
////    }
////}
//
////// project 4
////public class Main {
////    public static void main(String[] args) {
////        OS.kernel = new Kernel(); // Initialize the kernel
////
////        // Ensure both Ping and Pong processes are created
////        System.out.println("Creating Ping process");
////        OS.CreateProcess(new Ping());
////
////        System.out.println("Creating Pong process");
////        OS.CreateProcess(new Pong());
////
////        OS.switchProcess();
////    }
////}

