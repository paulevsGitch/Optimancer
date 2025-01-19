package paulevs.optimancer.helper;

public class OptimancerMathHelper {
	public static long pack(int x, int z) {
		return (long) x << 32L | (long) z & 0xFFFFFFFFL;
	}
	
	public static int getX(long index) {
		return (int) (index >> 32);
	}
	
	public static int getZ(long index) {
		return (int) index;
	}
}
