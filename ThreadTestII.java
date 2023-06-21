/**
 * Find the Integer in the range of 1 to 100_000 that has the largest number of divisors.
 * First divide the task into
 */
public class ThreadTestII {
    private final static int NUMBER_OF_THREADS = 4;
    private final static int MAX_VALUE = 100_000;
    public static int maxIntegerFound;
    public static int highestNoOfDivisorsSoFar;

    public static void main(String[] args) {
        int partValue = MAX_VALUE/NUMBER_OF_THREADS;
        long startTime, endTime;
        double totalRuntime;
        WorkerThread[] worker = new WorkerThread[NUMBER_OF_THREADS];

        Thread waiting = new Thread(() -> {
            Thread.currentThread().setPriority(worker[0].getPriority() - 1);
            System.out.print("Running.");
            while (true) {
                try {
                    Thread.sleep(1600);
                }
                catch (InterruptedException ignored) {
                    break;
                }
                System.out.print(".");
            }
        });

        System.out.println("This program finds the integer in the range of 1 to 100,000 that has\n" +
                "the largest number of divisors.");
        System.out.println("It does this using " + NUMBER_OF_THREADS + " threads.");
        startTime = System.currentTimeMillis();

        // Assign start and end values to each of the four threads.
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            worker[i] = new WorkerThread(partValue * i + 1, partValue * (i + 1));
            worker[i].start();
        }
        System.out.println("Threads have started.");
        // waiting.start();

        // Wait for all worker threads to finish.
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            while (worker[i].isAlive()) {
                try {
                    worker[i].join();
                }
                catch (InterruptedException ignored) {}
            }
        }

        endTime = System.currentTimeMillis();
        // waiting.interrupt();
        totalRuntime = (endTime - startTime) / 1000.0;
        System.out.println("\n\nFinished.");
        System.out.println("Max Integer found: " + maxIntegerFound);
        System.out.println("Number of divisors: " + highestNoOfDivisorsSoFar);
        System.out.println("Total runtime: " + totalRuntime + "s");

    }

    synchronized static void updateValues(int noOfDivisors, int currentMax) {
        highestNoOfDivisorsSoFar = noOfDivisors;
        maxIntegerFound = currentMax;
    }

    static class WorkerThread extends Thread {
        private final int start;
        private final int end;
        public int noOfDivisors;

        // Constructor to initialize where to start searching from, and where to end.
        public WorkerThread(int start, int end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public void run() {
            // This is where the search algorithm will take place
            int startNumber = start;
            while (startNumber <= end) {
                for (int divisor = 1; divisor <= startNumber; divisor++) {
                    if (startNumber % divisor == 0) {
                        noOfDivisors++;
                    }
                }
                if (noOfDivisors > highestNoOfDivisorsSoFar)
                    updateValues(noOfDivisors, startNumber);
                startNumber++;
                noOfDivisors = 0;      // Re-initialize.
            }
        }
    }
}
