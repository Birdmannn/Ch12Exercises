import java.util.Scanner;
public class ThreadTest {

    private static Counter counter;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        WorkerThread[] worker;
        int noOfThreads, noOfIncrement;
        int actualTotal;

        System.out.println("This is a program that will test the error occurred when many threads");
        System.out.println("use a method that is not synchronized to increment a counter.");
        System.out.println();
        System.out.println("Type in the number of threads you wish to use,");
        System.out.println("and the number of times each thread will increment\nthe counter.\n");
        System.out.print("No. of Threads to use: ");
        noOfThreads = in.nextInt();
        System.out.print("No. of times to Increment: ");
        noOfIncrement = in.nextInt();
        counter = new Counter();

        actualTotal = noOfThreads * noOfIncrement;
        worker = new WorkerThread[noOfThreads];

        for (int i = 0; i < noOfThreads; i++) {
            worker[i] = new WorkerThread(noOfIncrement);
            worker[i].start();
        }
        System.out.println("Threads have started.");

        for (int i = 0; i < noOfThreads; i++) {
            while (worker[i].isAlive()) {
                try  {
                    worker[i].join();
                }
                catch (InterruptedException ignored) {}
            }
        }

        System.out.println("Operation finished.");
        System.out.println("Actual Count: " + actualTotal);
        System.out.println("Count Result: " + counter.getCount());

    }

    static class Counter {
        private int count;
        void inc() {
            count++;
        }
        int getCount() {
            return count;
        }
    }

    static class WorkerThread extends Thread {
        int noOfIncrement;
        WorkerThread(int noOfIncrement) {
            this.noOfIncrement = noOfIncrement;
        }
        public void run() {
            for (int i = 0; i < noOfIncrement; i++) {
                counter.inc();
            }
        }
    }
}
