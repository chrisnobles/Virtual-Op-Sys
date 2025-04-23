//project 4 -- Oct 27, 2024

public class IdleProcess extends UserlandProcess {

    @Override
    public void main() {
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("IdleProcess interrupted.");
                Thread.currentThread().interrupt();
            }
            cooperate();
        }
    }
}
