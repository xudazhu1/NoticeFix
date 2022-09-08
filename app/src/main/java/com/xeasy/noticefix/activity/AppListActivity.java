package com.xeasy.noticefix.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.adapter.AppInfoAdapter;
import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.constant.MyConstant;
import com.xeasy.noticefix.dao.AppUtil;
import com.xeasy.noticefix.dao.CustomIconDao;
import com.xeasy.noticefix.databinding.AppListActivityBinding;
import com.xeasy.noticefix.utils.GetFilePathFromUri;
import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.TaskUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("FieldCanBeLocal")
public class AppListActivity extends AppCompatActivity {

    private AppListActivityBinding binding;
    public View expandedView = null;
    public AppInfoAdapter.ViewHolder viewHolder = null;
    /**
     * 被点击的index
     */
    public int pos = -1;
    private AppInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("size" + AppUtil.appInfo4ViewMap.size());
        binding = AppListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 菜单栏
        setSupportActionBar(binding.toolbar);
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // 返回箭头
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        List<PackageInfo> allAppInfo = AppUtil.getAllAppInfo(this);
        RecyclerView appListView = findViewById(R.id.appListView);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        appListView.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        appListView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // 选取图标的回调
        ActivityResultLauncher<Intent> intentActivityResultLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    //此处是跳转的result回调方法
                    System.out.println(result);
                    Intent data = result.getData();
                    // 没选取照片时不做任何操作
                    if (data != null && data.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri

                        String fileAbsolutePath = GetFilePathFromUri.getFileAbsolutePath(AppListActivity.this, selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeFile(fileAbsolutePath);
                        reqImage(bitmap);
                    }
                });
        // //创建适配器
        adapter = new AppInfoAdapter(this, allAppInfo, intentActivityResultLauncher);
        //设置适配器
        appListView.setAdapter(adapter);

        TaskUtils.createTask(this:: inflateAppCount);

    }

    private void reqImage(Bitmap bitmap) {

        //  Avoid passing null as the view root (needed to resolve layout parameters on the inflated layout's root element)
        View view = LayoutInflater.from(this).inflate(R.layout.image_pre, null, false);
        ImageView ImgPre = view.findViewById(R.id.icon_pre_img);
        ImgPre.setImageBitmap(bitmap);

        SwitchCompat iconPreGrayscale = view.findViewById(R.id.icon_pre_grayscale);
        SwitchCompat iconPreReverseColor = view.findViewById(R.id.icon_pre_reverse_color);
        SwitchCompat iconPreRoundedCorners = view.findViewById(R.id.icon_pre_rounded_corners);
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
            Map<String, Bitmap> tempMap = new HashMap<>();
            tempMap.put("temp", bitmap);
            if ( iconPreGrayscale.isChecked() ) {
                tempMap.put("temp", ImageTools.getSinglePic4Pre(Objects.requireNonNull(tempMap.get("temp"))));
            }
            if ( iconPreReverseColor.isChecked() ) {
                tempMap.put("temp", ImageTools.reverseColor(Objects.requireNonNull(tempMap.get("temp"))));
            }
            if ( iconPreRoundedCorners.isChecked() ) {
                Bitmap nonNull = Objects.requireNonNull(tempMap.get("temp"));
                tempMap.put("temp", ImageTools.toRoundCorner(nonNull, nonNull.getWidth() / 2));
            }
            Bitmap end = Objects.requireNonNull(tempMap.get("temp"));
            ImgPre.setImageBitmap(end);
        };

        iconPreGrayscale.setOnCheckedChangeListener(onCheckedChangeListener);
        iconPreReverseColor.setOnCheckedChangeListener(onCheckedChangeListener);
        iconPreRoundedCorners.setOnCheckedChangeListener(onCheckedChangeListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.custom_icon).setView(view)
                .setNegativeButton(getString(R.string.cancel), null);
        builder.setPositiveButton(getString(R.string.submit), (dialog, which) -> {
            if ( expandedView != null ) {
                TextView appPkg = expandedView.findViewById(R.id.app_info_pkg);
                String pkgName = appPkg.getText().toString();
                // 持久化
                Bitmap res = ImageTools.toBitmap(ImgPre.getDrawable());
                CustomIconDao.saveCustomIcon(this, pkgName, res);
//                            Objects.requireNonNull(AppUtil.appInfo4ViewMap.get(pkgName)).customIcon = res;

                ImageView appInfoIconCustom = expandedView.findViewById(R.id.app_info_icon_custom);
                appInfoIconCustom.setImageBitmap(res);
//                            // 重新渲染该行
//                            adapter.notifyItemChanged(pos);
                // 重新渲染app类型的统计数量
                inflateAppCount();
            }
        });
        builder.show();
    }

    /**
     * 渲染app类型的统计数量
     */
    @SuppressLint("SetTextI18n")
    public boolean inflateAppCount() {
        try {
            List<AppInfo4View> appInfo4Views = AppUtil.cacheTask.get();
            this.runOnUiThread(() -> {
                if ( appInfo4Views.size() > 0 ) {
                    TextView systemAppCountView = findViewById(R.id.system_app_count);
                    systemAppCountView.setText(AppUtil.appCount.get(MyConstant.AppType.SYSTEM.typeId) + "");
                    TextView userAppCount = findViewById(R.id.user_app_count);
                    userAppCount.setText(AppUtil.appCount.get(MyConstant.AppType.USER.typeId) + "");
                    TextView libIconAppCount = findViewById(R.id.lib_icon_app_count);
                    libIconAppCount.setText(AppUtil.appCount.get(MyConstant.AppType.LIB_ICON.typeId) + "");
                    TextView customIconAppCount = findViewById(R.id.custom_icon_app_count);
                    customIconAppCount.setText(AppUtil.appCount.get(MyConstant.AppType.CUSTOM_ICON.typeId) + "");
                }
            });
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String query = "";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_list, menu);

        //可以在这获取菜单Item和他的actionView，做一些设置，比如给searchView添加文本变化监听和提交搜索监听
        SearchView searchView = (SearchView) menu.findItem(R.id.app_filter_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AppListActivity.this.query = query;
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AppListActivity.this.query = query;
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    /**
     * 右上角勾选的条件 只存未勾选
     */
    public Set<MyConstant.AppType> isCheckList = new HashSet<MyConstant.AppType>(){{
        add(MyConstant.AppType.SYSTEM);
        add(MyConstant.AppType.USER);
        add(MyConstant.AppType.LIB_ICON);
        add(MyConstant.AppType.CUSTOM_ICON);
    }};

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // 手动指定checkbox状态 真睿智啊安卓
        item.setChecked(! item.isChecked());

        //noinspection SimplifiableIfStatement
        boolean checked = item.isChecked();
        if (id == R.id.app_filter_system) {
            if ( checked ) {
                isCheckList.add(MyConstant.AppType.SYSTEM);
            } else {
                isCheckList.remove(MyConstant.AppType.SYSTEM);
            }
        }
        if (id == R.id.app_filter_user) {
            if ( checked ) {
                isCheckList.add(MyConstant.AppType.USER);
            } else {
                isCheckList.remove(MyConstant.AppType.USER);
            }
        }
        if (id == R.id.app_filter_icon_matched) {
            if ( checked ) {
                isCheckList.add(MyConstant.AppType.LIB_ICON);
            } else {
                isCheckList.remove(MyConstant.AppType.LIB_ICON);
            }
        }
        if (id == R.id.app_filter_custom_icon) {
            if ( checked ) {
                isCheckList.add(MyConstant.AppType.CUSTOM_ICON);
            } else {
                isCheckList.remove(MyConstant.AppType.CUSTOM_ICON);
            }
        }
        adapter.getFilter().filter(query);



        return super.onOptionsItemSelected(item);
    }

}