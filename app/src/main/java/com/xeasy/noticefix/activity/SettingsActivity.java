package com.xeasy.noticefix.activity;

import static android.view.animation.Animation.INFINITE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.dao.GlobalConfigDao;
import com.xeasy.noticefix.dao.IconLibDao;
import com.xeasy.noticefix.databinding.SettingsActivityBinding;
import com.xeasy.noticefix.utils.GetFilePathFromUri;
import com.xeasy.noticefix.utils.TaskUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private SettingsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 菜单栏
        setSupportActionBar(binding.toolbar);
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // 返回箭头
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 调试模式开关
        SwitchCompat debugModeSwitchCompat = findViewById(R.id.debug_mode);
        debugModeSwitchCompat.setChecked(GlobalConfigDao.globalConfigDao.debugMode);
        debugModeSwitchCompat.setOnClickListener((v) -> {
            boolean isChecked = debugModeSwitchCompat.isChecked();
            if ( isChecked ) {
                debugModeSwitchCompat.setChecked(false);
                reqPass();
            }

        });
        // 保存
        debugModeSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            GlobalConfigDao.globalConfigDao.debugMode = isChecked;
            GlobalConfigDao.saveConfig(SettingsActivity.this, GlobalConfigDao.globalConfigDao);
        });
        // 跳过灰度开关
        SwitchCompat skipGrayscaleIconSwitchCompat = findViewById(R.id.skip_grayscale_icon);
        skipGrayscaleIconSwitchCompat.setChecked(GlobalConfigDao.globalConfigDao.skipGrayscale);
        skipGrayscaleIconSwitchCompat.setOnCheckedChangeListener((btn, isChecked) -> {
            GlobalConfigDao.globalConfigDao.skipGrayscale = isChecked;
            GlobalConfigDao.saveConfig(this, GlobalConfigDao.globalConfigDao);
        });
        // 解除彩色开关
        SwitchCompat showColoredIconsSwitchCompat = findViewById(R.id.show_colored_icons);
        showColoredIconsSwitchCompat.setChecked(GlobalConfigDao.globalConfigDao.showColoredIcons);
        showColoredIconsSwitchCompat.setOnCheckedChangeListener((btn, isChecked) -> {
            GlobalConfigDao.globalConfigDao.showColoredIcons = isChecked;
            GlobalConfigDao.saveConfig(this, GlobalConfigDao.globalConfigDao);
        });
//        // 自定义图标帮助
//        SwitchCompat customIconHelperSwitchCompat = findViewById(R.id.custom_icon_helper);
//        customIconHelperSwitchCompat.setChecked(GlobalConfigDao.globalConfigDao.customIconHelper);
//        customIconHelperSwitchCompat.setOnCheckedChangeListener((btn, isChecked) -> {
//            GlobalConfigDao.globalConfigDao.customIconHelper = isChecked;
//            GlobalConfigDao.saveConfig(this, GlobalConfigDao.globalConfigDao);
//        });
        // 上传图标包
        ImageView updateIconLibrary = findViewById(R.id.update_icon_library);

        // 选取图标包的回调
        ActivityResultLauncher<Intent> intentActivityResultLauncher = SettingsActivity.this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    //此处是跳转的result回调方法
                    System.out.println(result);
                    Intent data = result.getData();
                    // 没选取照片时不做任何操作
                    if (data != null && data.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri

                        String fileAbsolutePath = GetFilePathFromUri.getFileAbsolutePath(SettingsActivity.this, selectedImage);
                        try {
                            IconLibDao.readAndInitIconLib(SettingsActivity.this, new FileInputStream(fileAbsolutePath));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

        updateIconLibrary.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");//json 类型
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intentActivityResultLauncher.launch(intent);
            }
        });

        ImageView refreshIconLibrary = findViewById(R.id.refresh_icon_library);
        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(refreshIconLibrary, "rotation", 1080f);
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(INFINITE);
        refreshIconLibrary.setOnClickListener(v -> {
            // 在线刷新图标库按钮
            if (GlobalConfigDao.globalConfigDao.debugMode) {
                objectAnimator.start();
                TaskUtils.createTask(()->IconLibDao.refreshIconLibOnLine(SettingsActivity.this, objectAnimator));
            } else {
                Toast.makeText(SettingsActivity.this, getString(R.string.function_not_open), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void reqPass() {
        final EditText inputServer = new EditText(this);

        inputServer.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        SwitchCompat viewById = findViewById(R.id.debug_mode);
        builder.setTitle("暗号?").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> viewById.setChecked(false));
        builder.setPositiveButton(getString(R.string.submit), (dialog, which) -> {
            String _sign = inputServer.getText().toString();
            if (_sign.equals("easy") || _sign.equals("星夜不荟") ) {
                    viewById.setChecked(true);
                } else {
                    Toast.makeText(SettingsActivity.this, "暗号错误", Toast.LENGTH_SHORT).show();
                    viewById.setChecked(false);
                }
        });
        builder.show();
    }

}