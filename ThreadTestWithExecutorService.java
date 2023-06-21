
import java.util.concurrent.*;

public class ThreadTestWithExecutorService {

    private static final int MAX_VALUE = 100_000;
    private static final int DIVISION = 100;
    private static final int NUMBER_OF_TASKS = MAX_VALUE/DIVISION;

    public static void main(String[] args) {
        ArrayBlockingQueue<Future<String>> resultQueue;

        System.out.println("Does the same task as the last exercise Ex 12.3.");
        System.out.println("Creating and adding tasks...");

        int noOfProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(noOfProcessors);
        resultQueue = new ArrayBlockingQueue<>(NUMBER_OF_TASKS);

        System.out.println("Processing started.");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            int start = DIVISION * i + 1;
            int end = DIVISION * (i + 1);
            WorkerTask task = new WorkerTask(start, end);
            Future<String> oneTaskResult = executor.submit(task);
            try {
                resultQueue.put(oneTaskResult);         // Save the future result.
            }
            catch (Exception ignored) {}
        }

        executor.shutdown();

        // Here, process all the results
        int maxIntegerFound = 0;
        int numberOfDivisors = 0;

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            try {
                Future<String> result = resultQueue.take();
                String[] firstValue = result.get().split(",");
                int firstInteger = Integer.parseInt(firstValue[0]);
                if (firstInteger > numberOfDivisors) {
                    numberOfDivisors = firstInteger;      // Record the value;
                    maxIntegerFound = Integer.parseInt(firstValue[1]);
                }
            }
            catch (Exception ignored) {}
        }

        long endTime = System.currentTimeMillis();
        double totalRuntime = (endTime - startTime) / 1000.0;
        // Here all the results in the queue have been processed. Print out the result
        System.out.println("\nFinished.");
        System.out.println("Max Integer Found: " + maxIntegerFound);
        System.out.println("Number of Divisors: " + numberOfDivisors);
        System.out.println("Total Runtime: " + totalRuntime);
    }

    static class WorkerTask implements Callable<String> {
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
        public String call() {
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

            return value1 + "," + value2;
        }
    }

}
