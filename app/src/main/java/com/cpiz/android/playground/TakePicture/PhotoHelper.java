package com.cpiz.android.playground.TakePicture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 照片操作辅助类
 * <p>
 * Created by caijw on 2015/9/11.
 */
public class PhotoHelper {
    private static final String TAG = "PhotoHelper";

    public static final int REQUEST_TAKE_PICTURE = 805;                 // 启动图片采集界面的 REQUEST_CODE
    public static final String OUTPUT_PATH = "OUTPUT_PATH";             // 输出图片文件路径
    public static final String OUTPUT_SIZE = "OUTPUT_SIZE";             // 输出图片尺寸

    protected static final String PORTRAIT = "PORTRAIT";                // 手机方向
    protected static final String FRONT_CAMERA = "FRONT_CAMERA";        // 使用前置摄像头
    protected static final String SOURCE_PATH = "SOURCE_PATH";          // 源文件路径，若指定，则只提供裁剪功能
    protected static final String OUTPUT_RATIO = "OUTPUT_RATIO";        // 要求输出图像比例
    protected static final String PREFERRED_SIZE = "PREFERRED_SIZE";    // 指定最佳输出尺寸
    protected static final String OUTPUT_QUALITY = "OUTPUT_QUALITY";    // 图像保存质量
    protected static final String SOURCE_OF_GALLERY = "GALLERY";        // 用来填入 SOURCE_PATH 中，表示从系统相册选择

    private static Bitmap cacheBitmap;

    public static Bitmap getCacheBitmap() {
        return cacheBitmap;
    }

    public static void setCacheBitmap(Bitmap cacheBitmap) {
        PhotoHelper.cacheBitmap = cacheBitmap;
    }

    public static void clearCacheBitmap() {
        if (cacheBitmap != null) {
            cacheBitmap.recycle();
            cacheBitmap = null;
        }
    }

    /**
     * 创基一个相机构建器，设置好参数后通过 start() 启动界面
     */
    public static CameraLauncher create(Activity activity) {
        return new CameraLauncher(activity)
                .setPortrait(true)
                .setUseFrontCamera(false)
                .setRatio(1, 1);
    }


    public static final class CameraLauncher {
        private Activity mActivity;
        private Intent mIntent;

        private CameraLauncher(Activity activity) {
            mActivity = activity;
            mIntent = new Intent(activity, CameraActivity.class);
        }

        /**
         * 设置照片界面是否肖像模式（竖屏）
         *
         * @param portrait true竖屏模式，fase横屏模式
         * @return
         */
        public CameraLauncher setPortrait(boolean portrait) {
            mIntent.putExtra(PORTRAIT, portrait);
            return this;
        }

        /**
         * 设置是否使用前置摄像头
         *
         * @return
         */
        public CameraLauncher setUseFrontCamera(boolean front) {
            mIntent.putExtra(FRONT_CAMERA, front);
            return this;
        }

        /**
         * 设置输入的图片路径
         *
         * @param path 要处理的图片路径，若指定，将只启用裁剪功能，若不指定，将启动相机拍照再进行裁剪
         * @return
         */
        public CameraLauncher setSourcePath(String path) {
            mIntent.putExtra(SOURCE_PATH, path);
            return this;
        }

        /**
         * 设置从系统相册获得
         * 启动用将自动打开相册界面，与 setSourcePath 将互相覆盖
         *
         * @return
         */
        public CameraLauncher setSourceGallery() {
            mIntent.putExtra(SOURCE_PATH, SOURCE_OF_GALLERY);
            return this;
        }

        /**
         * 设置输出的图片路径
         *
         * @param path 输出的图片路径，若不指定，将自动生成到图像下的APP目录中
         * @return
         */
        public CameraLauncher setOutputPath(String path) {
            mIntent.putExtra(OUTPUT_PATH, path);
            return this;
        }

        /**
         * 设置要生成图片的纵横比例
         *
         * @param widthRatio
         * @param heightRatio
         * @return
         */
        public CameraLauncher setRatio(int widthRatio, int heightRatio) {
            mIntent.putExtra(OUTPUT_RATIO, new int[]{widthRatio, heightRatio});
            return this;
        }

        /**
         * 设置输出图片的最佳尺寸，请确保比例与 setRatio 保持一致
         * 输出图片将尽可能接近设定大小
         *
         * @param preferredWidth
         * @param preferredHeight
         * @return
         */
        public CameraLauncher setPreferredSize(int preferredWidth, int preferredHeight) {
            mIntent.putExtra(PREFERRED_SIZE, new int[]{preferredWidth, preferredHeight});
            return this;
        }

        /**
         * 设置导出JPG图片的质量
         *
         * @param quality 图像质量，1~100，越高越清晰体积越大
         * @return
         */
        public CameraLauncher setQuality(int quality) {
            mIntent.putExtra(OUTPUT_QUALITY, quality);
            return this;
        }

        /**
         * 启动图片创建界面（相机/裁剪）
         * <p>
         * Activity结束后，根据 REQUEST_TAKE_PICTURE 辨别返回结果
         * 通过Intent.getData()获得输出图片路径
         * 通过Intent.getIntArrayExtra(OUTPUT_SIZE)获得输出图片尺寸
         */
        public void start() {
            mActivity.startActivityForResult(mIntent, REQUEST_TAKE_PICTURE);
        }
    }

    /**
     * 创建一个图片文件
     *
     * @param postfix
     * @return
     */
    public static File newPictureFile(String postfix) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        if (postfix != null) {
            imageFileName += "_" + postfix;
        }

        File storageDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        storageDirFile = new File(storageDirFile.getAbsolutePath());
        try {
            if (!storageDirFile.exists()) {
                if (storageDirFile.mkdirs()) {
                    Log.d(TAG, "Create dir success: " + storageDirFile.getAbsolutePath());
                } else {
                    Log.w(TAG, "Create dir failed: " + storageDirFile.getAbsolutePath());
                    return null;
                }
            }

            Log.d(TAG, String.format("File name: %s, directory: %s", imageFileName, storageDirFile.getAbsolutePath()));

            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDirFile      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            String path = image.getAbsolutePath();

            Log.d(TAG, "Picture path: " + path);

            return image;
        } catch (IOException ioe) {
            Log.w(TAG, "create image file failed.", ioe);
            return null;
        }
    }

}
