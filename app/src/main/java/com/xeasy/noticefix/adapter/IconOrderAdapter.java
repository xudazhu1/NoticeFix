package com.xeasy.noticefix.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.bean.IconFunc;
import com.xeasy.noticefix.dao.IconFuncDao;

import java.util.List;

/**
 * 可拖拽列表的适配器，
 */
public class IconOrderAdapter extends RecyclerView.Adapter<IconOrderAdapter.ViewHolder> {


    private final List<IconFuncDao.IconFuncStatus> dataList;
    private final ItemTouchHelper mItemTouchHelper;
    private final Context context;

    public IconOrderAdapter(List<IconFuncDao.IconFuncStatus> dataList, RecyclerView recyclerView
            , Context context) {
        this.dataList = dataList;
        this.context = context;
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new MyItemTouchHelperCallback(this, dataList, context));
        this.mItemTouchHelper = mItemTouchHelper;
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_config_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IconFuncDao.IconFuncStatus content = dataList.get(position);
        Integer descById = IconFunc.getDescById(content.iconFuncId);
        assert descById != null;
        // desc
        holder.tv.setText(context.getString(descById));
        // status
        holder.status.setChecked(content.active);

        // 调优先级事件
        holder.dragButton.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mItemTouchHelper.startDrag(holder);
            }
            v.performClick();
            return false;
        });
        // 开关事件
        holder.status.setOnCheckedChangeListener((buttonView, isChecked) -> {
            content.active = isChecked;
            // 持久化
            IconFuncDao.save(context, content);
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv;
        private final SwitchCompat status;
        private final ImageView dragButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.icon_config_content);
            status = itemView.findViewById(R.id.icon_config_switchCompat);
            dragButton = itemView.findViewById(R.id.icon_config_content_order);
        }
    }
}