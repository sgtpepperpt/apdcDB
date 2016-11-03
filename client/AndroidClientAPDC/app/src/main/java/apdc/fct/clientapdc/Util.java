package apdc.fct.clientapdc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Util {
    public static byte[] fullyReadFileToBytes(File f) {
        int totalSize   = (int) f.length();
        byte [] bytes   = new byte[totalSize];
        byte [] tmpBuff = new byte[totalSize];

        try {
            FileInputStream fis = new FileInputStream(f);
            int read = fis.read(bytes, 0, totalSize);
            if (read < totalSize) {
                int remaining = totalSize - read;
                while (remaining > 0) {
                    read = fis.read(tmpBuff, 0, remaining);
                    System.arraycopy(tmpBuff, 0, bytes, totalSize - remaining, read);
                    remaining -= read;
                }
            }
            fis.close();
        } catch (IOException e) {
            return null;
        }
        return bytes;
    }
}