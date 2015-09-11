package com.cpiz.android.playground.TakePicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cpiz.android.common.RingtonePlayer;
import com.cpiz.android.playground.R;
import com.cpiz.android.utils.MathUtils;
import com.cpiz.android.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by caijw on 2015/3/27.
 */
public class CameraSurfaceView extends SurfaceView implements SensorEventListener, SurfaceHolder.Callback {
    private static final String TAG = "CameraSurfaceView";

    public static final int BEST_PICTURE_WIDTH = 1920;
    public static final int BEST_PICTURE_HEIGHT = 1080;

    @SuppressWarnings("FieldCanBeLocal")
    private static float BEST_PICTURE_RATIO = 0.75f;

    public static final int MAX_PICTURE_SHORT_SIDE = 1440;

    @SuppressWarnings("FieldCanBeLocal")
    private static float MOTIONLESS_ACC_IN_THRESHOLD = 0.35f;

    @SuppressWarnings("FieldCanBeLocal")
    private static float MOTIONLESS_ACC_OUT_THRESHOLD = 0.7f;

    @SuppressWarnings("FieldCanBeLocal")
    private static int MOTIONLESS_KEEP_TIME = 400;              // 静止此段时间后，自动对焦
    private static final float HIGH_MARK = 6.5f;                // 用于判断手机持向
    private static final float LOW_MARK = 2.5f;                 // 用于判断手机持向

    private Camera mCamera;
    private SurfaceHolder mCameraSurfaceHolder;
    private int mCameraId;
    private boolean mIsTakingPicture;
    private Point mFocusPoint = new Point();
    private Camera.Size mFullPictureSize;
    private Camera.Size mPreviewPictureSize;
    private Rect mClipRect;

    private int mCurrentRotation = Surface.ROTATION_0;
    private OnRotationListener mOnRotationListener;

    // 静止触发自动对焦
    private SensorManager mSensorManager;
    private float mLastX;
    private float mLastY;
    private float mLastZ;
    private long mLastMotionlessTime;
    private boolean mCurrentFocused;

    private Paint mFocusPaint;
    private Paint mGuidesPaint;

    enum FocusState {
        FOCUS_READY, FOCUSING, FOCUS_COMPLETE, FOCUS_FAILED
    }

    private FocusState mFocusState = FocusState.FOCUS_READY;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Rect getClipRect() {
        return mClipRect;
    }

    /**
     * 设置CameraSurfaceView实际相机预览区域
     *
     * @param clipRect 实际预览区域
     */
    public void setClipRect(Rect clipRect) {
        mClipRect = clipRect;
    }

    @SuppressWarnings("deprecation")
    private void init() {
        float scale = getResources().getDisplayMetrics().density;

        mGuidesPaint = new Paint();
        mGuidesPaint.setStyle(Paint.Style.STROKE);
        mGuidesPaint.setColor(Color.parseColor("#BBFFFFFF"));
        mGuidesPaint.setStrokeWidth(1);

        mFocusPaint = new Paint();
        mFocusPaint.setStyle(Paint.Style.STROKE);
        mFocusPaint.setStrokeWidth(scale * 2f);

        mCameraSurfaceHolder = getHolder();
        mCameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated
        mCameraSurfaceHolder.addCallback(this);

        // 选取加速度感应器，用于自动对焦
        if (!isInEditMode()) {
            mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        }

        // 获得后置摄像头ID
        mCameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i;
            }
        }
    }

    public void setCurrentRotation(int currentRotation) {
        if (mCurrentRotation != currentRotation) {
            Log.v(TAG, "rotate to " + currentRotation * 90);
            int oldRotation = mCurrentRotation;
            mCurrentRotation = currentRotation;
            if (mOnRotationListener != null) {
                mOnRotationListener.onRotate(currentRotation, oldRotation);
            }
        }
    }

    public int getCurrentRotation() {
        return mCurrentRotation;
    }

    public enum FlashMode {
        FLASH_ON, FLASH_OFF, FLASH_AUTO
    }

    private FlashMode mFlashMode = FlashMode.FLASH_OFF;

    public void setFlashMode(FlashMode mode) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            switch (mode) {
                case FLASH_ON:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);    // 常亮
                    break;
                case FLASH_OFF:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
                case FLASH_AUTO:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                default:
                    break;
            }

            Log.i(TAG, "set flash mode to " + parameters.getFlashMode());
            mFlashMode = mode;
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 拍照
     *
     * @param callback 拍照结果
     */
    public void takePicture(final TakePictureCallback callback) {
        if (mCamera == null) {
            if (callback != null) {
                callback.onError(new Exception("Connect camera failed"));
            }
        } else {
            mIsTakingPicture = true;

            changeFocusState(FocusState.FOCUS_READY);   // 隐藏对焦框
            invalidate();

            Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    // 不处理，但会产生快门音
                }
            };

            Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    camera.stopPreview();  // 冻结画面

                    Bitmap output = processPictureData(bytes);

                    if (callback != null) {
                        callback.onSuccess(output);
                    }
                }
            };

            Log.v(TAG, "take picture step 0: call takePicture");
            mCamera.takePicture(shutterCallback, null, jpegCallback);
        }
    }

    /**
     * 照片处理
     *
     * @param bytes
     */
    private Bitmap processPictureData(byte[] bytes) {
        // 获得图片原始尺寸，降低采样，提升性能，防止OOM
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;  // 照片数据，使用RGB_565足够，节约内存
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        Log.v(TAG, String.format("take picture step 1: picture original width = %d, height = %d", options.outWidth, options.outHeight));
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, MAX_PICTURE_SHORT_SIDE, MAX_PICTURE_SHORT_SIDE);

        // 解码图片
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap source = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        Log.v(TAG, String.format("take picture step 2: picture output width = %d, height = %d", options.outWidth, options.outHeight));

        // 根据 ClipRect 裁剪输出
        double outAspectRadio = (double) (mClipRect.width()) / mClipRect.height();
        int shortSideLen = Math.min(options.outWidth, options.outHeight);

        int outX = mClipRect.left * (source.getWidth() / options.inSampleSize) / getWidth();
        int outY = mClipRect.top * (source.getHeight() / options.inSampleSize) / getHeight();
        int outWidth = shortSideLen == source.getWidth() ? shortSideLen : (int) (shortSideLen * outAspectRadio);
        int outHeight = shortSideLen == source.getHeight() ? shortSideLen : (int) (shortSideLen * outAspectRadio);

        // 输出的时候不旋转
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90 + mCurrentRotation * 90);
//        Bitmap output = Bitmap.createBitmap(
//                source,
//                0,
//                0,
//                outWidth,
//                outHeight,
//                new Matrix(),
//                true);

        // 输出的时候不旋转，交给外面的客户保存时再选择
        Bitmap output = Bitmap.createBitmap(source, outX, outY, outWidth, outHeight);
        source.recycle();

        Log.v(TAG, "take picture step 3: on bitmap cropped");
        return output;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surfaceCreated");

        if (mCameraId >= 0) {
            try {
                mCamera = Camera.open(mCameraId);
                mCamera.setPreviewDisplay(mCameraSurfaceHolder);
            } catch (IOException e) {
                Log.e(TAG, "IOException on setPreviewDisplay", e);
                mCamera = null;
            } catch (Exception e) {
                Log.e(TAG, "Exception on open camera", e);
                ToastUtils.show(getContext(), "Open camera failed");
                mCamera = null;
            }
        } else {
            ToastUtils.show(getContext(), "Back camera not found");
            mCamera = null;
        }

        if (mCamera != null) {
            setWillNotDraw(false); // 调用onDraw
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, String.format("surfaceChanged: format=%d, width=%d, height=%d", format, width, height));
        if (mClipRect == null) {
            mClipRect = new Rect(0, 0, width, height);
        }
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed");

        stopCamera();
    }

    /**
     * 点击画面触发自动对焦
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCamera == null) {
            return true;
        }

        if (event.getPointerCount() != 1) {
            Log.d(TAG, "multi touch");
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            Log.d(TAG, "ACTION -> " + event.getAction());
            return true;
        }

        try2Focus((int) event.getX(), (int) event.getY(), true);

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float dx = x - mLastX;
        float dy = y - mLastY;
        float dz = z - mLastZ;
        mLastX = x;
        mLastY = y;
        mLastZ = z;
        float delta = FloatMath.sqrt(dx * dx + dy * dy + dz * dz);

        // 持握朝向判断
        int newRotation = mCurrentRotation;
        if (Math.abs(y) > HIGH_MARK && Math.abs(x) < LOW_MARK) {
            newRotation = y > 0 ? Surface.ROTATION_0 : Surface.ROTATION_180;
        } else if (Math.abs(x) > HIGH_MARK && Math.abs(y) < LOW_MARK) {
            // Landscape
            newRotation = x > 0 ? Surface.ROTATION_270 : Surface.ROTATION_90;
        }

        setCurrentRotation(newRotation);

        // 自动对焦处理
        long nowTime = System.currentTimeMillis();
        if (mFocusState == FocusState.FOCUS_READY && !mCurrentFocused && delta > MOTIONLESS_ACC_IN_THRESHOLD) {
            // 检测到摇晃，须要重新对焦
            mCurrentFocused = false;
            mLastMotionlessTime = nowTime;
        } else if (mFocusState == FocusState.FOCUS_READY && mCurrentFocused && delta > MOTIONLESS_ACC_OUT_THRESHOLD) {
            // 处理同上，触发自动对焦后，为方便用户手动重新对焦，提高触发阀值
            Log.v(TAG, String.format("big shake detected = %f, reset focus!", delta));
            mCurrentFocused = false;
            mLastMotionlessTime = nowTime;
        } else if (!mIsTakingPicture && !mCurrentFocused && mLastMotionlessTime != 0 && nowTime - mLastMotionlessTime > MOTIONLESS_KEEP_TIME) {
            // 静止时间超过设定，自动触发中央对焦
            Log.v(TAG, String.format("delta = %f, auto refocus!", delta));
            try2Focus(mClipRect.centerX(), mClipRect.centerY(), false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startCameraPreview() {
        if (mCamera != null) {
            // 注册重力感应，静止时自动对焦
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);

            mIsTakingPicture = false;

            setCameraDisplayOrientation();
            setBestPictureSize();
            setBestPreviewSize();
            setFlashMode(mFlashMode);   // 回到相机界面时恢复之前闪关灯状态

            mCamera.startPreview();
        }
    }

    private void stopCamera() {
        if (mCamera != null) {
            mSensorManager.unregisterListener(this);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = getResources().getDisplayMetrics().density;

        // 基准线
        if (mClipRect != null) {
            for (int i = 1; i <= 2; i++) {
                canvas.drawLine(mClipRect.left, mClipRect.top + i * mClipRect.height() / 3, mClipRect.right, mClipRect.top + i * mClipRect.height() / 3, mGuidesPaint);
            }

            for (int i = 1; i <= 2; i++) {
                canvas.drawLine(mClipRect.left + i * mClipRect.width() / 3, mClipRect.top, mClipRect.left + i * mClipRect.width() / 3, mClipRect.bottom, mGuidesPaint);
            }
        }

        // 对焦框
        if (mCamera != null && mFocusState != FocusState.FOCUS_READY) {
            int size = (int) (scale * 40f);
            if (mFocusState == FocusState.FOCUSING) {
                mFocusPaint.setColor(Color.WHITE);
            } else if (mFocusState == FocusState.FOCUS_COMPLETE) {
                mFocusPaint.setColor(Color.GREEN);
            } else if (mFocusState == FocusState.FOCUS_FAILED) {
                mFocusPaint.setColor(Color.RED);
            }
            canvas.drawRect(mFocusPoint.x - size, mFocusPoint.y - size, mFocusPoint.x + size, mFocusPoint.y + size, mFocusPaint);
        }

        super.onDraw(canvas);
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        mCamera.setDisplayOrientation(90); // for portrait
    }

    /**
     * 设置拍照尺寸，尽可能取画面比例符合BEST_PICTURE_RATIO的硬件最大值
     */
    private void setBestPictureSize() {
        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size pictureSize = null;
        float pictureRatio = 0f;

        // 查找设定分辨率
        for (Camera.Size size : sizes) {
            Log.d(TAG, "picture sizes> w = " + size.width + ", h = " + size.height + ", ratio = " + (float) size.height / size.width);
            if (size.width == BEST_PICTURE_WIDTH && size.height == BEST_PICTURE_HEIGHT) {
                pictureSize = size;
                pictureRatio = (float) size.height / size.width;
            }
        }

        // 最佳分辨率
        if (pictureSize == null) {
            for (Camera.Size size : sizes) {
                float sizeRatio = (float) size.height / size.width;
                if (pictureSize == null
                        || (MathUtils.isApproximatelyEqual(sizeRatio, BEST_PICTURE_RATIO)
                        && (!MathUtils.isApproximatelyEqual(pictureRatio, BEST_PICTURE_RATIO) || size.width * size.height > pictureSize.width * pictureSize.height))) {
                    pictureSize = size;
                    pictureRatio = sizeRatio;
                }
            }
        }

        mFullPictureSize = pictureSize;
        if (mFullPictureSize != null) {
            Log.i(TAG, "set best picture size> width = " + pictureSize.width + ", " +
                    "height = " + pictureSize.height + ", ratio = " + pictureRatio);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            mCamera.setParameters(params);
        }
    }

    /**
     * 设置预览尺寸，在保证与照片比例一致(约等于，误差0.01)的前提下，尽可能取最大
     */
    private void setBestPreviewSize() {
        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        if (previewSizes.isEmpty()) {
            Log.d(TAG, "empty preview sizes");
            return;
        }

        float pictureRatio = (float) mFullPictureSize.height / mFullPictureSize.width;
        float previewRatio = 0f;
        float sizeRatio;
        for (Camera.Size size : previewSizes) {
            sizeRatio = (float) size.height / size.width;
            Log.d(TAG, "preview sizes> w = " + size.width + ", h = " + size.height + ", ratio = " + sizeRatio);

            if (mPreviewPictureSize == null
                    || (MathUtils.isApproximatelyEqual(sizeRatio, pictureRatio)
                    && (!MathUtils.isApproximatelyEqual(previewRatio, pictureRatio) || size.width * size.height > mPreviewPictureSize.width * mPreviewPictureSize.height))) {
                mPreviewPictureSize = size;
                previewRatio = sizeRatio;
            }
        }

        Log.i(TAG, "set best preview size> width = " + mPreviewPictureSize.width + ", " +
                "height = " + mPreviewPictureSize.height + ", ratio = " + previewRatio);
        params.setPreviewSize(mPreviewPictureSize.width, mPreviewPictureSize.height);
        mCamera.setDisplayOrientation(0);
        mCamera.setParameters(params);

        adjustViewSize(mPreviewPictureSize);
    }

    // Adjust SurfaceView size
    private void adjustViewSize(Camera.Size size) {
//        int picWidth = Math.max(size.width, size.height);
//        int picHeight = Math.min(size.width, size.height);
//        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
//        layoutParams.height = getHeight();
//        layoutParams.width = getHeight() * picWidth / picHeight;
//        this.setLayoutParams(layoutParams);
    }

    private ArrayList<Camera.Area> getCameraAreasFromPreview(float x, float y) {
        Matrix camera2prevMatrix = new Matrix();
        Log.d(TAG, "camera2prevMatrix reset" + camera2prevMatrix.toString());
        camera2prevMatrix.postRotate(0);
        Log.d(TAG, "camera2prevMatrix postRotate(0)" + camera2prevMatrix.toString());
        camera2prevMatrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
        Log.d(TAG, String.format("camera2prevMatrix postScale(%f, %f) = %s", getWidth() / 2000f, getHeight() / 2000f, camera2prevMatrix.toString()));
        camera2prevMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
        Log.d(TAG, String.format("camera2prevMatrix postTranslate(%f, %f) = %s", getWidth() / 2f, getHeight() / 2f, camera2prevMatrix.toString()));

        Matrix preview2cameraMatrix = new Matrix();
        if (!camera2prevMatrix.invert(preview2cameraMatrix)) {
            Log.d(TAG, "failed to invert matrix !");
        }
        Log.d(TAG, "preview2cameraMatrix " + preview2cameraMatrix.toString());

        float[] coords = {x, y};
        Log.d(TAG, "x => " + coords[0] + ", y => " + coords[1]);
        preview2cameraMatrix.mapPoints(coords);
        Log.d(TAG, "cx => " + coords[0] + ", cy => " + coords[1]);

        Rect rect = new Rect();
        rect.left = (int) coords[0] - 50;
        rect.right = (int) coords[0] + 50;
        rect.top = (int) coords[1] - +50;
        rect.bottom = (int) coords[1] + 50;

        final ArrayList<Camera.Area> areas = new ArrayList<>(1);
        areas.add(new Camera.Area(rect, 1000));

        return areas;
    }

    private void try2Focus(int x, int y, final boolean byTouch) {
        mCurrentFocused = true;
        if (mCamera != null) {
            Log.v(TAG, String.format("try to focus: x = %d, y = %d", x, y));

            Camera.Parameters parameters = mCamera.getParameters();
            String focusMode = parameters.getFocusMode();
            Log.d(TAG, "FocusMode -> " + focusMode);

            mFocusPoint.set(x, y);
            changeFocusState(FocusState.FOCUSING);

            final ArrayList<Camera.Area> focusAreas = getCameraAreasFromPreview(x, y);
            parameters.setFocusAreas(focusAreas);
            if (parameters.getMaxNumMeteringAreas() != 0) { // also set metering areas
                parameters.setMeteringAreas(focusAreas);
            }

            try {
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG, "autofocus complete: success -> " + success);
                        changeFocusState(success ? FocusState.FOCUS_COMPLETE : FocusState.FOCUS_FAILED);
                        if (success && byTouch) {
                            RingtonePlayer.Instance.playRingTone(R.raw.camera_focus, false);
                        }
                    }
                });
            } catch (Exception e) {
                Log.w(TAG, "autofocus failed", e);
            }
        }
    }


    /**
     * 对焦成功的提示处理
     */
    Timer mFocusCompletedTimer = null;

    private void changeFocusState(FocusState state) {
        if (mFocusCompletedTimer != null) {
            mFocusCompletedTimer.cancel();
            mFocusCompletedTimer.purge();
            mFocusCompletedTimer = null;
        }

        mFocusState = state;
        invalidate();

        if (state == FocusState.FOCUS_COMPLETE || state == FocusState.FOCUS_FAILED) {
            mFocusCompletedTimer = new Timer();
            mFocusCompletedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 对焦成功一定时间后，隐藏对焦框
                    mFocusState = FocusState.FOCUS_READY;
                    postInvalidate();
                    Log.v(TAG, "Focus ready!");
                }
            }, 800);
        }
    }

    public interface TakePictureCallback {
        void onSuccess(Bitmap bitmap);

        void onError(Exception e);
    }

    public interface OnRotationListener {
        void onRotate(int newRotation, int oldRotation);
    }

    public void setOnRotationListener(OnRotationListener onRotationListener) {
        mOnRotationListener = onRotationListener;
    }

    public static int calculateInSampleSize(final int rawWidth, final int rawHeight, final int reqWidth, final int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            long totalPixels = rawWidth / inSampleSize * rawHeight / inSampleSize;

            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }

        return inSampleSize;
    }
}

