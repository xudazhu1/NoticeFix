package com.xeasy.noticefix.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.adapter.AppLibAdapter;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.dao.AppUtil;
import com.xeasy.noticefix.dao.IconLibDao;
import com.xeasy.noticefix.databinding.ActivityIconLibBinding;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class IconLibActivity extends AppCompatActivity {

    private ActivityIconLibBinding binding;
    public View expandedView = null;
    /**
     * 被点击的index
     */
    public int pos = -1;
    private AppLibAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("size" + AppUtil.appInfo4ViewMap.size());
        binding = ActivityIconLibBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 菜单栏
        setSupportActionBar(binding.toolbar1);
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // 返回箭头
        binding.toolbar1.setNavigationOnClickListener(v -> finish());

        Map<String, IconLibBean> iconLibBeanMap = IconLibDao.getIconLib(this, true);
        RecyclerView appListView = findViewById(R.id.appListView);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        appListView.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        appListView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // //创建适配器
        adapter = new AppLibAdapter(this, iconLibBeanMap.values());
        //设置适配器
        appListView.setAdapter(adapter);

        TextView viewById = findViewById(R.id.lib_icon_app_count);
        viewById.setText(iconLibBeanMap.size() + "");
    }


    private String query = "";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_icon_lib, menu);

        //可以在这获取菜单Item和他的actionView，做一些设置，比如给searchView添加文本变化监听和提交搜索监听
        SearchView searchView = (SearchView) menu.findItem(R.id.app_filter_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                IconLibActivity.this.query = query;
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                IconLibActivity.this.query = query;
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

}