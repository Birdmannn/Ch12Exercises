import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadTestWithPool {
    private static LinkedBlockingQueue<Runnable> taskQueue;         // You can use a ConcurrentLinkedQueue too, but you'd
    private static ArrayBlockingQueue<String> resultQueue;          // have to add the task to the queue before starting
    private static final int MAX_VALUE = 100_000;                   // the thread.
    private static final int DIVISION = 100;
    private static final int NUMBER_OF_TASKS = MAX_VALUE/DIVISION;   // I'll be using a total of 1000 tasks here
    public static volatile boolean running;    // For abortion.

    public static void main(String[] args) {
        taskQueue = new LinkedBlockingQueue<>();
        resultQueue = new ArrayBlockingQueue<>(NUMBER_OF_TASKS);

        System.out.println("Does the same task as the last exercise Ex 12.2.");
        System.out.println("Creating and adding tasks...");

        int threadCount = Runtime.getRuntime().availableProcessors();
        WorkerThread[] worker = new WorkerThread[threadCount];
        running = true;

        // Create threads. Threads will block (be idle) and start processing once tasks are available.
        for (int i = 0; i < threadCount; i++) {
            worker[i] = new WorkerThread();
            worker[i].start();
        }
        System.out.println("Threads have started.");
        long startTime = System.currentTimeMillis();

        // Create tasks here and add tasks to the queue for threads are started.
        // For loop to add tasks into the taskQueue
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            int start = DIVISION * i + 1;
            int end = DIVISION * (i + 1);
            Runnable task = new WorkerTask(start, end);
            taskQueue.add(task);
        }

        // Process and compare results in the results queue
        int currentMax = 0;
        int numberOfDivisors = 0;
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            try {
                String result = resultQueue.take();
                String[] firstValue = result.split(",");
                int firstInteger = Integer.parseInt(firstValue[0]);
                if (firstInteger > numberOfDivisors) {
                    numberOfDivisors = firstInteger;      // Record the value;
                    currentMax = Integer.parseInt(firstValue[1]);
                }
            }
            catch (InterruptedException ignored) {}
        }

        running = false;
        long endTime = System.currentTimeMillis();
        double totalRuntime = (endTime - startTime) / 1000.0;
        // Here all the results in the queue have been processed. Print out the result
        System.out.println("\nFinished.");
        System.out.println("Max Integer Found: " + currentMax);
        System.out.println("Number of Divisors: " + numberOfDivisors);
        System.out.println("Total Runtime: " + totalRuntime);
    }

    /**
     * Divide the task into fairly small problems. For the task, we use a nested class
     * that implements the interface Runnable.
     */
    static class WorkerTask implements Runnable {
        private final int start;
        private final int end;
        public int noOfDivisors;
        private int highestNoOfDivisorsSoFar;
        private int maxIntegerFound;

        // Constructor to initialize where to start searching from, and where to end.
        public WorkerTask(int start, int end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public void run() {
            // This is where the search algorithm will take place
            int currentMax = start;
            while (currentMax <= end) {
                for (int divisor = 1; divisor <= currentMax; divisor++) {
                    if (currentMax % divisor == 0) {
                        noOfDivisors++;
                    }
                }
                if (noOfDivisors > highestNoOfDivisorsSoFar) {
                    highestNoOfDivisorsSoFar = noOfDivisors;
                    maxIntegerFound = currentMax;
                }
                currentMax++;
                noOfDivisors = 0;      // Re-initialize.
            }
            // After the while loop, add the result from the task into the queue.
            String value1 = String.valueOf(highestNoOfDivisorsSoFar);
            String value2 = String.valueOf(maxIntegerFound);
            try {
                resultQueue.put(value1 + "," + value2);
            }
            catch (InterruptedException ignored) {}
        }
    }

    /**
     *  A Thread for handling tasks in the thread pool
     */
    private static class WorkerThread extends Thread {
        WorkerThread() {
            try {
                setDaemon(true);
            }
            catch (Exception ignored) {}
        }
        @Override
        public void run() {
            try {
                while (running) {
                    Runnable task = taskQueue.take();   // Get a task from the queue
                    task.run();     // Execute the task
                }
            }
            catch (Exception ignored) {}
        }
    }
}
