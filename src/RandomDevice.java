//project 4 -- Oct 13, 2024

import java.util.Random;

public class RandomDevice implements Device {
    private Random[] devices = new Random[10];

    @Override
    public int Open(String s) {
        for (int i = 0; i < devices.length; i++) {
            if (devices[i] == null) { // Find the first available slot in the devices array
                if (s != null && !s.isEmpty()) { // If a seed is provided, use it to create the Random instance
                    devices[i] = new Random(Integer.parseInt(s));
                } else { // Else, create a Random instance without a seed
                    devices[i] = new Random();
                }
                return i; // Return the index of the opened device
            }
        }
        return -1; // Return -1 if no space is available in the array
    }

    @Override
    public void Close(int id) {
        if (id >= 0 && id < devices.length) {
            devices[id] = null; // Null indicates the device is closed
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            byte[] data = new byte[size]; // Create a byte array to store random numbers
            Random randomDevice = devices[id]; // Get the Random instance

            for (int i = 0; i < size; i++) {
                data[i] = (byte) randomDevice.nextInt(); // Fill the array with random integers converted to bytes
            }

            return data;
        }
        return null;
    }

    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            Random randomDevice = devices[id];

            for (int i = 0; i < to; i++) {
                randomDevice.nextInt();
            }
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        // since write is random, just return 0 for now
        return 0;
    }
}