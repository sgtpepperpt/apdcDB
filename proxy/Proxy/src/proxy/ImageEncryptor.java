package proxy;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import mie.crypto.CBIRDCipherParameterSpec;
//cipherbase
public class ImageEncryptor {
	private static final String CBIR_IMG_NAME = "CBIRDWithSymmetricCipher";

	public static void main(String[] args) throws Exception {
		AlgorithmParameterSpec cipherSpec = new CBIRDCipherParameterSpec("AES/CTR/PKCS7Padding");
		Key key		= generateKey(cipherSpec);
		byte[] pic	= getPictureBytes("/home/user/datasets/flickr_imgs/im0.jpg");

		byte[] encryptedPic		= encryptImage(cipherSpec, key, pic);
		byte[] unencryptedPic	= decryptImage(cipherSpec, key, encryptedPic);
		System.out.println(Util.compareArrays(unencryptedPic, pic));

		String pictureHash = new String(generateHash(pic));
		System.out.println(pictureHash);
	}

	private static Key generateKey(AlgorithmParameterSpec cipherSpec) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {	
		SecureRandom random = new SecureRandom();
		KeyGenerator keyGen = KeyGenerator.getInstance(CBIR_IMG_NAME);
		keyGen.init(cipherSpec, random);

		return keyGen.generateKey();
	}

	private static byte[] encryptImage(AlgorithmParameterSpec cipherSpec, Key key, byte [] unencryptedPic) throws Exception {		
		Cipher cipher = Cipher.getInstance(CBIR_IMG_NAME);
		cipher.init(Cipher.ENCRYPT_MODE, key, cipherSpec);
		byte[] cipherText = cipher.doFinal(unencryptedPic);

		ByteBuffer b = ByteBuffer.wrap(cipherText);
		int cbirLength = b.getInt();
		b.position(cbirLength + 4);
		byte [] encryptedData = new byte[b.remaining()];
		b.get(encryptedData);

		return encryptedData;
	}

	private static byte[] decryptImage(AlgorithmParameterSpec cipherSpec, Key key, byte [] encryptedPic) throws Exception {
		Cipher cipher = Cipher.getInstance(CBIR_IMG_NAME);
		cipher.init(Cipher.DECRYPT_MODE, key, cipherSpec);

		AlgorithmParameters p = cipher.getParameters();
		cipher.init(Cipher.DECRYPT_MODE, key, p);
		return cipher.doFinal(encryptedPic);
	}

	private static byte[] getPictureBytes(String name) throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(name);
		byte[] buffer = new byte[in.available()];
		in.read(buffer);
		in.close();
		return buffer;
	}

	static byte[] generateHash(byte [] pic) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(pic);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}