package com.cyq.progressview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.cyq.progressview.R;
import com.cyq.progressview.Utils;
import com.cyq.progressview.evaluator.MyColors;
import com.cyq.progressview.evaluator.MyColorsEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author : ChenYangQi
 * date   : 2020/5/6 14:24
 * desc   : 温度进度控件
 */
public class MySmartProgressView extends View {
    /**
     * 控件宽高
     */
    private int width, height;
    /**
     * 粒子总个数
     */
    private int pointCount = 200;
    /**
     * 粒子列表
     */
    private List<AnimPoint> mPointList = new ArrayList<>(pointCount);
    /**
     * 粒子外层圆环原点坐标和半径长度
     */
    private int mCenterX, mCenterY, mRadius;
    /**
     * 粒子外层圆环的画笔
     */
    private Paint mCirclePaint;
    /**
     * 粒子画笔
     */
    private Paint mPointPaint;
    /**
     * 底色圆环画笔
     */
    private Paint mBackCirclePaiht;
    /**
     * 开始时底色圆环渐变的画笔
     */
    private Paint mBackShadePaint;
    /**
     * 白色
     */
    private int whiteColor = Color.parseColor("#FFFFFFFF");
    private int blackColor = Color.parseColor("#FF000000");
    private int endRadialGradientColor = Color.parseColor("#1978FF");
    private int middleRadialGradientColor = Color.parseColor("#1A001BFF");
    private int radialCircleColor = Color.parseColor("#FF0066FF");
    /**
     * 底色圆环的颜色
     */
    private int backCircleColor = Color.parseColor("#290066FF");
    /**
     * 透明颜色
     */
    private int transparentColor = Color.parseColor("#00000000");
    /**
     * 底色圆环初始化动画渐变色
     */
    private int[] backShaderColorArr = {transparentColor, transparentColor, blackColor};
    private float[] backPositionArr = {0, 0, 1};
    private int[] radialArr = {blackColor, blackColor, middleRadialGradientColor};

    /**
     * 0.64透明度 //0.16透明度
     */
    private int progressColor1 = Color.parseColor("#FF0066FF");
    private int startColor1 = Color.parseColor("#A30066FF");
    private int endColor1 = Color.parseColor("#230066FF");

    private int progressColor2 = Color.parseColor("#FFFFDB00");
    private int startColor2 = Color.parseColor("#A3FFDB00");
    private int endColor2 = Color.parseColor("#23FFDB00");

    private int progressColor3 = Color.parseColor("#FFFFA300");
    private int startColor3 = Color.parseColor("#A3FFA300");
    private int endColor3 = Color.parseColor("#23FFA300");

    private int progressColor4 = Color.parseColor("#FFFF8000");
    private int startColor4 = Color.parseColor("#A3FF8000");
    private int endColor4 = Color.parseColor("#23FF8000");

    private float[] radialPositionArr = {0F, 0.7F, 0.1F};
    private LinearGradient mBackCircleLinearGradient;
    private Paint mSweptPaint;
    private RadialGradient mRadialGradient;
    private Random mRandom = new Random();
    /**
     * 宽高等于控件大小额矩形
     */
    private RectF mRect;


    private Bitmap mBitmap;
    private Paint mBmpPaint;
    private float scaleHeight;
    private float scaleWidth;
    private int mCurrentAngle;

    /**
     * 外层粒子圆环的边框大小
     */
    private int mOutCircleStrokeWidth = 25;

    public MySmartProgressView(Context context) {
        this(context, null);
    }

    public MySmartProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySmartProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
        mCirclePaint = new Paint();
        mCirclePaint.setColor(endRadialGradientColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mOutCircleStrokeWidth);
        mCirclePaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.SOLID));

        mPointPaint = new Paint();
        mPointPaint.setColor(whiteColor);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));

        mSweptPaint = new Paint();
        mSweptPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSweptPaint.setColor(radialCircleColor);
        mRadialGradient = new RadialGradient(
                0,
                0,
                mRadius,
                radialArr,
                radialPositionArr,
                Shader.TileMode.CLAMP);
        mSweptPaint.setShader(mRadialGradient);

        /**
         * 圆弧画笔
         */
        Paint mArcPaint = new Paint();
        mArcPaint.setColor(Color.RED);
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setStrokeWidth(20);

        Paint mArcPathPaint = new Paint();
        mArcPathPaint.setColor(Color.RED);
        mArcPathPaint.setStyle(Paint.Style.STROKE);
        mArcPathPaint.setAntiAlias(true);

        mBackCirclePaiht = new Paint();
        mBackCirclePaiht.setColor(backCircleColor);
        mBackCirclePaiht.setStrokeWidth(20);
        mBackCirclePaiht.setAntiAlias(true);
        mBackCirclePaiht.setStyle(Paint.Style.STROKE);

        mBackShadePaint = new Paint();

        //绘制扇形path
        mArcPath = new Path();
        final ValueAnimator arcAnimator = ValueAnimator.ofInt(0, 3600);
        arcAnimator.setDuration(10000);
        arcAnimator.setRepeatMode(ValueAnimator.RESTART);
        arcAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arcAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mCurrentAngle = value;
                //获取此时的扇形区域path，用于裁剪动画粒子的canvas
                getSectorClip(width / 2F, -90, value / 10F);
            }
        });

        //颜色变化动画
        MyColors colors1 = new MyColors(progressColor1, startColor1, endColor1);
        MyColors colors2 = new MyColors(progressColor2, startColor2, endColor2);
        MyColors colors3 = new MyColors(progressColor3, startColor3, endColor3);
        MyColors colors4 = new MyColors(progressColor4, startColor4, endColor4);
        final ValueAnimator clickColorAnim = ValueAnimator.ofObject(new MyColorsEvaluator(),
                colors1, colors2, colors3, colors4);
        clickColorAnim.setDuration(10000);
        clickColorAnim.setRepeatCount(ValueAnimator.INFINITE);

        final float[] pos = {0F, 06F, 1F};

        clickColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                MyColors colors = (MyColors) animation.getAnimatedValue();
                mPointPaint.setColor(colors.getOutColor());
                mCirclePaint.setColor(colors.getOutColor());
                //设置内圈变色圆的shader
                radialArr[0] = Color.BLACK;
                radialArr[1] = colors.getEndColor();
                radialArr[2] = colors.getBeginColor();
                mRadialGradient = new RadialGradient(
                        0,
                        0,
                        mRadius,
                        radialArr,
                        pos,
                        Shader.TileMode.CLAMP);
                mSweptPaint.setShader(mRadialGradient);
            }
        });

        mPointList.clear();
        AnimPoint animPoint = new AnimPoint();
        animPoint.setAlpha(1);
        for (int i = 0; i < pointCount; i++) {
            //通过clone创建对象，避免重复创建
            AnimPoint cloneAnimPoint = animPoint.clone();
            cloneAnimPoint.init(mRandom, mRadius);
            mPointList.add(cloneAnimPoint);
        }
        //画运动粒子
        final ValueAnimator pointsAnimator = ValueAnimator.ofFloat(0.1F, 1F);
        pointsAnimator.setDuration(Integer.MAX_VALUE);
        pointsAnimator.setRepeatMode(ValueAnimator.RESTART);
        pointsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pointsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (AnimPoint point : mPointList) {
                    point.updatePoint(mRandom, mRadius);
                }
                invalidate();
            }
        });

        final ValueAnimator initAnimator = ValueAnimator.ofFloat(0, 1F);
        initAnimator.setDuration(1000);
        initAnimator.setInterpolator(new AccelerateInterpolator());
        initAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                backPositionArr[1] = value;
                mBackCircleLinearGradient = new LinearGradient(
                        0,
                        -mCenterX,
                        0,
                        mCenterY,
                        backShaderColorArr,
                        backPositionArr,
                        Shader.TileMode.CLAMP);
                mBackShadePaint.setShader(mBackCircleLinearGradient);
                invalidate();
            }
        });
        initAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                arcAnimator.start();
                clickColorAnim.start();
                pointsAnimator.start();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initAnimator.start();
            }
        }, 1000);

        //初始化指针Bitmap画布
        initBitmap();
    }

    /**
     * 初始化控件的各类宽高，边框，半径等大小
     */
    private void initView() {
        width = Utils.dip2px(300, getContext());
        height = Utils.dip2px(300, getContext());
        mCenterX = width / 2;
        mCenterY = width / 2;
        // 粒子圆环的宽度
        //TODO 这个25是外框距离圆环边框中点的距离，具体大小需要等UI设计图再确认
        mRadius = width / 2 - 20;
        mRect = new RectF(-mCenterX, -mCenterX, mCenterX, mCenterX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private Path mArcPath;

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        //step:画底色圆
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        canvas.drawCircle(0, 0, mRadius, mBackCirclePaiht);
        canvas.drawRect(mRect, mBackShadePaint);
        canvas.restore();

        //step2:画扇形区域的运动粒子
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        //把画布裁剪成扇形
        canvas.clipPath(mArcPath);
        //画运动粒子
        for (AnimPoint animPoint : mPointList) {
            canvas.drawCircle(animPoint.getmX(), animPoint.getmY(),
                    animPoint.getRadius(), mPointPaint);
        }
        canvas.drawCircle(0, 0, mRadius, mSweptPaint);
        canvas.drawCircle(0, 0, mRadius, mCirclePaint);
        canvas.restore();
        //画指针
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        canvas.rotate(mCurrentAngle / 10F);
        canvas.translate(-scaleWidth + 20, -scaleHeight - 10);
        canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
        canvas.restore();
    }

    /**
     * 绘制扇形path
     *
     * @param r
     * @param startAngle
     * @param sweepAngle
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getSectorClip(float r, float startAngle, float sweepAngle) {
        mArcPath.reset();
        mArcPath.addArc(-r, -r, r, r, startAngle, sweepAngle);
        mArcPath.lineTo(0, 0);
        mArcPath.close();
    }

    /**
     * 初始化指针图片的Bitmap
     */
    private void initBitmap() {
        mBmpPaint = new Paint();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator);
        float bitmapWidth = mBitmap.getWidth();
        float bitmapHeight = mBitmap.getHeight();
        scaleHeight = mCenterX;
        scaleWidth = bitmapWidth * (mCenterX / bitmapHeight);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) scaleWidth, (int) scaleHeight, false);
    }
}
