package com.xeasy.noticefix.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.utils.ImageTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AppLibAdapter extends RecyclerView.Adapter<AppLibAdapter.ViewHolder> implements Filterable {

    private final List<IconLibBean> appInfoList;
    public List<IconLibBean> mFilterList;
    private final Context context;

    public AppLibAdapter(Context context, Collection<IconLibBean> appInfoList) {
        super();
        this.appInfoList = new ArrayList<>(appInfoList);
        this.mFilterList = this.appInfoList;
        this.context = context;
    }

    @NonNull
    @Override
    public AppLibAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_info, parent, false);
        return new AppLibAdapter.ViewHolder(view);
    }

    /**
     * androidx.recyclerview.widget.RecyclerView.Adapter#onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull AppLibAdapter.ViewHolder holder, int position) {
        View contentView = holder.that.itemView;
        IconLibBean temp = mFilterList.get(position);
//        AppInfo4View info = AppUtil.getApp4ViewByIconLibBean(context, temp);
        // app图标
        ImageView appInfoIcon = contentView.findViewById(R.id.app_info_icon);
        appInfoIcon.setBackgroundColor(context.getColor(android.R.color.background_dark));
        appInfoIcon.setImageBitmap(ImageTools.base64ToBitmap(temp.iconBitmap));
        TextView appInfoName = contentView.findViewById(R.id.app_info_name);
        appInfoName.setText(temp.appName);
        TextView appInfoPkg = contentView.findViewById(R.id.app_info_pkg);
        appInfoPkg.setText(temp.packageName);
        TextView appInfoVersion = contentView.findViewById(R.id.app_info_version);
        // Do not concatenate text displayed with setText. Use resource string with placeholders.
        appInfoVersion.setText("----");
        TextView appInfoIconConfig = contentView.findViewById(R.id.app_info_icon_config);
        appInfoIconConfig.setText("----");



        holder.bind();
    }

    @Override
    public int getItemCount() {
        return mFilterList == null ? 0 : mFilterList.size();
    }

    private final Filter filter = new Filter() {
        //执行过滤操作
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String charString = charSequence.toString();
            System.out.println(charString);
            long start = System.currentTimeMillis();
            if ( charString.isEmpty() ) {
                //没有过滤的内容，则使用源数据
                mFilterList = appInfoList;
            } else {
                String searchContent = charSequence.toString().trim().toLowerCase();
                mFilterList = appInfoList.stream().filter(temp -> {
//                    AppInfo4View info = AppUtil.getApp4ViewByIconLibBean(AppLibAdapter.this.context, temp);
                    // 同时判断勾选的条件
                    return (temp.appName.toLowerCase().contains(searchContent) || temp.packageName.toLowerCase().contains(searchContent));
                }).collect(Collectors.toList());

            }
            System.out.println("耗时 = " + (System.currentTimeMillis() - start));
            FilterResults filterResults = new FilterResults();
            filterResults.values = mFilterList;
            return filterResults;
        }
        //把过滤后的值返回出来
        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults
        filterResults) {
            mFilterList = (List<IconLibBean>) filterResults.values;
            notifyDataSetChanged();
        }
    };
    @Override
    public Filter getFilter() {
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private final ViewHolder that;
        // 需要给值的先存成变量?
        ViewHolder(@NonNull View convertView) {
            super(convertView);
            that = this;
        }

        public void bind() {
            // 为上半部分设置点击事件 点击展开/收起下半部
//            itemView.findViewById(R.id.app_info_show).setOnClickListener(this);

            // 长按保存事件
            ImageView appIcon = itemView.findViewById(R.id.app_info_icon);
            appIcon.setTag( "appIcon");
            appIcon.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            new AlertDialog.Builder(context).setTitle(R.string.save_img_title)//设置对话框标题
                    .setMessage(R.string.save_img)
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {//确定按钮的响应事件，点击事件没写，自己添加
                        TextView nameView = this.itemView.findViewById(R.id.app_info_name);
                        String appName = nameView.getText().toString();
                        ImageView imageView = (ImageView) v;
                        ImageTools.saveImage(context, ImageTools.toBitmap(imageView.getDrawable()) ,appName + "_" + imageView.getTag());
                    }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {//响应事件，点击事件没写，自己添加
                    }).show();//在按键响应事件中显示此对话框

            return true;
        }

    }

}
