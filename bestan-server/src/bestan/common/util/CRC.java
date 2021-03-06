/***************************************************************************
 *                  CRC algorithm                                          *
 ***************************************************************************/
package bestan.common.util;

/**
 * an implementation of the CRC checksum algorithm.
 */
public class CRC {

	// calculating 16-bit CRC

	/**
	 * generator polynomial
	 */
	private static final int POLY = 0x1021; /*
	                                        * x16 + x12 + x5 + 1 generator
	                                        * polynomial
	                                        */

	/* 0x8408 used in European X.25 */

	/**
	 * scrambler lookup table for fast computation.
	 */
	private static int[] crcTable = new int[256];
	static {
		// initialise scrambler table
		for (int i = 0; i < 256; i++) {
			int fcs = 0;
			int d = i << 8;
			for (int k = 0; k < 8; k++) {
				if (((fcs ^ d) & 0x8000) != 0) {
					fcs = (fcs << 1) ^ POLY;
				} else {
					fcs = (fcs << 1);
				}
				d <<= 1;
				fcs &= 0xffff;
			}
			crcTable[i] = fcs;
		}
	}

	/**
	 * Calc CRC with cmp method.
	 *
	 * @param b
	 *            byte array to compute CRC on
	 *
	 * @return 16-bit CRC, signed
	 */
	public static short cmpCRC(byte[] b) {
		// loop, calculating CRC for each byte of the string
		int work = 0xffff;
		for (int i = 0; i < b.length; i++) {
			work = (crcTable[((work >> 8)) & 0xff] ^ (work << 8) ^ (b[i] & 0xff)) & 0xffff;
		}

		return (short) work;
	}
}
