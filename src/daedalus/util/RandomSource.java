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
	private static final long a = 1103515245;
	private static final long b = 12345;
	private static final long m = 1 << 31;
	
	/**
	 * Current value
	 */
	private static long current = System.nanoTime();
	
	/**
	 * Get next int value (just cast from long)
	 */
	public static int nextInt() {
		return (int) nextLong();
	}
	
	/**
	 * Get next long value
	 * For algorithm, look at:
	 * http://en.wikipedia.org/wiki/Linear_congruential_generator
	 */
	public static long nextLong() {
		current = (a * current + b) & (m - 1);
		return current;
	}
}
