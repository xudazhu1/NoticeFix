package com.xeasy.noticefix.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.adapter.IconOrderAdapter;
import com.xeasy.noticefix.dao.IconFuncDao;
import com.xeasy.noticefix.databinding.ActivityMainBinding;
import com.xeasy.noticefix.utils.AppNotification;
import com.xeasy.noticefix.utils.CommandUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // 从工具类获取配置文件信息
        List<IconFuncDao.IconFuncStatus> iconFunc = IconFuncDao.getIconFunc(this);

        RecyclerView recyclerView = findViewById(R.id.main_recyclerView);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        //创建适配器
        IconOrderAdapter adapter = new IconOrderAdapter(iconFunc, recyclerView, this);
        //设置适配器
        recyclerView.setAdapter(adapter);

        // 自定义图标页面跳转
        View viewById = findViewById(R.id.custom_icon_config);
        viewById.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppListActivity.class);
            startActivity(intent);
        });
        // 图标库 页面跳转
        View viewIconLib = findViewById(R.id.view_icon_lib);
        viewIconLib.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IconLibActivity.class);
            startActivity(intent);
        });

        activeXposed(false);
    }

    @SuppressWarnings("SameParameterValue")
    private void activeXposed(boolean active) {
        TextView status = findViewById(R.id.xposed_status);
        if ( active ) {
            status.setText(getString(R.string.xposed_status, getString(R.string.yes)));
            status.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            status.setText(getString(R.string.xposed_status, getString(R.string.no)));
            status.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private static final String LOG_PREV = "NoticeFix---";

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

//            PackageManager packageManager = this.getPackageManager();
//            boolean b = packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI);
//            Log.d("Sel", "FEATURE_MIDI.. ==>  " + b);
//            MidiManager mMidiManager = (MidiManager) this.getApplicationContext().getSystemService(Context.MIDI_SERVICE);
//            MidiDeviceInfo[] devices = mMidiManager.getDevices();
//            for ( MidiDeviceInfo device : devices ) {
//                Log.d("Sel", "device is "+ device.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER)
//                        + " | PROPERTY_USB_DEVICE ? == " + device.getProperties().getString(MidiDeviceInfo.PROPERTY_USB_DEVICE)
//                        + " | PROPERTY_BLUETOOTH_DEVICE ? == " + device.getProperties().getString(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE));
//            }
//            Log.d("Sel", "end.. ");


            // 设置页面跳转
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.reset_icon) {

            // todo 测试命令
//            CommandUtil.execShellBackAll("chmod -R 777 /data_mirror/data_ce/null/0/com.xiaomi.smarthome/shared_prefs/1516982620_local_userconfig_pref.xml", true);
//            String pgrep_system = CommandUtil.execShellBackAll("cat /data_mirror/data_ce/null/0/com.xiaomi.smarthome/shared_prefs/1516982620_local_userconfig_pref.xml", true);
//            Toast.makeText(this, pgrep_system, Toast.LENGTH_SHORT).show();
            String s = CommandUtil.execShellBackAll("chmod 664 /data/data/com.xeasy.noticefix/shared_prefs/global_config_file.xml", false);
            Log.d(LOG_PREV, "设置权限664  ==》 " + s);
            String s2 = CommandUtil.execShellBackAll("chmod -R 755 /data/data/com.xeasy.noticefix/shared_prefs", false);
            Log.d(LOG_PREV, "设置权限 755  ==》 " + s2);

            AppNotification.sendFlashNoticeMessage(this, null);
        }
        if (id == R.id.restart_systemui) {
            new AlertDialog.Builder(this).setTitle("confirm")//设置对话框标题
                    .setMessage("Restart SystemUI ? ")
                    .setPositiveButton(this.getString(R.string.yes), (dialog, which) -> {//确定按钮的响应事件，点击事件没写，自己添加
                        CommandUtil.restartSystemUI(this);
                    }).setNegativeButton(this.getString(R.string.no), (dialog, which) -> {//响应事件，点击事件没写，自己添加
                    }).show();//在按键响应事件中显示此对话框

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}