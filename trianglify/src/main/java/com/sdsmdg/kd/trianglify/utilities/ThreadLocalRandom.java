package com.sdsmdg.kd.trianglify.utilities;

/**
 * <h1>Thread Local Random</h1>
 * <b>Description : Generates Random numbers that are local to a thread thus reducing instructions
 * needed to generate a random number.</b>
 *
 * @author suyash
 * @since 2/5/17.
 */

public class ThreadLocalRandom {
    private long seed = 0x5DEECE66DL;

    public ThreadLocalRandom() {

    }

    public ThreadLocalRandom(long seed) {
        this.seed = seed;
    }

    /**
     * Generates a pseudoRandom integer that has computational costs of several instructions less
     * than that of java.util.random.nextInt().
     *
     * @param mod limit for generation of pseudo random number
     * @return Random number between 0 and mod (exclusive)
     */
    public int nextInt(int mod) {
        if (mod != 0) {
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            return (int) (seed % mod);
        }
        return 0;
    }

}
