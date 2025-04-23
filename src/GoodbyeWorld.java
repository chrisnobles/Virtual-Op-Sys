//project 4 -- Oct 27, 2024

public class GoodbyeWorld extends UserlandProcess {
    public void main() {
        while (true) {
            System.out.println("Goodbye World");
            cooperate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }
}