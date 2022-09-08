package com.xeasy.noticefix.activity;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // 设置页面跳转
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.restart_systemui) {
            CommandUtil.restartSystemUI(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}