package com.cpiz.android.playground.TakePicture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

/**
 * 照片操作辅助类
 * <p>
 * Created by caijw on 2015/9/11.
 */
public class PhotoHelper {
    protected static final String PORTRAIT = "PORTRAIT";            // 手机方向
    protected static final String SOURCE_PATH = "SOURCE_PATH";      // 源文件路径，若指定，则只提供裁剪功能
    protected static final String OUTPUT_PATH = "OUTPUT_PATH";      // 输出文件路径
    protected static final String OUTPUT_RATIO = "OUTPUT_RATIO";    // 要求输出图像比例
    protected static final String OUTPUT_QUALITY = "OUTPUT_QUALITY";// 图像保存质量
    protected static final String SOURCE_OF_GALLERY = "GALLERY";    // 用来填入 SOURCE_PATH 中，表示从系统相册选择


    protected static final String SIZE = "SIZE";                    // 最终图像尺寸

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
     * 相机、截图构建器
     */
    public static final class PhotoBuilder {
        private Context mContext;
        private Intent mIntent;

        public PhotoBuilder(Context context) {
            mContext = context;
            mIntent = new Intent(context, CameraActivity.class);
        }

        /**
         * 设置照片界面是否肖像模式（竖屏）
         *
         * @param portrait true竖屏模式，fase横屏模式
         * @return
         */
        public PhotoBuilder setPortrait(boolean portrait) {
            mIntent.putExtra(PORTRAIT, portrait);
            return this;
        }

        /**
         * 设置输入的图片路径
         *
         * @param path 要处理的图片路径，若指定，将只启用裁剪功能，若不指定，将启动相机拍照再进行裁剪
         * @return
         */
        public PhotoBuilder setSourcePath(String path) {
            mIntent.putExtra(SOURCE_PATH, path);
            return this;
        }

        /**
         * 设置从系统相册获得
         * 启动用将自动打开相册界面，与 setSourcePath 将互相覆盖
         *
         * @return
         */
        public PhotoBuilder setSourceGallery() {
            mIntent.putExtra(SOURCE_PATH, SOURCE_OF_GALLERY);
            return this;
        }

        /**
         * 设置输出的图片路径
         *
         * @param path 输出的图片路径，若不指定，将自动生成到图像下的APP目录中
         * @return
         */
        public PhotoBuilder setOutputPath(String path) {
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
        public PhotoBuilder setRatio(int widthRatio, int heightRatio) {
            mIntent.putExtra(OUTPUT_RATIO, new int[]{widthRatio, heightRatio});
            return this;
        }

        /**
         * 设置导出JPG图片的质量
         *
         * @param quality 图像质量，1~100，越高越清晰体积越大
         * @return
         */
        public PhotoBuilder setQuality(int quality) {
            mIntent.putExtra(OUTPUT_RATIO, quality);
            return this;
        }

        /**
         * 启动图片创建界面（相机/裁剪）
         * <p>
         * Activity结束后，通过Intent.getData()获得输出图片路径
         * 通过Intent.getIntArrayExtra(SIZE)获得输出图片尺寸
         */
        public void start() {
            mContext.startActivity(mIntent);
        }
    }
}
