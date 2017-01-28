package com.example.yonko.funnyface.utils.async.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.yonko.funnyface.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveBitmapToDeviceTask extends AsyncTask<Bitmap, Void, String> {
    private static final String TAG = SaveBitmapToDeviceTask.class.getSimpleName();

    public interface SaveBitmapToDeviceCallback {
        void onPreExecute();
        void onPostExecute(String data);
    }

    private final Context context;
    private boolean savedOnSD = false;
    private SaveBitmapToDeviceCallback saveBitmapToDeviceCallback = null;

    public SaveBitmapToDeviceTask(Context context, SaveBitmapToDeviceCallback saveBitmapToDeviceCallback){
        this.context = context;
        this.saveBitmapToDeviceCallback = saveBitmapToDeviceCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(saveBitmapToDeviceCallback != null) {
            saveBitmapToDeviceCallback.onPreExecute();
        }
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        String path = insertImageIntoGallery(context.getContentResolver(), bitmaps[0], context.getString(R.string.image_gallery_title),
                context.getString(R.string.card_gallery_label));
        bitmaps[0].recycle();
        return path;
    }

     /*
     * A copy of the Android internals MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String).
     * With Android internal insertImage DATE_ADDED and DATE_TAKEN are not populated and image is saved
     * on the bottom of the Gallery and this custom method fixed this
     * If the MediaStore not available, redirect the file to alternative source, the SD card.
     **/
    public String insertImageIntoGallery(ContentResolver cr, Bitmap source, String title, String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = null;
                if (url != null) {
                    imageOut = cr.openOutputStream(url);
                }
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    if (imageOut != null) {
                        imageOut.close();
                    }
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                return storeToAlternateSd(source, title);
                // url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                return storeToAlternateSd(source, title);
                // url = null;
            }
        }

        savedOnSD = false;
        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

     /*
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage.
     * The StoreThumbnail method is private so it must be duplicated here.
     **/
    private Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = null;
            if (url != null) {
                thumbOut = cr.openOutputStream(url);
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            }
            if (thumbOut != null) {
                thumbOut.close();
            }
            return thumb;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*
     * Backup method if MediaStore not exist.
     * @return - the file's path
    * */
    private String storeToAlternateSd(Bitmap src, String title){
        if(src == null)
            return null;

        File sdCardDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "FunnyFace");
        if(!sdCardDirectory.exists())
            sdCardDirectory.mkdir();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy - (hh.mm.a)", Locale.US);
        File image = new File(sdCardDirectory, title + " -- [" + sdf.format(new Date()) + "].jpg");
        try {
            FileOutputStream imageOut = new FileOutputStream(image);
            src.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);
            imageOut.close();
            savedOnSD = true;
            return image.getAbsolutePath();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onPostExecute(String url){
        if(url != null){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if(savedOnSD){
                File file = new File(url);
                if(file.exists())
                    intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                else
                    return;
            }
            else
                intent.setDataAndType(Uri.parse(url), "image/jpeg");

            context.startActivity(intent);
        }
        else
            Toast.makeText(context, context.getString(R.string.error_compressing), Toast.LENGTH_SHORT).show();

        if(saveBitmapToDeviceCallback != null) {
            saveBitmapToDeviceCallback.onPostExecute(null);
        }
    }

}
