package com.cpiz.android.playground.TakePicture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.cpiz.android.controls.ImageButtonEx;
import com.cpiz.android.playground.R;
import com.cpiz.android.playground.TakePicture.cropper.CropImageView;
import com.cpiz.android.utils.FileUtils;
import com.cpiz.android.utils.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 提供拍照、闪关灯、相册选择、二次确认、旋转、裁剪功能
 * <p/>
 * 使用 startActivityForResult 启动，可在 Intent 中 setData 指定图片存储路径，不指定则会自动创建路径
 * 通过 onActivityResult 的 Intent.getData 获得输出图片路径，getIntArrayExtra(PIC_SIZE) 可获得图片尺寸
 * <p/>
 * Created by caijw on 2015/9/12.
 */
public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";

    public static final String PIC_SIZE = "PIC_SIZE";

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";

    private CameraSurfaceView mCameraSurfaceView;
    private CropImageView mCropImageView;
    private View mHorizontalHintText;
    private View mPreviewClipLayout;
    private ImageButtonEx cameraFlashBtn;
    private ImageButtonEx mTakePhotoBtn;
    private ImageButtonEx mGalleryBtn;
    private ImageButtonEx mCancelCropBtn;
    private ImageButtonEx mConfirmCropBtn;
    private ImageButtonEx mRotateBtn;

    private File mPictureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Uri fileUri = getIntent().getData();
        if (fileUri != null) {
            mPictureFile = new File(fileUri.getPath());
        } else {
            // 若无外部传入，则创建临时图片
            mPictureFile = FileUtils.createTempImageFile(null);
        }

        if (mPictureFile != null) {
            Log.i(TAG, String.format("Set picture file=[%s]", mPictureFile.getAbsolutePath()));
        } else {
            ToastUtils.show(this, "Invalid image file");
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPreviewClipLayout = findViewById(R.id.previewClipLayout);
        mPreviewClipLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCameraSurfaceView.setClipRect(new Rect(left, top, right, bottom));
            }
        });

        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        mCameraSurfaceView.setOnRotationListener(new CameraSurfaceView.OnRotationListener() {
            @Override
            public void onRotate(int newRotation, int oldRotation) {
                toggleHorizontalHint(newRotation != Surface.ROTATION_270);
            }
        });

        mHorizontalHintText = findViewById(R.id.horizontalHintText);

        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
        mCropImageView.setGuidelines(2);    // no guide lines
        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setAspectRatio(4, 3);


        cameraFlashBtn = (ImageButtonEx) findViewById(R.id.cameraFlashBtn);
        cameraFlashBtn.setOnCheckedChangeListener(new ImageButtonEx.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ImageButtonEx button, boolean b) {
                if (b) {
                    mCameraSurfaceView.setFlashMode(CameraSurfaceView.FlashMode.FLASH_ON);
                } else {
                    mCameraSurfaceView.setFlashMode(CameraSurfaceView.FlashMode.FLASH_OFF);
                }
            }
        });

        mTakePhotoBtn = (ImageButtonEx) findViewById(R.id.shutterBtn);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCameraButtonsEnabled(false); // 禁用拍照控制按钮，避免连击
                mCameraSurfaceView.takePicture(new CameraSurfaceView.TakePictureCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.v(TAG, "take picture step 4: on onSuccess");
                        CameraModel.Instance.setCacheBitmap(bitmap);
                        switchToCropMode(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError", e);
                        ToastUtils.show(CameraActivity.this, String.format("Take photo failed[%s]", e == null ? "" : e.getMessage()));
                        setCameraButtonsEnabled(true);
                    }
                });
            }
        });

        mGalleryBtn = (ImageButtonEx) findViewById(R.id.galleryBtn);
        mGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CameraActivity.this.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            }
        });

        mRotateBtn = (ImageButtonEx) findViewById(R.id.rotateBtn);
        mRotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.rotateImage(90);
            }
        });

        mCancelCropBtn = (ImageButtonEx) findViewById(R.id.cancelPictureBtn);
        mCancelCropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraModel.Instance.clearCacheBitmap();
                switchToCameraMode();
            }
        });

        mConfirmCropBtn = (ImageButtonEx) findViewById(R.id.confirmPictureBtn);
        mConfirmCropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedBitmap = mCropImageView.getCroppedImage();

                if (saveBitmap2File(croppedBitmap, mPictureFile)) {
                    CameraModel.Instance.setCacheBitmap(croppedBitmap);
                    // 照片选取完成，结束Activity，通过Intent返回数据
                    Intent resultIntent = new Intent();
                    resultIntent.setData(Uri.fromFile(mPictureFile));
                    resultIntent.putExtra(PIC_SIZE, new int[]{croppedBitmap.getWidth(), croppedBitmap.getHeight()});
                    setResult(RESULT_OK, resultIntent);
                    CameraActivity.this.finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 从相册取得照片
        if (REQUEST_IMAGE_GALLERY == requestCode) {
            if (RESULT_OK != resultCode) {
                return;
            }

            switchToCropMode(false);

            Uri uri = data.getData();
            if (uri != null) {
                File originalFile = getFromMediaUri(getContentResolver(), uri);
                mCropImageView.setImageFile(originalFile.getAbsolutePath());
            }
        }
    }

    /**
     * 禁用控制按钮
     *
     * @param enabled
     */
    private void setCameraButtonsEnabled(boolean enabled) {
        cameraFlashBtn.setEnabled(enabled);
        mTakePhotoBtn.setEnabled(enabled);
        mGalleryBtn.setEnabled(enabled);
    }

    /**
     * 将裁剪后的Bitmap保存为jpg图片
     *
     * @param bitmap
     * @param file
     * @return
     */
    private boolean saveBitmap2File(Bitmap bitmap, File file) {
        if (file == null) {
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            boolean saveSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
            fos.close();
            return saveSuccess;
        } catch (IOException e) {
            Log.e(TAG, "save picture error", e);
            return false;
        }
    }

    /**
     * 切换到拍照视图
     */
    private void switchToCameraMode() {
        toggleButtons(true);
    }

    /**
     * 切换到裁剪视图
     *
     * @param confirmOnly 是否仅确认图像，而不进行裁剪
     */
    private void switchToCropMode(boolean confirmOnly) {
        toggleButtons(false);

        mCropImageView.setImageBitmap(CameraModel.Instance.getCacheBitmap());
        mCropImageView.setCropEnabled(true);
    }

    private void toggleButtons(boolean camera) {
        mCropImageView.setVisibility(camera ? View.INVISIBLE : View.VISIBLE);
        mCameraSurfaceView.setVisibility(camera ? View.VISIBLE : View.INVISIBLE);
        mTakePhotoBtn.setVisibility(camera ? View.VISIBLE : View.INVISIBLE);
        cameraFlashBtn.setVisibility(camera ? View.VISIBLE : View.GONE);
        mGalleryBtn.setVisibility(camera ? View.VISIBLE : View.GONE);
        mCancelCropBtn.setVisibility(camera ? View.GONE : View.VISIBLE);
        mConfirmCropBtn.setVisibility(camera ? View.GONE : View.VISIBLE);
        mRotateBtn.setVisibility(camera ? View.GONE : View.VISIBLE);
        setCameraButtonsEnabled(camera);
    }

    private void toggleHorizontalHint(boolean visible) {
        if (visible != mHorizontalHintText.isShown()) {
            if (visible) {
                mHorizontalHintText.clearAnimation();
                mHorizontalHintText.setVisibility(View.VISIBLE);
            } else {
                // 消失动画
                AlphaAnimation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
                hideAnimation.setDuration(600);
                hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mHorizontalHintText.setVisibility(View.INVISIBLE);
                        mHorizontalHintText.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mHorizontalHintText.startAnimation(hideAnimation);
            }
        }
    }

    private static File getFromMediaUri(ContentResolver resolver, Uri uri) {
        if (uri == null) return null;

        if (SCHEME_FILE.equals(uri.getScheme())) {
            return new File(uri.getPath());
        } else if (SCHEME_CONTENT.equals(uri.getScheme())) {
            final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, filePathColumn, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int columnIndex = (uri.toString().startsWith("content://com.google.android.gallery3d")) ?
                            cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME) :
                            cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    // Picasa image on newer devices with Honeycomb and up
                    if (columnIndex != -1) {
                        String filePath = cursor.getString(columnIndex);
                        if (!TextUtils.isEmpty(filePath)) {
                            return new File(filePath);
                        }
                    }
                }
            } catch (SecurityException ignored) {
                // Nothing we can do
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (mCropImageView.isShown()) {
            // Crop mode
            switchToCameraMode();
        } else {
            // finish
            super.onBackPressed();
        }
    }
}
