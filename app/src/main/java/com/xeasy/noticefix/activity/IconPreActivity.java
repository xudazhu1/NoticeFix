package com.xeasy.noticefix.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.xeasy.noticefix.databinding.IconPreActivityBinding;

/**
 * 选取图标后 简单处理并预览的activity
 */
public class IconPreActivity extends AppCompatActivity {

    private IconPreActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = IconPreActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        // 返回箭头
//        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

}