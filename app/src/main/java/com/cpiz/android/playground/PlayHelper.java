package com.cpiz.android.playground;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by caijw on 2015/9/11.
 */
public class PlayHelper {
    /**
     * 获得手机中最新一张照片
     *
     * @param context
     * @return
     */
    public static String getLatestPicture(Context context) {
        String filePath = null;

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        // Put it in the image view
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(1);
        }
        cursor.close();

        return filePath;
    }
}
