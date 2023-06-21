import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedFileServer {
    static int DEFAULT_PORT = 1729;
    public static ArrayBlockingQueue<Socket> socketQueue;

    private static final int MAX_CAPACITY = 25;
    private static final int MAX_NUMBER_OF_THREADS = 14;
    private static File outputFile;
    private static int noOfConnections;

    public static void main(String[] args) {
        int port;
        ServerSocket listener;
        Socket connection;

        socketQueue = new ArrayBlockingQueue<>(MAX_CAPACITY);
        // ExecutorService executor = Executors.newFixedThreadPool(noOfProcessors);    //...

        File userHomeDirectory = new File(System.getProperty("user.home"));

        if(args.length == 0) {
            port = DEFAULT_PORT;
            outputFile = new File(userHomeDirectory, "Documents");
        }
        else {
            char ch = args[0].charAt(0);
            if(args.length == 1) {
                if(Character.isDigit(ch)) {
                    try {
                        port = Integer.parseInt(args[0]);
                        outputFile = new File(userHomeDirectory, "Documents");
                        if(port < 0 || port > 65535)
                            throw new NumberFormatException();
                    }
                    catch(NumberFormatException e) {
                        System.out.println("Illegal port number, " + args[0]);
                        return;
                    }
                }
                else {
                    outputFile = new File(userHomeDirectory, args[0]);
                    port = DEFAULT_PORT;
                }
            }
            else {
                outputFile = new File(userHomeDirectory, args[1]);
                port = DEFAULT_PORT;
            }
        }

        // Create thread pool
        WorkerThread[] worker = new WorkerThread[MAX_NUMBER_OF_THREADS];
        for (int i = 0; i < worker.length; i++) {
            worker[i] = new WorkerThread();
            worker[i].start();
        }
        //Wait for a connection request.

        while (true) {
            try {
                listener = new ServerSocket(port);
                System.out.println("File Server: " + outputFile);
                connection = listener.accept();
            }
            catch (IOException e) {
                System.out.println("Couldn't create connection.");
                System.out.println("ERROR: " + e);
                e.printStackTrace();
                break;
            }
            try {
                socketQueue.put(connection);
            }
            catch (Exception e) {
                System.out.println("Couldn't put socket into the queue.");
                System.out.println("ERROR: " + e);
            }
            port++;
            noOfConnections++;
        }

    }   // End of main()

    private static class WorkerThread extends Thread {
        WorkerThread() {
            try {
                setDaemon(true);
            }
            catch (Exception ignored) {}
        }
        @Override
        public void run() {
            while (true) {

                ConnectionHandler connectionHandler;
                connectionHandler = new ConnectionHandler();
                connectionHandler.run();
            }
        }
    }

    // Create a separate thread to handle time-outs.
    private static class ReaperThread extends Thread {
        private Socket connection;
        public ReaperThread(Socket connection) {
            this.connection = connection;
        }
        @Override
        public void run() {
            int timeCount = 0;
            while (true) {
                try {
                    sleep(1000);
                    timeCount++;
                    if (timeCount == 30) {
                        throw new RuntimeException("Connection Time out");
                    }
                }
                catch (Exception ignored) {}
                finally {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static class ConnectionHandler implements Runnable {
        private static Socket connection;

        @Override
        public void run() {
            while (true) {
                Scanner readFromClient;
                PrintWriter writeToClient;
                try {
                    connection = socketQueue.take();
                }
                catch (Exception e) {
                    System.out.println("An Error just Occurred.\nStatus: 1");
                    System.out.println("Error: " + e);
                }
                try {
                    readFromClient = new Scanner(new InputStreamReader(connection.getInputStream()));
                    writeToClient = new PrintWriter(connection.getOutputStream());
                    writeToClient.println("Connected.");
                    writeToClient.flush();
                    try {
                        String clientInput;
                        if (!outputFile.exists()) {
                            if (!outputFile.isFile())
                                System.out.println(outputFile + " is not a file.");
                            else
                                System.out.println(outputFile + " does not exist.");
                            return;
                        }
                        File[] fileList;
                        while (true) {
                            writeToClient.println("Enter 'quit' to end the program.");
                            clientInput = readFromClient.next();
                            if (clientInput.equalsIgnoreCase("QUIT")) {
                                writeToClient.println("The connection has been closed.");
                                writeToClient.flush();
                                writeToClient.close();
                                System.out.println("Connection closed.");
                                break;
                            }
                            else if (clientInput.equalsIgnoreCase("INDEX")) {
                                fileList = outputFile.listFiles();
                                writeToClient.println("Files in " + outputFile + ": ");
                                assert fileList != null;
                                for (File file : fileList) {
                                    writeToClient.println("    " + file);
                                    writeToClient.flush();
                                }
                                writeToClient.println("Finished.");
                                writeToClient.println("....");
                                writeToClient.flush();
                            }
                            else if (clientInput.equalsIgnoreCase("GET")) {
                                writeToClient.println("Type name of file:");
                                writeToClient.println("....");
                                writeToClient.flush();
                                clientInput = readFromClient.next();
                                File checkFile = new File(outputFile, clientInput);
                                if (!checkFile.exists() && !checkFile.isFile()) {
                                    writeToClient.println("ERROR." + checkFile);
                                    writeToClient.println("....");
                                    writeToClient.flush();
                                    break;
                                }
                                if (checkFile.isDirectory()) {
                                    writeToClient.println("OK.");
                                    writeToClient.println("Files in " + checkFile + ": ");
                                    fileList = checkFile.listFiles();
                                    assert fileList != null;
                                    for (File file : fileList) {
                                        writeToClient.println("    " + file);
                                    }
                                    writeToClient.println("....");
                                    writeToClient.flush();
                                }
                                else {
                                    writeToClient.println(checkFile + " is not a directory.");
                                    writeToClient.println("....");
                                    writeToClient.flush();
                                }
                            }
                            else {
                                writeToClient.println("\"" + clientInput + "\" is not a command.");
                                writeToClient.println("....");
                                writeToClient.flush();
                            }
                            writeToClient.flush();
                            if (writeToClient.checkError())
                                throw new IOException("Error occurred while transmitting message.");
                        } // end of while.
                    }
                    catch (Exception e) {
                        System.out.println("An Error has occurred.\nStatus: Second to the last.");
                        System.out.println("Error: " + e);
                        writeToClient.println(e);
                    }
                }
                catch (Exception e) {
                    System.out.println("An error occurred while opening connection.");
                    System.out.println("Error: " + e);
                }
            }
        }
    }
}
