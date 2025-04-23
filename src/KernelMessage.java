import java.util.Arrays;

public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int message;
    private byte[] data;

    public KernelMessage(int senderPid, int targetPid, int what, byte[] data) {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.message = what;
        this.data = Arrays.copyOf(data, data.length); // Ensure data is copied
    }

    // copy constructor
    public KernelMessage(KernelMessage other) { // accepts a Kernel message, and makes a copy of it
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.message = other.message;
        this.data = Arrays.copyOf(other.data, other.data.length);
    }

    @Override
    public String toString() { // any format, does not matter
        return "senderPid=" + senderPid + ", targetPid=" + targetPid + " data=" + Arrays.toString(data);
    }

    public int getSenderPid() {
        return senderPid;
    }

    public int getTargetPid() {
        return targetPid;
    }

    public int getMessage() {
        return message;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
