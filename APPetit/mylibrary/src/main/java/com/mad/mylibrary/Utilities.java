package com.mad.mylibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;


public class Utilities {

    public static File reizeImageFileWithGlide(String path) throws ExecutionException, InterruptedException, IOException {
        File imgFile = new File(path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        Bitmap resized = Bitmap.createScaledBitmap(myBitmap,
                (int) (myBitmap.getWidth() * 0.8),
                (int)(myBitmap.getHeight()*0.8),
                true);

        File file = new File("prova.png");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileOutputStream fos = new FileOutputStream(file);
        resized.compress(Bitmap.CompressFormat.PNG, 10,bos);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        return file;
    }


}
