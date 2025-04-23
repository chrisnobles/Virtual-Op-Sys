//project 4 -- Oct 27, 2024

public class HelloWorld extends UserlandProcess {
    public void main() {
        while (true) {
            System.out.println("Hello World");
            cooperate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }
}