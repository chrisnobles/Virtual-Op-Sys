//project 4 -- Oct 27, 2024

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

public class FakeFileSystem implements Device {
    private RandomAccessFile[] files = new RandomAccessFile[10];
    private int nextPage = 0; // Tracks the next page to write
    File fakeFile;

    @Override
    public int Open(String s) {
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                fakeFile = new File(s);
                try {
                    files[i] = new RandomAccessFile(s, "rw");
                    return i;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    @Override
    public void Close(int id) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].close();
                files[id] = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < files.length && files[id] != null) {
            byte[] data = new byte[size];
            try {
                files[id].read(data);
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].seek(to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        if (id >= 0 && id < files.length && files[id] != null) {
            if (data.length != Hardware.pageLength) {
                throw new IllegalArgumentException("Data must be exactly " + Hardware.pageLength + " bytes.");
            }
            try {
                files[id].write(data);
                nextPage++;
                return data.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int getNextPage() {
        return nextPage;
    }
}
