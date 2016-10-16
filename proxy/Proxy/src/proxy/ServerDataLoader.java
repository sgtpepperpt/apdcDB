package proxy;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import cryptDB.Proxy;

public class ServerDataLoader {
	static void initializeCryptDB(Proxy cryptDB){
		//reset db first
		cryptDB.queryCryptDBProxy("DELETE FROM images");

		try {
			for(int id = 0; id < 1000; id++){
				byte[] unencryptedImg = Util.readImg(id);
				
				String hash	= DatatypeConverter.printHexBinary(ImageEncryptor.generateHash(unencryptedImg));
				String tags	= Util.readTag(id);
				String[] tag2 = tags.split(" ");
				String tag = tag2[new Random().nextInt(tag2.length)];
				
				//send data to servers (mysql (through proxy) and cbir server)
				cryptDB.queryCryptDBProxy(String.format("INSERT INTO images (id, tags, hash) VALUES (%d,'%s','%s')", id, tag, hash));
				
				System.out.printf("Added image %d!\n", id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}