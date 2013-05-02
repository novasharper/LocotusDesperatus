package daedalus.util;

/**
 * Custom random source
 * Using a LCG with glibc constants
 * Seeded with System.nanoTime
 */
public class RandomSource {
	/**
	 * glibc constants
	 */
	public static final long a = 1103515245;
	public static final long b = 12345;
	public static final long m = 1 << 31;
	
	/**
	 * Current value
	 */
	private long current;

    
    public RandomSource(long seed) {
        current = seed;
        System.out.println(seed);
    }

    public RandomSource() {
        this(System.nanoTime());
    }
	
	/**
	 * Get next int value (just cast from long)
	 */
	public int nextInt() {
		return (int) nextLong();
	}
	
	/**
	 * Get next long value
	 * For algorithm, look at:
	 * http://en.wikipedia.org/wiki/Linear_congruential_generator
	 */
	public long nextLong() {
		current = Math.abs((a * current + b) & (m - 1));
		return current;
	}

    public double nextDouble() {
        return Math.abs((nextLong() & (m - 1)) * 1.0) / m;
    }
}

