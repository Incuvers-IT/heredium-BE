package art.heredium.hanabank;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HanaParams {
	private static final int AES_BLOCK_SIZE = 16;
	private static final int AES256_KEY_SIZE = 32;
	private static final int HMACSHA256_KEY_SIZE = 32;

	public static Map<String, String> encrypt(String key, String salt, String nonceParam, String data) throws HanaParamsException {

		final byte[] cipherKey;
		final byte[] hashKey;
		{
			final byte[] keySource;
			try {
				keySource = Base64.getDecoder().decode(key);
			} catch (IllegalArgumentException e) {
				throw new HanaParamsException("key has illegal character", e);
			}

			if ((keySource.length < AES256_KEY_SIZE) || (keySource.length < HMACSHA256_KEY_SIZE)) {
				throw new HanaParamsException("key is short");
			}

			cipherKey = new byte[AES256_KEY_SIZE];
			System.arraycopy(keySource, 0, cipherKey, 0, AES256_KEY_SIZE);

			hashKey = new byte[HMACSHA256_KEY_SIZE];
			System.arraycopy(keySource, 0, hashKey, 0, HMACSHA256_KEY_SIZE);
		}

		final String nonce = nonceParam;
		final byte[] cipherIv;
		final byte[] hmacKey;
		{
			final byte[] random = hmacSha256(hashKey, (salt + "|#" + nonce).getBytes(StandardCharsets.UTF_8));
			assert random.length >= AES_BLOCK_SIZE;
			assert random.length >= HMACSHA256_KEY_SIZE;

			cipherIv = new byte[AES_BLOCK_SIZE];
			System.arraycopy(random, 0, cipherIv, 0, AES_BLOCK_SIZE);

			hmacKey = new byte[HMACSHA256_KEY_SIZE];
			System.arraycopy(random, 0, hmacKey, 0, HMACSHA256_KEY_SIZE);
		}

		final byte[] cipher = aes256CbcP5encrypt(cipherKey, cipherIv, data.getBytes(StandardCharsets.UTF_8));

		final byte[] mac = hmacSha256(hmacKey, cipher);
		Map<String, String> map = new HashMap<>();
		map.put("message", new String(Base64.getEncoder().encode(cipher), StandardCharsets.UTF_8));
		map.put("mac", new String(Base64.getEncoder().encode(mac), StandardCharsets.UTF_8));
		map.put("nonce", nonce);
		return map;
	}

	public static String parse(String key, String salt, String messageParam, String macParam, String nonceParam)
			throws HanaParamsException {
		final byte[] cipher;
		try {
			cipher = Base64.getDecoder().decode(messageParam);
		} catch (IllegalArgumentException e) {
			throw new HanaParamsException("messageParam has illegal character", e);
		}

		final byte[] mac;
		try {
			mac = Base64.getDecoder().decode(macParam);
		} catch (IllegalArgumentException e) {
			throw new HanaParamsException("macParam has illegal character", e);
		}

		final String nonce = nonceParam;

		final byte[] cipherKey;
		final byte[] hashKey;
		{
			final byte[] keySource;
			try {
				keySource = Base64.getDecoder().decode(key);
			} catch (IllegalArgumentException e) {
				throw new HanaParamsException("key has illegal character", e);
			}

			if ((keySource.length < AES256_KEY_SIZE) || (keySource.length < HMACSHA256_KEY_SIZE)) {
				throw new HanaParamsException("key is short");
			}

			cipherKey = new byte[AES256_KEY_SIZE];
			System.arraycopy(keySource, 0, cipherKey, 0, AES256_KEY_SIZE);

			hashKey = new byte[HMACSHA256_KEY_SIZE];
			System.arraycopy(keySource, 0, hashKey, 0, HMACSHA256_KEY_SIZE);
		}

		final byte[] cipherIv;
		final byte[] hmacKey;
		{
			final byte[] random = hmacSha256(hashKey, (salt + "|#" + nonce).getBytes(StandardCharsets.UTF_8));
			assert random.length >= AES_BLOCK_SIZE;
			assert random.length >= HMACSHA256_KEY_SIZE;

			cipherIv = new byte[AES_BLOCK_SIZE];
			System.arraycopy(random, 0, cipherIv, 0, AES_BLOCK_SIZE);

			hmacKey = new byte[HMACSHA256_KEY_SIZE];
			System.arraycopy(random, 0, hmacKey, 0, HMACSHA256_KEY_SIZE);
		}

		final byte[] mac2 = hmacSha256(hmacKey, cipher);

		if (!Arrays.equals(mac, mac2)) {
			throw new HanaParamsDamagedException("mac is not match");
		}

		return new String(aes256CbcP5Decrypt(cipherKey, cipherIv, cipher), StandardCharsets.UTF_8);
	}

	private static byte[] aes256CbcP5encrypt(byte[] key, byte[] iv, byte[] data) throws HanaParamsException {
		assert key.length == AES256_KEY_SIZE;
		assert iv.length == AES_BLOCK_SIZE;

		final Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException("bug", e);
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new HanaParamsException("cipher init failed", e);
		}

		try {
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new HanaParamsException("decrypt failed", e);
		}
	}

	private static byte[] aes256CbcP5Decrypt(byte[] key, byte[] iv, byte[] encrypted) throws HanaParamsException {
		assert key.length == AES256_KEY_SIZE;
		assert iv.length == AES_BLOCK_SIZE;

		final Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException("bug", e);
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new HanaParamsException("cipher init failed", e);
		}

		try {
			return cipher.doFinal(encrypted);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new HanaParamsException("decrypt failed", e);
		}
	}

	private static byte[] hmacSha256(byte[] key, byte[] message) throws HanaParamsException {
		assert key.length == HMACSHA256_KEY_SIZE;

		final Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("bug", e);
		}

		try {
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
		} catch (InvalidKeyException e) {
			throw new HanaParamsException("mac init failed", e);
		}

		return mac.doFinal(message);
	}
}
