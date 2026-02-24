package queueengine_v1;

public class QueueRunner {

    public static void main(String[] args) {

        long startTime = System.nanoTime();

        System.out.println("=================================");
        System.out.println(" DIAGNOIQ - QUEUE ENGINE V1 ");
        System.out.println("=================================");

        QueueProcessor processor = new QueueProcessor();
        processor.processNextReport();

        long endTime = System.nanoTime();

        long durationNano = endTime - startTime;
        double durationMs = durationNano / 1_000_000.0;

        System.out.println("\n=================================");
        System.out.println(" TOTAL EXECUTION TIME ");
        System.out.println("=================================");
        System.out.println("Time in ms  : " + durationMs);
        System.out.println("Time in sec : " + (durationMs / 1000));
        System.out.println("=================================");
    }
}