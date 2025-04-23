//project 4 -- Oct 27, 2024

public class VFS implements Device {
    private Device[] devices = new Device[10];
    private int[] ids = new int[10];

    public VFS() {
        for (int i = 0; i < ids.length; i++) {
            ids[i] = -1;
        }
    }

    @Override
    public int Open(String s) {
        String[] parts = s.split(" ", 2);
        String deviceType = parts[0]; // first word = device type
        String deviceArgument = "";

        if (parts.length > 1) {
            deviceArgument = parts[1];
        }

        System.out.println("VFS: Attempting to open device of type '" + deviceType + "' with argument: '" + deviceArgument + "'");

        Device device = null;

        // Debugging -- check if the device type == 'random' or 'file'
        if (deviceType.equals("random")) {
            device = new RandomDevice();
            System.out.println("VFS: Created RandomDevice.");
        } else if (deviceType.equals("file")) {
            device = new FakeFileSystem();
            System.out.println("VFS: Created FakeFileSystem.");
        }

        if (device != null) {
            for (int i = 0; i < devices.length; i++) {
                if (devices[i] == null) {  // Find an available slot
                    int id = device.Open(deviceArgument); // Open the device with the argument
                    if (id != -1) {
                        devices[i] = device;  // Store the device in the array
                        ids[i] = id;
                        System.out.println("VFS: Device opened with VFS id " + i + " and device id " + id);
                        return i;  // Return the index
                    } else { // Otherwise, an error occurred while opening the device.
                        System.out.println("VFS: Device failed to open.");
                    }
                }
            }
        }

        System.out.println("VFS: The device failed to open.");
        return -1;  // return -1 if an error occurred opening the device
    }


    @Override
    public void Close(int id) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            System.out.println("VFS: Closing device with VFS id " + id + " and device id " + ids[id]);
            devices[id].Close(ids[id]);
            devices[id] = null;
            ids[id] = -1;
        } else {
            System.out.println("VFS: Failed to close device. Invalid VFS id " + id);
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            System.out.println("VFS: Reading " + size + " bytes from device with VFS id " + id + " and device id " + ids[id]);
            return devices[id].Read(ids[id], size);
        }
        System.out.println("VFS: Failed to read. Invalid VFS id " + id);
        return null;
    }

    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            System.out.println("VFS: Seeking in device with VFS id " + id + " and device id " + ids[id] + " to position " + to);
            devices[id].Seek(ids[id], to);
        } else {
            System.out.println("VFS: Failed to seek. Invalid VFS id " + id);
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            System.out.println("VFS: Writing " + data.length + " bytes to device with VFS id " + id + " and device id " + ids[id]);
            return devices[id].Write(ids[id], data);
        }
        System.out.println("VFS: Failed to write. Invalid VFS id " + id);
        return 0;
    }
}
