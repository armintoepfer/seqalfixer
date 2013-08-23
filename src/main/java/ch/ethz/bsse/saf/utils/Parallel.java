/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.utils;


/**
 * Java Parallel.For Parallel.ForEach
 */
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


import java.util.concurrent.ExecutionException;
/**
 *    A Java Paralle.For | Parallel.ForEach 
 */
public class Parallel {

    static int iCPU = Runtime.getRuntime().availableProcessors();

    /**
     * Parallel.ForEach
     */
    public static <T> void ForEach(Iterable<T> parameters, final LoopBody<T> loopBody) {
        ExecutorService executor = Executors.newFixedThreadPool(iCPU);
        List<Future<?>> futures = new LinkedList<>();

        for (final T param : parameters) {
            Future<?> future = executor.submit(new Runnable() {
                public void run() {
                    loopBody.run(param);
                }
            });

            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        executor.shutdown();
    }

    /**
     * Parallel.For
     */
    public static void For(int start, int stop, final LoopBody<Integer> loopBody) {
        ExecutorService executor = Executors.newFixedThreadPool(iCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();

        for (int i = start; i < stop; i++) {
            final Integer k = i;
            Future<?> future = executor.submit(new Runnable() {
                public void run() {
                    loopBody.run(k);
                }
            });

            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        executor.shutdown();
    }

    /**
     * Create Partitions To Turn Parallel.For To Parallel.ForEach
     */
    public static ArrayList<Partition> create(int inclusiveStart, int exclusiveEnd) {
        return create(inclusiveStart, exclusiveEnd, iCPU);
    }

    public static ArrayList<Partition> create(int inclusiveStart, int exclusiveEnd, int cores) {
        //increment
        int total = exclusiveEnd - inclusiveStart;
        double dc = (double) total / cores;
        int ic = (int) dc;

        if (ic <= 0) {
            ic = 1;
        }
        if (dc > ic) {
            ic++;
        }

        //partitions
        ArrayList<Partition> partitions = new ArrayList<Partition>();
        if (total <= cores) {
            for (int i = 0; i < total; i++) {
                Partition p = new Partition();
                p.start = i;
                p.end = i + 1;
                partitions.add(p);
            }
            return partitions;
        }

        int count = inclusiveStart;
        while (count < exclusiveEnd) {
            Partition p = new Partition();
            p.start = count;
            p.end = count + ic;

            partitions.add(p);
            count += ic;

            //boundary check
            if (p.end >= exclusiveEnd) {
                p.end = exclusiveEnd;
                break;
            }
        }

        return partitions;
    }

    /**
     * Unit Test
     */
    public static void main(String[] argv) {
        //sample data
        final ArrayList<String> ss = new ArrayList<String>();

        String[] s = {"a", "b", "c", "d", "e", "f", "g"};
        for (String z : s) {
            ss.add(z);
        }
        int m = ss.size();

        //parallel-for loop
        Parallel.For(0, m, new LoopBody<Integer>() {
            public void run(Integer i) {
                System.out.println(ss.get(i));
            }
        });

        //parallel-forEach loop         
        Parallel.ForEach(ss, new LoopBody<String>() {
            public void run(String p) {
                System.out.println(p);
            }
        });

        //partitioned parallel loop
        Parallel.ForEach(Parallel.create(0, m), new LoopBody<Partition>() {
            public void run(Partition p) {
                for (int i = p.start; i < p.end; i++) {
                    System.out.println(i + "\t" + ss.get(i));
                }
            }
        });
    }
    /**
     * End of Parallel class
     */
}