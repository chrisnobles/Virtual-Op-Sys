public class Ping extends UserlandProcess {

    @Override
    public void main() {
        System.out.println("HELLO!!!");
        int pongPid = OS.GetPidByName("Pong");
        System.out.println("I am PING, pong = " + pongPid);
        int what = 0;

        while (true) {
            KernelMessage message = new KernelMessage(OS.GetPid(), pongPid, what, new byte[10]);
            OS.SendMessage(message);

            KernelMessage receivedMessage = OS.WaitForMessage();
            if (receivedMessage != null) {
                System.out.println("PING: from " + receivedMessage.getSenderPid() + " to " + receivedMessage.getTargetPid() + " what: " + receivedMessage.getMessage());
                what++;
            }

            OS.switchProcess();
            //OS.exit();
        }
    }
}
