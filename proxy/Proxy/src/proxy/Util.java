package proxy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.DatatypeConverter;

import util.ProxyConfigs;

public class Util {
	static boolean compareArrays(byte[] array1, byte[] array2) {
		if (array1 != null && array2 != null) {
			if (array1.length != array2.length)
				return false;
			else {
				for (int i = 0; i < array1.length; i++) {
					if (array1[i] != array2[i])
						return false;
				}
			}
			return true;
		} else if (array1 == null && array2 == null)
			return true;

		return false;
	}

	public static String toBase64(byte[] arr){
		return DatatypeConverter.printBase64Binary(arr);
	}

	public static byte[] fromBase64(String arr){
		return DatatypeConverter.parseBase64Binary(arr);
	}

	public static byte[] readImg(int id, ProxyConfigs config) {
		return Util.readFile(config.DATASET_DIRECTORY + "flickr_imgs/im" + id + ".jpg");
	}

	public static String readTag(int id, ProxyConfigs config){
		return new String(Util.readFile(config.DATASET_DIRECTORY + "flickr_tags/tags" + id + ".txt")).replace("\r\n", " ").trim();
	}

	public static byte[] readFile(String fileName){
		try {
			return Files.readAllBytes(Paths.get(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean writeFile(String fileName, byte [] content){
		try {
			FileOutputStream out = new FileOutputStream(fileName, false);
			out.write(content);
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}