package com.cpiz.android.playground.TakePicture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.cpiz.android.playground.R;
import com.cpiz.android.playground.TakePicture.cropper.CropImageView;
import com.cpiz.android.utils.FileUtils;
import com.cpiz.android.utils.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 定制过的相机界面，也提供从相册选择的路径
 * <p>
 * 使用 startActivityForResult 启动，可在 Intent 中 setData 指定图片存储路径，若不指定则会自动创建路径
 * 在调用的 Activity 中通过 onActivityResult 的 Intent.getData 获得输出图片路径
 * <p>
 * Created by caijw on 2015/3/26.
 */
public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";

    private View mPreviewClipLayout;
    private CameraSurfaceView mCameraSurfaceView;
    private CropImageView mCropImageView;
    private CheckBox mFlashToggleBtn;
    private ImageButton mTakePhotoBtn;
    private ImageButton mGalleryBtn;
    private ImageButton mCancelCropBtn;
    private ImageButton mConfirmCropBtn;

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
        findViewById(R.id.title_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPreviewClipLayout = findViewById(R.id.preview_clip_layout);
        mPreviewClipLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCameraSurfaceView.setClipRect(new Rect(left, top, right, bottom));
            }
        });

        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        mCropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        mCropImageView.setGuidelines(2);    // no guide lines
        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setAspectRatio(4, 3);

        mFlashToggleBtn = (CheckBox) findViewById(R.id.flashToggleCheckBox);
        mFlashToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mCameraSurfaceView.setFlashMode(CameraSurfaceView.FlashMode.FLASH_ON);
                } else {
                    mCameraSurfaceView.setFlashMode(CameraSurfaceView.FlashMode.FLASH_OFF);
                }
            }
        });

        mTakePhotoBtn = (ImageButton) findViewById(R.id.takePicBtn);
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

        mGalleryBtn = (ImageButton) findViewById(R.id.galleryBtn);
        mGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CameraActivity.this.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            }
        });

        mCancelCropBtn = (ImageButton) findViewById(R.id.cancel_crop_btn);
        mCancelCropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraModel.Instance.clearCacheBitmap();
                switchToCameraMode();
                setCameraButtonsEnabled(true);
            }
        });

        mConfirmCropBtn = (ImageButton) findViewById(R.id.confirm_crop_btn);
        mConfirmCropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveBitmap2File(mCropImageView.getCroppedImage(), mPictureFile)) {
                    ToastUtils.show(CameraActivity.this, String.format("Picture saved to %s", mPictureFile.getAbsoluteFile()));
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
        mFlashToggleBtn.setEnabled(enabled);
        mTakePhotoBtn.setEnabled(enabled);
        mGalleryBtn.setEnabled(enabled);
    }

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
            Log.e(TAG, "save picture failed", e);
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
        mCropImageView.setCropEnabled(!confirmOnly);
    }

    private void toggleButtons(boolean camera) {
        mCropImageView.setVisibility(camera ? View.INVISIBLE : View.VISIBLE);
        mCameraSurfaceView.setVisibility(camera ? View.VISIBLE : View.INVISIBLE);
        mTakePhotoBtn.setVisibility(camera ? View.VISIBLE : View.INVISIBLE);
        mFlashToggleBtn.setVisibility(camera ? View.VISIBLE : View.GONE);
        mGalleryBtn.setVisibility(camera ? View.VISIBLE : View.GONE);
        mCancelCropBtn.setVisibility(camera ? View.GONE : View.VISIBLE);
        mConfirmCropBtn.setVisibility(camera ? View.GONE : View.VISIBLE);
    }

    public static File getFromMediaUri(ContentResolver resolver, Uri uri) {
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
}
