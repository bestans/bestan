package bestan.common.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import bestan.common.log.LogManager;
import bestan.common.log.Logger;


/**
 * Implementation of a RSA key.
 */
public class RSAPublicKey {
	private static final Logger logger = LogManager.getLogger(RSAPublicKey.class);

	/** 0 */
	public static final BigInteger big0 = new BigInteger("0");

	/** 1 */
	public static final BigInteger big1 = new BigInteger("1");

	/** 2 */
	public static final BigInteger big2 = new BigInteger("2");

	/** 6 */
	public static final BigInteger big6 = new BigInteger("6");

	/** n */
	protected BigInteger n;

	/** e */
	protected BigInteger e;

	/**
	 * creates a new RSAPublicKey object
	 *
	 * @param n n
	 * @param e e
	 */
	public RSAPublicKey(BigInteger n, BigInteger e) {
		this.n = n;
		this.e = e;
	}

	/**
	 * prints the key to a writer
	 *
	 * @param out writer to print to
	 */
	public void print(PrintWriter out) {
		out.println("n = " + n);
		out.println("e = " + e);
	}

	/**
	 * prints the key to a stream
	 *
	 * @param out stream to print to
	 */
	public void print(PrintStream out) {
		out.println("n = " + n);
		out.println("e = " + e);
	}

	/**
	 * gets n
	 *
	 * @return n
	 */
	public BigInteger getN() {
		return n;
	}

	/**
	 * get e
	 *
	 * @return e
	 */
	public BigInteger getE() {
		return e;
	}

	/**
	 * encodes a BigInteger
	 *
	 * @param message BigInteger
	 * @return encoded BigInteger
	 */
	public BigInteger encode(BigInteger message) {
		return message.modPow(e, n);
	}

	/**
	 * encodes an array
	 *
	 * @param message array
	 * @return encoded array
	 */
	public byte[] encodeByteArray(byte[] message) {
		return encode(Hash.bytesToBigInt(message)).toByteArray();
	}

	/**
	 * verifies a signature
	 *
	 * @param message BigInteger
	 * @param signature BigInteger
	 * @return true, if the signature is correct; false otherwise
	 */
	public boolean verifySignature(BigInteger message, BigInteger signature) {
		return message.equals(encode(signature));
	}

	/**
	 * converts a string into a BigInteger
	 *
	 * @param str to convert
	 * @return BigInteger
	 */
	public static BigInteger getValue(String str) {
		try {
			byte[] v = str.getBytes("US-ASCII");
			for (byte b : v) {
				if (b != 0) {
					return new BigInteger(1, v);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return big0;
	}

	/**
	 * converts a BigInteger into a string
	 *
	 * @param value BigInteger
	 * @return String
	 */
	public static String getString(BigInteger value) {
		try {
			return new String(value.toByteArray(), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
