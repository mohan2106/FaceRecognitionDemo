package com.example.facerecognition.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ImageUtil {
    private String directoryName = "FaceDetectorApp";
    private String fileName = "user.png";
    private Context context;
    private boolean external;

    public ImageUtil(Context context) {
        this.context = context;
    }

    public ImageUtil setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ImageUtil setExternal(boolean external) {
        this.external = external;
        return this;
    }

    public ImageUtil setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }

    public void save(Bitmap bitmapImage) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFile());
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            Log.d("Mohan","File has been saved");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Mohan","Error in saving file : " + e.getMessage());
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    Log.d("Mohan","File has been closed");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Mohan","Error in closing file");
            }
        }
    }

    @NonNull
    private File createFile() {
//        File directory;
//        if(external){
//            directory = getAlbumStorageDir(directoryName);
//        }
//        else {
//            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
//        }
//        if(!directory.exists() && !directory.mkdirs()){
//            Log.e("ImageUtil","Error creating directory " + directory);
//        }
//
//        return new File(directory, fileName);
        String filePath;
        // If the SD card already exists, store it; otherwise there is a data directory
        if (isMountedSDCard()) {
            // SD卡路径
            filePath = Environment.getExternalStorageDirectory() + File.separator + directoryName;
        } else {
            filePath = context.getCacheDir().getPath() + File.separator + directoryName;
        }
        File destDir = new File(filePath);
        if (!destDir.exists()) {
            boolean isCreate = destDir.mkdirs();
            Log.i("FileUtils", filePath + " has created. " + isCreate);
        }
        return new File(destDir,fileName);
    }

    public static boolean isMountedSDCard() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            return true;
        } else {
            Log.w(TAG, "SDCARD is not MOUNTED !");
            return false;
        }
    }

    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public Bitmap load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            Log.d("Mohan","Successfully readed image");
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Mohan","Error in reading file : " + e.getMessage() );
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    Log.d("Mohan","Successfully closed file image");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Mohan","Error while closign file image");
            }
        }
        return null;
    }
}
