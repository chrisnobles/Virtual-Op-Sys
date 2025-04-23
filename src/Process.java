//project 4 -- Oct 27, 2024

import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable {
    private Thread thread;
    private Semaphore semaphore;
    private boolean quantumExpired = false;

    public Process() {
        this.semaphore = new Semaphore(0);
        this.thread = new Thread(this);
        thread.start();
    }

    public void requestStop() {
        quantumExpired = true;
    }

    public abstract void main();

    public boolean isStopped() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isDone() {
        return !thread.isAlive();
    }

    public void start() {
        semaphore.release();
    }

    public void stop() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("Process was interrupted while stopping.");
        }
    }

    @Override
    public void run() {
        try {
            semaphore.acquire(); // Wait until start() releases the semaphore
            System.out.println("Running main() for: " + this.getClass().getSimpleName());
            main();
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted while running: " + this.getClass().getSimpleName());
            Thread.currentThread().interrupt();
        }
    }

    public void cooperate() {
        if (quantumExpired) {
            quantumExpired = false;
            OS.switchProcess();
        }
    }
}