package com.cpiz.android.playground.ExButtonTest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cpiz.android.playground.R;

import butterknife.ButterKnife;

public class TintableImageButtonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tintable_image_button);
        ButterKnife.bind(this);
    }
}
