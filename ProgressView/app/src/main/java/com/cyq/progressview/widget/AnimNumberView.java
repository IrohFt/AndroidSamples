package com.cyq.progressview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cyq.progressview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.cyq.progressview.widget.NumberView.DOWN_ANIMATOR_MODE;
import static com.cyq.progressview.widget.NumberView.UP_ANIMATOR_MODE;

/**
 * @author : ChenYangQi
 * date   : 2020/6/20 15:41
 * desc   : 温度变化的数字滚动控件
 */
public class AnimNumberView extends LinearLayout {
    /**
     * 温度模式
     */
    public static final int TEMPERATURE_MODE = 4658;
    /**
     * 时间模式
     */
    public static final int TIMER_MODE = 9889;
    /**
     * 正计时
     */
    public static final int UP_TIMER = 78648;
    /**
     * 倒计时
     */
    public static final int DOWN_TIMER = 56544;
    private int mMode = TIMER_MODE;
    private NumberView mSingleView;
    private NumberView mTenView;
    private NumberView mHundredView;
    private NumberView mHour1View;
    private NumberView mHour2View;
    private NumberView mMinute1View;
    private NumberView mMinute2View;
    private NumberView mSecond1View;
    private NumberView mSecond2View;
    private TextView mColon1View;
    private TextView mColon2View;
    private LinearLayout mTempContainer, mClockContainer;
    private Disposable mTimerDisposable;
    private OnTimerComplete mTimerListener;
    private int numberWidth;
    private int numberHeight;
    private int numberTextSize;
    private TextView mTvCircle;
    /**
     * 全局变量
     */
    Disposable disposable;

    public AnimNumberView(Context context) {
        this(context, null);
    }

    public AnimNumberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimNumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.widget_progress_temp_number_layout, this, true);
        mSingleView = findViewById(R.id.mSingleNumber);
        mTenView = findViewById(R.id.mTenNumber);
        mHundredView = findViewById(R.id.mHundredNumber);
        mHour1View = findViewById(R.id.mHour1);
        mHour2View = findViewById(R.id.mHour2);
        mMinute1View = findViewById(R.id.mMinute1);
        mMinute2View = findViewById(R.id.mMinute2);
        mSecond1View = findViewById(R.id.mSecond1);
        mSecond2View = findViewById(R.id.mSecond2);
        mColon1View = findViewById(R.id.mColon1);
        mColon2View = findViewById(R.id.mColon2);
        mTempContainer = findViewById(R.id.mTempNumberContainer);
        mClockContainer = findViewById(R.id.mClockContainer);
        mTvCircle = findViewById(R.id.tv_number_one);
        checkMode(mMode);
    }

    /**
     * 设置温度
     *
     * @param temperature
     */
    public void setTemperature(int temperature, int mode) {
        checkMode(mode);
        int mSingle = temperature % 10;
        int mTen = temperature / 10 % 10;
        int mHundred = temperature / 100 % 10;
        //设置控件的尺寸
        mSingleView.setLayoutSize(92, 190, 80);
        mTenView.setLayoutSize(92, 190, 80);
        mHundredView.setLayoutSize(92, 190, 80);
        mTvCircle.setTextSize(80);
        mSingleView.setCurrentValue(NumberView.POSITION_ONE, mSingle, temperature, 0);
        mTenView.setCurrentValue(NumberView.POSITION_TOW, mTen, temperature, 0);
        mHundredView.setCurrentValue(NumberView.POSITION_THREE, mHundred, temperature, 0);
    }

    /**
     * 设计正计时倒计时
     *
     * @param second
     * @param timerMode
     */
    public void setTimer(int second, int timerMode) {
        disposeTimer();
        checkMode(TIMER_MODE);
        if (timerMode == UP_TIMER && second <= 0) {
            //没有设置正计时的目标时间
            second = Integer.MAX_VALUE / 2;
        }
        final int finalSecond = second;
        mTimerDisposable = Flowable.intervalRange(0,
                second + 1,
                0,
                1,
                TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    if (timerMode == UP_TIMER) {
                        setClock(aLong.intValue(), UP_ANIMATOR_MODE);
                    }

                    if (timerMode == DOWN_TIMER) {
                        setClock(finalSecond - aLong.intValue(), DOWN_ANIMATOR_MODE);
                    }
                })
                .doOnComplete(() -> {
                    disposeTimer();
                    if (mTimerListener != null) {
                        mTimerListener.onComplete();
                    }
                })
                .subscribe();
    }

    /**
     * 设置时间 单位秒
     *
     * @param second
     */
    private void setClock(int second, int mode) {
        if (second > 60 - 1) {
            //大于1分钟
            mSecond1View.setVisibility(VISIBLE);
            mSecond2View.setVisibility(VISIBLE);
            mMinute1View.setVisibility(VISIBLE);
        } else if (second > 60 * 10 - 1) {
            //大于十分钟
            mMinute2View.setVisibility(VISIBLE);
        } else if (second > 60 * 60 - 1) {
            //大于60分钟
            mColon2View.setVisibility(VISIBLE);
            mHour1View.setVisibility(VISIBLE);
        } else if (second > 60 * 60 * 10 - 1) {
            //大于十个小时
            mHour2View.setVisibility(VISIBLE);
        }
        int hour, min, sec;
        hour = (int) (second / (60 * 60));
        min = (int) ((second - hour * 3600) / 60);
        sec = (int) (second - hour * 3600 - min * 60);
        System.out.println();
        //计算HH：MM：SS 6个位置的值
        int mHour1Value = hour / 10 % 10;
        int mHour2Value = hour % 10;
        int mMinute1Value = min / 10 % 10;
        int mMinute2Value = min % 10;
        int mSecond1Value = sec / 10 % 10;
        int mSecond2Value = sec % 10;

        if (mHour1Value > 0) {
            //展示六位数字
            numberWidth = 72;
            numberHeight = 152;
            numberTextSize = 64;
            mHour1View.setVisibility(VISIBLE);
            mHour2View.setVisibility(VISIBLE);
            mColon1View.setVisibility(VISIBLE);
            mMinute1View.setVisibility(VISIBLE);
        } else if (mHour2Value > 0) {
            //展示五位数字
            numberWidth = 82;
            numberHeight = 171;
            numberTextSize = 72;
            mHour1View.setVisibility(GONE);
            mHour2View.setVisibility(VISIBLE);
            mColon1View.setVisibility(VISIBLE);
            mMinute1View.setVisibility(VISIBLE);
        } else if (mMinute1Value > 0) {
            //展示四位数字
            numberWidth = 92;
            numberHeight = 190;
            numberTextSize = 80;
            mHour1View.setVisibility(GONE);
            mHour2View.setVisibility(GONE);
            mColon1View.setVisibility(GONE);
            mMinute1View.setVisibility(VISIBLE);
        } else {
            //展示三位数字
            numberWidth = 112;
            numberHeight = 228;
            numberTextSize = 96;
            mHour1View.setVisibility(GONE);
            mHour2View.setVisibility(GONE);
            mColon1View.setVisibility(GONE);
            mMinute1View.setVisibility(GONE);
        }

        mColon1View.setTextSize(numberTextSize);
        mColon2View.setTextSize(numberTextSize);
        mHour1View.setLayoutSize(numberWidth, numberHeight, numberTextSize);
        mHour2View.setLayoutSize(numberWidth, numberHeight, numberTextSize);
        mMinute1View.setLayoutSize(numberWidth, numberHeight, numberTextSize);
        mMinute2View.setLayoutSize(numberWidth, numberHeight, numberTextSize);
        mSecond1View.setLayoutSize(numberWidth, numberHeight, numberTextSize);
        mSecond2View.setLayoutSize(numberWidth, numberHeight, numberTextSize);

        mHour1View.setCurrentValue(NumberView.POSITION_SIX, mHour1Value, mode);
        mHour2View.setCurrentValue(NumberView.POSITION_FIVE, mHour2Value, mode);
        mMinute1View.setCurrentValue(NumberView.POSITION_FORT, mMinute1Value, mode);
        mMinute2View.setCurrentValue(NumberView.POSITION_THREE, mMinute2Value, mode);
        mSecond1View.setCurrentValue(NumberView.POSITION_TOW, mSecond1Value, mode);
        mSecond2View.setCurrentValue(NumberView.POSITION_ONE, mSecond2Value, mode);
    }

    /**
     * 判断当前控件展示的模式
     *
     * @param mode
     */
    private void checkMode(int mode) {
        if (mode == mMode) {
            return;
        }
        mMode = mode;
        if (mode == TEMPERATURE_MODE) {
            mTempContainer.setVisibility(VISIBLE);
            mClockContainer.setVisibility(GONE);
        }
        if (mode == TIMER_MODE || mode == UP_TIMER || mode == DOWN_TIMER) {
            mTempContainer.setVisibility(GONE);
            mClockContainer.setVisibility(VISIBLE);
        }
    }

    /**
     * 解绑计时器
     */
    private void disposeTimer() {
        if (mTimerDisposable != null && !mTimerDisposable.isDisposed()) {
            mTimerDisposable.dispose();
            mTimerDisposable = null;
        }
    }

    /**
     * 设置计时完成的回调
     *
     * @param mTimerListener
     */
    public void setOnTimerCompleteListener(OnTimerComplete mTimerListener) {
        this.mTimerListener = mTimerListener;
    }

    public interface OnTimerComplete {
        /**
         * 计时完成回调
         */
        void onComplete();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
