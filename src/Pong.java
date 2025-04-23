public class Pong extends UserlandProcess {

    @Override
    public void main() {
        int pingPid = OS.GetPidByName("Ping");
        System.out.println("I am PONG, ping = " + pingPid);
        int what = 0;

        while (true) {
            KernelMessage receivedMessage = OS.WaitForMessage();
            if (receivedMessage != null) {
                System.out.println("PONG: from " + receivedMessage.getSenderPid() + " to " + receivedMessage.getTargetPid() + " what: " + receivedMessage.getMessage());

                KernelMessage responseMessage = new KernelMessage(OS.GetPid(), pingPid, what, new byte[10]);
                OS.SendMessage(responseMessage);
                what++;
            }

            cooperate();
        }
    }
}
