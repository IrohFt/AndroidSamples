package com.cyq.library;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Time: 2019-12-07 21:44
 * Author: ChenYangQi
 * Description:烤面包刻度选择器
 */
public class BreadScaleView extends ObservableScrollView implements ObservableScrollView.OnScrollListener {
    private List<ScaleBean> mData = new ArrayList<>();
    private LinearLayout container;
    private Context mContext;
    private int itemHeight = 160;//item默认高度
    private int displayCount = 9;//默认展示9个item
    private int tvSize = 24;//刻度文字默认大小，单位：sp
    private int ivWidth = 140;//面包图片宽高
    private int lineWidth = 200;//中间横线宽度
    private int lineHeight = 12;//中间横线的高度
    private int lineMarginLeft = 100;//刻度文字marginLeft
    private int lineMarginRight = 70;//面包图标marginRight
    private int lightColor = Color.parseColor("#616161");//未选中刻度颜色
    private int middleColor = Color.parseColor("#fafafa");//二级刻度颜色
    private int heightColor = Color.parseColor("#ff6d00");//三级刻度颜色

    public BreadScaleView(Context context) {
        this(context, null);
    }

    public BreadScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BreadScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 控件初始化
     */
    private void init() {
        this.setVerticalScrollBarEnabled(false);
        for (int i = 0; i < 20; i++) {
            ScaleBean bean = new ScaleBean();
            bean.setScale(String.valueOf(i + 1));
            bean.setType(ScaleType.MIDDLE);
            mData.add(bean);
        }

        mContext = getContext();
        container = new LinearLayout(mContext);
        container.setOrientation(LinearLayout.VERTICAL);
        for (ScaleBean bean : mData) {
            container.addView(createItemView(bean.getScale(), bean.getType()));
        }
        this.addView(container);
        setOnScrollListener(this);
    }

    /**
     * 创建Item
     *
     * @param scaleValue 刻度上要展示的文字
     * @param empty      展示左侧面包的图标类型
     * @return 返回item的View
     */
    private View createItemView(String scaleValue, ScaleType empty) {
        LinearLayout itemView = new LinearLayout(mContext);
        itemView.setOrientation(LinearLayout.HORIZONTAL);
        itemView.setGravity(Gravity.CENTER_VERTICAL);
        ViewGroup.LayoutParams itemLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, itemHeight);
        itemView.setLayoutParams(itemLayoutParams);

        //创建刻度文字
        TextView tv = new TextView(mContext);
        MarginLayoutParams tvLayoutParams =
                new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
        tv.setLayoutParams(tvLayoutParams);
        tv.setSingleLine(true);
        //TODO 需要根据不同的状态变更颜色
        tv.setTextColor(lightColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, tvSize);
        tv.setText(scaleValue);
        tv.setGravity(Gravity.CENTER);

        //创建面包图片
        ImageView iv = new ImageView(mContext);
        MarginLayoutParams ivLayoutParams = new MarginLayoutParams(ivWidth, ivWidth);
        iv.setLayoutParams(ivLayoutParams);
        //TODO 替换成面包图标
        iv.setBackgroundResource(R.drawable.bread_type_height);

        //创建中间的横线刻度
        View lineView = new View(mContext);
        MarginLayoutParams layoutParams = new MarginLayoutParams(lineWidth, lineHeight);
        layoutParams.leftMargin = lineMarginLeft;
        layoutParams.rightMargin = lineMarginRight;
        lineView.setLayoutParams(layoutParams);
        //TODO 需要根据不同的状态变更颜色
        lineView.setBackgroundColor(lightColor);

        itemView.addView(iv);
        itemView.addView(lineView);
        itemView.addView(tv);
        return itemView;
    }

    /**
     * 修改ScrollView的滑动速度
     *
     * @param velocityY
     */
    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 2);
    }

    /**
     * 设置控件高度为=item高度*item个数
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, itemHeight * displayCount);
    }

    /**
     * 滑动状态监听
     *
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(ObservableScrollView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                //停止滑动
                if (!autoScrollTag) {
                    return;
                }
                int topCount = scrollY / itemHeight;
                int criticalY = scrollY % itemHeight;
                Log.i("test", "topCount:" + topCount + "-----criticalY:" + criticalY);
                if (criticalY > itemHeight / 2) {
                    //滑动到下一个位置
                    BreadScaleView.this.smoothScrollTo(0, (topCount + 1) * itemHeight);
                } else {
                    //滑动到上一个位置
                    BreadScaleView.this.smoothScrollTo(0, topCount * itemHeight);
                }
                autoScrollTag = false;
                break;
            case SCROLL_STATE_TOUCH_SCROLL://按下
            case SCROLL_STATE_FLING://开始滑动
                autoScrollTag = true;
                break;
            default:
        }
    }

    int scrollY = 0;//标记Y方向已滑动距离
    boolean autoScrollTag = false;//用来标记是否是自动滚动

    @Override
    public void onScroll(ObservableScrollView view, boolean isTouchScroll, int x, int y, int oldX
            , int oldY) {
//        Log.i("test", "fling-----");
        scrollY = y;
    }
}
