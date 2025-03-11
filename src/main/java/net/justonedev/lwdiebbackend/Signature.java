/* (C)2025 */
package net.justonedev.lwdiebbackend;

public final class Signature {
	private final String SIGNATURE_SECRET;

	public Signature(String signatureSecret) {
		this.SIGNATURE_SECRET = signatureSecret;
	}

	/**
	 * Computes the signature for a given string and outputs it as HighBits#LowBits,
	 * as unsigned decimal values.
	 * 
	 * @param data The string to compute a signature for
	 * @return The signature
	 */
	public String signAsString(String data) {
		long signature = sign(data);
		return ((signature >> 32L) & 0xFFFFFFFFL) + "#" + (signature & 0xFFFFFFFFL);
	}

	/**
	 * Computes the signature for a given string.
	 * 
	 * @param data The string to compute a signature for
	 * @return The signature
	 */
	public long sign(String data) {
		long first = generateHash(data.substring(0, data.length() / 3));
		long second = generateHash(data.substring(data.length() / 4, (int) Math.round(data.length() / 1.5)));
		long third = generateHash(data.substring(data.length() / 2, data.length() - data.length() / 4));
		long fourth = generateHash(data.substring(data.length() / 3, data.length() - data.length() / 5 - 1));
		long sign = (first * second * third * fourth + third * fourth);

		return finalizeSignature(sign == 0 ? 1 : sign);
	}

	private long generateHash(String data) {
		long product = 1;
		for (char c : data.toCharArray()) {
			long num = c << 2;
			product *= ((num + (num << 2)) >> 1);
			product += ((num + (num << 7)) >> 2);
		}
		return product;
	}

	private long finalizeSignature(long signature) {
		String secret = SIGNATURE_SECRET == null ? "3" : SIGNATURE_SECRET;
		System.out.println(secret);
		if (secret.length() < 10)
			return signature * Long.parseLong(secret);
		int index = 0;
		while (index < secret.length()) {
			var nextIndex = index + Math.min(secret.length() - index, (int) Math.ceil((secret.charAt(index) - '0') / 2d) + 11); // Maximum number is 5, max
																																// digits is 17
			var subStr = secret.substring(index, nextIndex);
			signature = signature * Long.parseLong(subStr);
			index = nextIndex;
		}
		return signature;
	}
}
