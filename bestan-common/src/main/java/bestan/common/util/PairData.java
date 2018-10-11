package bestan.common.util;

/**
 * @author yeyouhuan
 *
 */
public class PairData<K, V> {
	public K first;
	public V second;
	
	public PairData(K first, V second) {
		this.first = first;
		this.second = second;
	}
	
	public static <K, V> PairData<K, V> newPair(K first, V second) {
		return new PairData<>(first, second);
	}
}
