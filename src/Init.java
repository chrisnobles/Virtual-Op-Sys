//project 4 -- Oct 27, 2024

public class Init extends UserlandProcess{

    @Override
    public void main() {

        HelloWorld hello = new HelloWorld();
        GoodbyeWorld goodbye = new GoodbyeWorld();

        OS.CreateProcess(hello);
        OS.CreateProcess(goodbye);
        System.out.println("Processes HelloWorld and GoodbyeWorld created.");

    }
}