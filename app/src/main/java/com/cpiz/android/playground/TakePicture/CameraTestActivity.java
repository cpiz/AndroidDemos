package com.cpiz.android.playground.TakePicture;

import android.content.Intent;

import com.cpiz.android.playground.BaseListActivity;
import com.cpiz.android.playground.PlayHelper;
import com.cpiz.android.playground.TestAction;
import com.cpiz.android.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caijw on 2015/9/15.
 */
public class CameraTestActivity extends BaseListActivity {
    @Override
    public List<TestAction> getActions() {
        List<TestAction> actions = new ArrayList<>(5);

        actions.add(new TestAction("Portrait 1:1 Back Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(true)
                .setUseFrontCamera(false)
                .setRatio(1, 1)
                .setPreferredSize(1080, 1080)
                .start()));

        actions.add(new TestAction("Portrait 1:1 Front Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(true)
                .setUseFrontCamera(true)
                .setRatio(1, 1)
                .setPreferredSize(1080, 1080)
                .start()));

        actions.add(new TestAction("Landscape 1:1 Back Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(false)
                .setUseFrontCamera(false)
                .setRatio(1, 1)
                .setPreferredSize(1080, 1080)
                .start()));

        actions.add(new TestAction("Landscape 1:1 Front Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(false)
                .setUseFrontCamera(true)
                .setRatio(1, 1)
                .setPreferredSize(1080, 1080)
                .start()));

        actions.add(new TestAction("Landscape 4:3 Back Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(false)
                .setRatio(4, 3)
                .setPreferredSize(1920, 1440)
                .start()));

        actions.add(new TestAction("Landscape 4:3 Front Camera", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(false)
                .setUseFrontCamera(true)
                .setRatio(4, 3)
                .setPreferredSize(1920, 1440)
                .start()));

        actions.add(new TestAction("Choose from gallery", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(true)
                .setRatio(4, 3)
                .setSourceGallery()
                .start()));

        actions.add(new TestAction("Crop latest picture", () -> PhotoHelper.create(CameraTestActivity.this)
                .setPortrait(true)
                .setRatio(4, 3)
                .setSourcePath(PlayHelper.getLatestPicture(CameraTestActivity.this))
                .start()));

        return actions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PhotoHelper.REQUEST_TAKE_PICTURE) {
            if (resultCode != RESULT_OK) {
                ToastUtils.show(this, "Canceled");
                return;
            }

            String outputFile = data.getStringExtra(PhotoHelper.OUTPUT_PATH);
            int[] size = data.getIntArrayExtra(PhotoHelper.OUTPUT_SIZE);
            ToastUtils.show(this, String.format("Output picture path=[%s] size=%dx%d", outputFile, size[0], size[1]));

            PhotoHelper.create(CameraTestActivity.this)
                    .setPortrait(true)
                    .setRatio(size[0], size[1])
                    .setSourcePath(outputFile)
                    .start();
        }
    }
}
