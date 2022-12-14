package com.xeasy.noticefix.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.activity.AppListActivity;
import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.dao.AppUtil;
import com.xeasy.noticefix.dao.CustomIconDao;
import com.xeasy.noticefix.utils.ExpandableViewHoldersUtil;
import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.PermissionsUtil;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.ViewHolder> implements Filterable {

    private final List<PackageInfo> appInfoList;
    public List<PackageInfo> mFilterList;
    private final Context context;
    private final ActivityResultLauncher<Intent> intentActivityResultLauncher;

    public AppInfoAdapter(Context context, List<PackageInfo> appInfoList, ActivityResultLauncher<Intent> intentActivityResultLauncher) {
        super();
        this.appInfoList = appInfoList;
        this.mFilterList = appInfoList;
        this.context = context;
        this.intentActivityResultLauncher = intentActivityResultLauncher;
    }

    @NonNull
    @Override
    public AppInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_info, parent, false);
        return new AppInfoAdapter.ViewHolder(view);
    }

    /**
     * androidx.recyclerview.widget.RecyclerView.Adapter#onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull AppInfoAdapter.ViewHolder holder, int position) {
        View contentView = holder.that.itemView;
        PackageInfo temp = mFilterList.get(position);
        inflateView(contentView, temp);
        holder.bind(position, temp);
    }

    public void inflateView(View contentView , PackageInfo temp) {
//
        AppInfo4View info = AppUtil.getApp4ViewByPackageInfo(context, temp);
        // app??????
        ImageView appInfoIcon = contentView.findViewById(R.id.app_info_icon);
        appInfoIcon.setImageDrawable(info.AppIcon);
        TextView appInfoName = contentView.findViewById(R.id.app_info_name);
        appInfoName.setText(info.AppName);
        if (info.isSystem) {
            appInfoName.setTextColor(context.getColor(android.R.color.holo_red_dark));
        } else {
            appInfoName.setTextColor(context.getColor(android.R.color.holo_green_dark));
        }
        TextView appInfoPkg = contentView.findViewById(R.id.app_info_pkg);
        appInfoPkg.setText(info.AppPkg);
        TextView appInfoVersion = contentView.findViewById(R.id.app_info_version);
        // Do not concatenate text displayed with setText. Use resource string with placeholders.
        appInfoVersion.setText(info.versionAndType);
        TextView appInfoIconConfig = contentView.findViewById(R.id.app_info_icon_config);
        String hasLibIcon = "??";
        String hasCustomIcon = "??";
        String notHandle = "??";

        // ????????????????????????
        ImageView libIcon = contentView.findViewById(R.id.app_info_icon_lib);
        if (info.libIcon != null ) {
            hasLibIcon = "???";
            appInfoIconConfig.setTextColor(context.getColor(android.R.color.holo_blue_dark));
            libIcon.setImageBitmap(info.libIcon);
        }else {
            libIcon.setImageResource(R.mipmap.none);
        }
        ImageView customIcon = contentView.findViewById(R.id.app_info_icon_custom);
        if ( info.customIcon != null ) {
            hasCustomIcon = "???";
            appInfoIconConfig.setTextColor(context.getColor(android.R.color.holo_orange_dark));
            // ????????????
            customIcon.setImageBitmap(info.customIcon);
        } else {
            customIcon.setImageResource(android.R.drawable.ic_menu_add);
        }
        if ( info.libIcon == null && info.customIcon == null ) {
            ColorStateList textColors = appInfoVersion.getTextColors();
            appInfoIconConfig.setTextColor(textColors.getDefaultColor());
        }
        if ( info.notHandle ) {
            notHandle = "???";
            appInfoIconConfig.setTextColor(context.getColor(android.R.color.holo_green_dark));
        }

        appInfoIconConfig.setText(context.getString(R.string.app_info_icon_config,
                hasLibIcon, hasCustomIcon, notHandle));

        SwitchCompat notHandleSwitchCompat = contentView.findViewById(R.id.not_handle);
        notHandleSwitchCompat.setChecked(info.notHandle);

        // todo ??????????????????
        ImageView lastIcon = contentView.findViewById(R.id.app_info_last_icon);
    }

    public ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> keepOne = new ExpandableViewHoldersUtil.KeepOneHolder<>();

    @Override
    public int getItemCount() {
        return mFilterList == null ? 0 : mFilterList.size();
    }

    private final Filter filter = new Filter() {
        //??????????????????
        @Override
        protected Filter.FilterResults performFiltering(CharSequence charSequence) {
            String charString = charSequence.toString();
            System.out.println(charString);
            AppListActivity context = (AppListActivity) AppInfoAdapter.this.context;
            long start = System.currentTimeMillis();
            if ( charString.isEmpty() && context.isCheckList.size() == 4 ) {
                //??????????????????????????????????????????
                mFilterList = appInfoList;
            } else {
                String searchContent = charSequence.toString().trim().toLowerCase();
                mFilterList = appInfoList.stream().filter(temp -> {
                    AppInfo4View info = AppUtil.getApp4ViewByPackageInfo(AppInfoAdapter.this.context, temp);
                    // ???????????????????????????
                    return AppUtil.appInFilter(info, context.isCheckList) &&
                            (info.AppName.toLowerCase().contains(searchContent) || temp.packageName.toLowerCase().contains(searchContent));
                }).collect(Collectors.toList());

            }
            System.out.println("?????? = " + (System.currentTimeMillis() - start));
            Filter.FilterResults filterResults = new Filter.FilterResults();
            filterResults.values = mFilterList;
            return filterResults;
        }
        //??????????????????????????????
        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, Filter.FilterResults
        filterResults) {
            mFilterList = (List<PackageInfo>) filterResults.values;
            notifyDataSetChanged();
        }
    };
    @Override
    public Filter getFilter() {
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, ExpandableViewHoldersUtil.Expandable {

        private final ViewHolder that;
        private int pos;
        // ???????????????????????????????
        ViewHolder(@NonNull View convertView) {
            super(convertView);
            that = this;
        }

        public void bind(int pos, PackageInfo bean) {
            this.pos = pos;
            keepOne.bind(this, pos);
            // ????????????????????????????????? ????????????/???????????????
            itemView.findViewById(R.id.app_info_show).setOnClickListener(this);
            // ????????????????????????????????????????????? ????????????????????????????????????????????????
            ImageView viewById = itemView.findViewById(R.id.app_info_icon_custom);
            viewById.setOnClickListener(v -> {

                Callable<Objects> callable = () -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/png");//png??????
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intentActivityResultLauncher.launch(intent);
                    return null;
                };
                if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2 ) {
                    PermissionsUtil.reqPermission((Activity) context, Manifest.permission.READ_MEDIA_IMAGES, callable);
                } else {
                    PermissionsUtil.reqPermission((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE, callable);
                }
            });
            viewById.setOnLongClickListener(v -> {
                //??????????????????
                //??????????????????
                new AlertDialog.Builder(context).setTitle(R.string.delete_custom_icon_title)//?????????????????????
                        .setMessage(R.string.delete_custom_icon_message)
                        .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {//???????????????????????????????????????????????????????????????
                            viewById.setImageResource(android.R.drawable.ic_menu_add);
                            // ?????????
                            CustomIconDao.delete(context, bean.packageName);
                            // ??????????????????
                            inflateView(itemView, bean);
                            // ??????????????????
                            ((AppListActivity)context).inflateAppCount();
                        }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {//????????????????????????????????????????????????
                        }).show();//??????????????????????????????????????????
                return true;
            });

            // ??????????????????
            ImageView iconLib = itemView.findViewById(R.id.app_info_icon_lib);
            iconLib.setTag( "iconLib");
            ImageView appIcon = itemView.findViewById(R.id.app_info_icon);
            appIcon.setTag( "appIcon");
            ImageView autoIcon = itemView.findViewById(R.id.app_info_last_icon);
            autoIcon.setTag( "autoIcon");
            iconLib.setOnLongClickListener(this);
            appIcon.setOnLongClickListener(this);
            autoIcon.setOnLongClickListener(this);

            // ????????????app??????
            SwitchCompat noHandle = itemView.findViewById(R.id.not_handle);
            noHandle.setOnClickListener(v -> {
                CustomIconBean customIcons = CustomIconDao.getCustomIcons(context, bean.packageName);
                AppInfo4View app4ViewByPackageName = AppUtil.getApp4ViewByPackageName(context, bean.packageName);
                assert app4ViewByPackageName != null;
                app4ViewByPackageName.notHandle = noHandle.isChecked();
                if ( customIcons == null ) {
                    customIcons = new CustomIconBean();
                    customIcons.pkgName = bean.packageName;
                    customIcons.label = app4ViewByPackageName.AppName;
                }
                customIcons.noHandle = noHandle.isChecked();
                CustomIconDao.save(context, customIcons);
                //?????????itemview
                inflateView(itemView, bean);
                // ????????????
                ((AppListActivity)context).inflateAppCount();
            });

        }

        @Override
        public boolean onLongClick(View v) {
            new AlertDialog.Builder(context).setTitle(R.string.save_img_title)//?????????????????????
                    .setMessage(R.string.save_img)
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {//???????????????????????????????????????????????????????????????
                        TextView nameView = this.itemView.findViewById(R.id.app_info_name);
                        String appName = nameView.getText().toString();
                        ImageView imageView = (ImageView) v;
                        ImageTools.saveImage(context, ImageTools.toBitmap(imageView.getDrawable()) ,appName + "_" + imageView.getTag());
                    }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {//????????????????????????????????????????????????
                    }).show();//??????????????????????????????????????????

            return true;
        }


        @Override
        public View getExpandView() {
            return itemView.findViewById(R.id.app_info_icon_info);
        }

        @Override
        public void onClick(View v) {
            keepOne.toggle(that);
            // ????????????Activity?????????????????????view
            ((AppListActivity)context).expandedView = that.itemView;
            ((AppListActivity)context).viewHolder = that;
            ((AppListActivity)context).pos = that.pos;
        }

    }

}
