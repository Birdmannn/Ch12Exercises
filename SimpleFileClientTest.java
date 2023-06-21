import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SimpleFileClientTest {

    static final String DEFAULT_PORT = "1729";

    public static void main(String[] args) {

        String computer;         // The computer where the server is running
        String portStr;          // Port Number as a string.
        int port;                // The port on which the server listens.
        Socket connection;
        Scanner incoming;
        PrintWriter outgoing;

        String messageOut;
        String messageIn;
        Scanner userInput = new Scanner(System.in);       // Might not be used, eh?

        if(args.length == 0) {
            Scanner in = new Scanner(System.in);
            System.out.print("Enter computer name or IP address: ");
            computer = in.nextLine();
            System.out.print("Enter port, or press return to use default: ");
            portStr = in.nextLine();
            if(portStr.length() == 0)
                portStr = DEFAULT_PORT;
        }
        else {
            computer = args[0];
            if(args.length == 1)
                portStr = DEFAULT_PORT;
            else
                portStr = args[1];
        }
        try {
            port = Integer.parseInt(portStr);
            if(port <= 0 || port > 65535)
                throw new NumberFormatException();
        }
        catch(NumberFormatException e) {
            System.out.println("Illegal port number, " + portStr);
            return;
        }

        // Open a connection to the server.

        try {
            System.out.println("Connecting to " + computer + " on port " + port);
            connection = new Socket(computer, port);
            incoming = new Scanner(new InputStreamReader(connection.getInputStream()));
            outgoing = new PrintWriter(connection.getOutputStream());
            System.out.println(incoming.nextLine());
        }
        catch (Exception e) {
            System.out.println("An error occurred while opening connection.");
            System.out.println("Error: " + e);
            return;
        }

        while (true) {
            System.out.print("Input your command: ");
            messageOut = userInput.nextLine();
            try {
                if (messageOut.trim().equalsIgnoreCase("QUIT")) {
                    System.out.println("Connection closed.");
                    connection.close();
                    break;
                }

                outgoing.println(messageOut);
                outgoing.flush();

                if (outgoing.checkError())
                    throw new IOException("Error occurred while transmitting message.");

                // Receive items from the server...
                while (incoming.hasNextLine()) {
                    messageIn = incoming.nextLine();
                    if (messageIn.equals("...."))
                        break;
                    System.out.println(messageIn);
                }
            }
            catch (Exception e) {
                System.out.println("Sorry, an error has occurred. Connection lost.");
                System.out.println("Error: " + e);
                System.exit(1);
            }
        }
    }
}
