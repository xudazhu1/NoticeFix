package com.xeasy.noticefix.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ExpandableViewHoldersUtil {

    /*//自定义处理列表中右侧图标，这里是一个旋转动画
    public static void rotateExpandIcon(final ImageView mImage, float from, float to) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);//属性动画
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mImage.setRotation((Float) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }*/

    //参数介绍：1、holder对象 2、展开部分的View，由holder.getExpandView()方法获取 3、animate参数为true，则有动画效果
    public static void openHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.VISIBLE);
            //改变高度的动画
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder);
            //扩展的动画，结束后透明度动画开始
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 1);
                    alphaAnimator.addListener(new ViewHolderAnimator.ViewHolderAnimatorListener(holder));
                    alphaAnimator.start();
                }
            });
            animator.start();
        } else { //为false时直接显示
            expandView.setVisibility(View.VISIBLE);
            expandView.setAlpha(1);
        }
    }

    //类似于打开的方法
    public static void closeHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.GONE);
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder);
            expandView.setVisibility(View.VISIBLE);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
            });
            animator.start();
        } else {
            expandView.setVisibility(View.GONE);
            expandView.setAlpha(0);
        }
    }

    //获取展开部分的View
    public interface Expandable {
        View getExpandView();
    }

    public static class KeepOneHolder<VH extends RecyclerView.ViewHolder & Expandable> {
        //-1表示所有item是关闭状态，opend为pos值的表示pos位置的item为展开的状态
        private int opened = -1;

        /**
         * 此方法是在Adapter的onBindViewHolder()方法中调用
         *
         * @param holder holder对象
         * @param pos    下标
         */
        public void bind(VH holder, int pos) {
            if (pos == opened) //展开ExpandView
                ExpandableViewHoldersUtil.openHolder(holder, holder.getExpandView(), false);
            else //关闭ExpandView
                ExpandableViewHoldersUtil.closeHolder(holder, holder.getExpandView(), false);
        }

        /**
         * 响应ViewHolder的点击事件
         *
         * @param holder    holder对象
         * ?? @param imageView 这里我传入了一个ImageView对象，为了处理图片旋转的动画，为了处理内部业务
         */
        @SuppressWarnings("unchecked")
        public void toggle(VH holder) {
            if (opened == holder.getLayoutPosition()) { //点击的就是打开的Item，则关闭item，并将opend置为-1
                opened = -1;
//                ExpandableViewHoldersUtil.rotateExpandIcon(imageView, 180, 0);
                ExpandableViewHoldersUtil.closeHolder(holder, holder.getExpandView(), true);
            } else { //点击的是本来关闭的Item，则把opend值换成当前pos，把之前打开的Item给关掉
                int previous = opened;
                opened = holder.getLayoutPosition();
//                ExpandableViewHoldersUtil.rotateExpandIcon(imageView, 0, 180);
                ExpandableViewHoldersUtil.openHolder(holder, holder.getExpandView(), true);
                //动画关闭之前打开的Item
                final VH oldHolder = (VH) ((RecyclerView) holder.itemView.getParent()).findViewHolderForPosition(previous);
                if (oldHolder != null)
                    ExpandableViewHoldersUtil.closeHolder(oldHolder, oldHolder.getExpandView(), true);
            }
        }
    }

}