package norswap.core.lexer;

import static norswap.core.lexer.Generation.generate_input_string;

public final class Benchmark
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Given an array of input strings, reports the time in ms it took to lex them all.
     */
    private static void bench (String[] dumps)
    {
        long start = System.nanoTime();
        for (String dump: dumps) Lexer.lex(dump);
        long end = System.nanoTime();
        double time = ((double) ((end - start) / 1000)) / 1000;
        System.out.println("Lexer: " + time + " ms");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a bunch of input strings, and report the time in ms it took to lex them.
     * (Currently: time to lex 100M tokens, spread accross 10k strings).
     */
    public static void main (String[] args)
    {
        int reps = 10000;
        int n = 10000;
        String[] dumps = new String[reps];
        for (int i = 0; i < reps; ++i) dumps[i] = generate_input_string(n);
        bench(dumps);
    }

    // Results
    //
    // MacBook Pro (Retina, 15-inch, Late 2013)
    // Processor 2,3 GHz Intel Core i7
    // Memory 16 GB 1600 MHz DDR3
    // Time: 8 seconds (12.5 MBps)

    // ---------------------------------------------------------------------------------------------
}
