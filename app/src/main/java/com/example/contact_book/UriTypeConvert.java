package com.example.contact_book;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UriTypeConvert {
    private final Context mContext;

    UriTypeConvert(Context context){
        mContext = context;
    }

    /**
     * 将content类型的Uri转化为file类型的Uri
     * @param uri content类型Uri
     * @return 文件类型Uri
     */
    public Uri convertUri(Uri uri){
        InputStream inputStream;
        try{
            inputStream = mContext.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return saveBitmap(bitmap);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Bitmap写入存储中的一个文件中，并返回写入文件的Uri
     * @param bitmap 被保存的Bitmap
     * @return 文件的Uri
     */
    public Uri saveBitmap(Bitmap bitmap) {
        File img = new File(mContext.getExternalCacheDir(),"avatar_image.png");
        try{
            if(!img.exists() && img.createNewFile())
                Log.d("My","createNewFile Success");
            FileOutputStream fileOutputStream=new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.PNG,70,fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            //需要指定Activity名
            return FileProvider.getUriForFile(mContext,"com.example.contact_book.fileprovider",img);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
