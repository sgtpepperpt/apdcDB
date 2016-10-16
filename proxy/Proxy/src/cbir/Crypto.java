package cbir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import mie.crypto.CBIRDCipherKeySpec;
import mie.crypto.CBIRDCipherParameterSpec;
import mie.crypto.CBIRDParameterSpec;
import proxy.Main;

public class Crypto {
	
	private static final String CBIRD_CIPHER = "CBIRDWithSymmetricCipher";
	
	private Key dkey;
	private CBIRDCipherParameterSpec cbirdParams;
	
	Crypto() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
		dkey = setup(Main.IMG_KEY_FILE, CBIRD_CIPHER);
	}
	
	private Key setup(String fileName, String transformation) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException{
		File file = new File(fileName);
		Key key = null;
		if(file.exists()){
			try {
				FileInputStream in = new FileInputStream(file);
				byte[] key_bytes = new byte[in.available()];
				in.read(key_bytes);
				in.close();
				try {
					if(transformation.equalsIgnoreCase(CBIRD_CIPHER)){
						key = new CBIRDCipherKeySpec(key_bytes);
					}
				} catch (InvalidKeyException e) {
					key = null;
					System.err.println("Couldn't read img cbir_cipher key from file");
				}
			} catch (FileNotFoundException e) {
				///should never happen
				e.printStackTrace();
			}
		}
		if(key == null){
			if(transformation.equalsIgnoreCase(CBIRD_CIPHER)){
				KeyGenerator keyGen = KeyGenerator.getInstance(transformation);
				key = keyGen.generateKey();
				FileOutputStream out = new FileOutputStream(fileName);
				out.write(key.getEncoded());
				out.close();
			}
		}
		file = new File(Main.IV_FILE);
		byte[] iv_bytes;
		if(file.exists()){
			///try to read iv from file
			FileInputStream in = new FileInputStream(file);
			iv_bytes = new byte[in.available()];
			in.read(iv_bytes);
			in.close();
		}
		else{
			///generate new iv, iv size is a constant for a given cipher (usually block size) 
			///so getting a cipher instance isn't necessary and makes this slower
			///but solves possible problems if the default cipher changes
			Cipher imgCipher = Cipher.getInstance(transformation);
			int iv_size = imgCipher.getBlockSize();
			iv_bytes = new byte[iv_size];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv_bytes);
			///store iv on a file or decryption won't be possible even with the correct key
			FileOutputStream out = new FileOutputStream(file);
			out.write(iv_bytes);;
			out.close();
		}
		IvParameterSpec iv = new IvParameterSpec(iv_bytes);
		try {
			if(transformation.equalsIgnoreCase(CBIRD_CIPHER)){
				cbirdParams = new CBIRDCipherParameterSpec(iv);
			}		
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return key;
	}

	public byte[] encryptImg(byte[] plain_text) {
		try {
			Cipher cipher = Cipher.getInstance(CBIRD_CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, dkey, cbirdParams);
			return cipher.doFinal(plain_text);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			///should not happen at this point
			e.printStackTrace();
			return null;
		}
	}

	public byte[] decryptImg(byte[] cipher_text) {
		try {
			Cipher imgCipher = Cipher.getInstance(CBIRD_CIPHER);
			imgCipher.init(Cipher.DECRYPT_MODE, dkey, cbirdParams);
			byte[] plain_text = imgCipher.doFinal(cipher_text);
			return plain_text;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			///should not happen at this point
			e.printStackTrace();
			return null;
		}
	}

	public byte[] getCBIRImgFeatures(byte[] img) {
		try {
			Mac cbird = Mac.getInstance("CBIRD");
			cbird.init(((CBIRDCipherKeySpec)dkey).getCBIRSpec());
			cbird.update(img);
			return cbird.doFinal();
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			///should not happen at this point
			e.printStackTrace();
			return null;
		}
	}

	public int getImgFeatureSize() {
		AlgorithmParameters t = cbirdParams.getCBIRParameters();
		try {
			CBIRDParameterSpec t2 = t.getParameterSpec(CBIRDParameterSpec.class);
			return t2.getM();
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
