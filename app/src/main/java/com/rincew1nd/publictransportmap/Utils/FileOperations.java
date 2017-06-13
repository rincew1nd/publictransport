package com.rincew1nd.publictransportmap.Utils;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileOperations {
    public static void CopyRAWtoSDCard(int id, String path, Context context) throws IOException {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }
}
