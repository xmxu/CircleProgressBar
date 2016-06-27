package com.github.xmxu.cpb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 圆圈型进度条
 * Created by Simon on 16/6/26.
 */
public class CircleProgressBar extends View {

    private Paint mPaint;
    private RectF mRectF;

    //默认属性值
    private static final int DEFAULT_STROKE_WIDTH = 10;
    private static final int DEFAULT_FINISHED_COLOR = Color.BLUE;
    private static final int DEFAULT_UNFINISHED_COLOR = Color.GRAY;
    private static final int DEFAULT_START_ANGLE = -90;
    private static final boolean DEFAULT_ROUND_CAP = true;

    //属性key
    public static final String KEY_INSTANCE_STATE = "key_instance_state";
    private static final String KEY_STROKE_WIDTH = "key_stroke_width";
    private static final String KEY_FINISHED_COLOR = "key_finished_color";
    private static final String KEY_UNFINISHED_COLOR = "key_unfinished_color";
    private static final String KEY_START_ANGLE = "key_start_angle";
    private static final String KEY_MAX = "key_max";
    private static final String KEY_PROGRESS = "key_progress";
    private static final String KEY_ROUND_CAP = "key_round_cap";

    /**
     * 进度最大值
     */
    private int mMax = 0;

    /**
     * 当前进度值
     */
    private int mProgress = 0;

    /**
     * 起始角度
     */
    private float mStartAngle = DEFAULT_START_ANGLE;

    /**
     * 画笔宽度
     */
    private int mStrokeWidth = DEFAULT_STROKE_WIDTH;

    /**
     * 完成进度的颜色
     */
    private int mFinishedColor = DEFAULT_FINISHED_COLOR;

    /**
     * 未完成进度的颜色
     */
    private int mUnfinishedColor = DEFAULT_UNFINISHED_COLOR;

    /**
     * 是否使用圆笔型
     */
    private boolean isRoundCap = DEFAULT_ROUND_CAP;

    /**
     * 自定义角度变化接口
     */
    private IAngleProvider mCustomDraw;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initParams();
    }

    private void initAttrs (AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        mStrokeWidth = (int) ta.getDimension(R.styleable.CircleProgressBar_cpb_stroke_width, DEFAULT_STROKE_WIDTH);
        mStartAngle = ta.getInteger(R.styleable.CircleProgressBar_cpb_start_angle, DEFAULT_START_ANGLE);
        mFinishedColor = ta.getColor(R.styleable.CircleProgressBar_cpb_finished_color, DEFAULT_FINISHED_COLOR);
        mUnfinishedColor = ta.getColor(R.styleable.CircleProgressBar_cpb_unfinished_color, DEFAULT_UNFINISHED_COLOR);
        mMax = ta.getInteger(R.styleable.CircleProgressBar_cpb_max, 0);
        mProgress = ta.getInteger(R.styleable.CircleProgressBar_cpb_progress, 0);
        isRoundCap = ta.getBoolean(R.styleable.CircleProgressBar_cpb_round_cap, true);
        ta.recycle();
    }

    private void initPaints () {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(isRoundCap ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    private void initParams () {
        if (mRectF == null) {
            mRectF = new RectF();
        }
        initPaints();
    }

    public void setMax(int max) {
        if (max != this.mMax && max > 0) {
            this.mMax = max;
            invalidate();
        }
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        if (this.mProgress > getMax()) {
            this.mProgress %= getMax();
        }
        invalidate();
    }

    public void setRoundCap(boolean roundCap) {
        if (isRoundCap != roundCap) {
            isRoundCap = roundCap;
            invalidate();
        }
    }

    public void setStrokeWidth(int strokeWidth) {
        if (mStrokeWidth != strokeWidth) {
            mStrokeWidth = strokeWidth;
            initPaints();
            requestLayout();
        }
    }

    public void setCustomDraw(IAngleProvider customDraw) {
        if (mCustomDraw != customDraw) {
            mCustomDraw = customDraw;
            invalidate();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int rectWidth = Math.min(measureWidth, measureHeight);
        float horizontalDelta = Math.abs((measureWidth - rectWidth) / 2 + mStrokeWidth);
        float verticalDelta = Math.abs((measureHeight - rectWidth) / 2 + mStrokeWidth);
        mRectF.set(horizontalDelta,
                verticalDelta,
                measureWidth - horizontalDelta,
                measureHeight - verticalDelta);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //总进度(底色)
        mPaint.setColor(mUnfinishedColor);
        canvas.drawArc(mRectF, 0, 360, false, mPaint);

        //完成进度
        mPaint.setColor(mFinishedColor);
        if (mCustomDraw != null) {
            //使用自定义角度
            float percent = getProgress() * 1.0f / getMax();
            canvas.drawArc(mRectF, mCustomDraw.getStartAngle(percent), mCustomDraw.getProgressAngle(percent), false, mPaint);
        } else {
            //使用默认角度变化
            float elapsedAngle = ((float) getProgress()) / getMax() * 360;
            float sweepAngle = 360 - elapsedAngle;
            canvas.drawArc(mRectF, mStartAngle + elapsedAngle, sweepAngle, false, mPaint);
        }

    }

    @Override
    public void invalidate() {
        initPaints();
        super.invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_STROKE_WIDTH, mStrokeWidth);
        bundle.putFloat(KEY_START_ANGLE, mStartAngle);
        bundle.putInt(KEY_FINISHED_COLOR, mFinishedColor);
        bundle.putInt(KEY_UNFINISHED_COLOR, mUnfinishedColor);
        bundle.putInt(KEY_MAX, mMax);
        bundle.putInt(KEY_PROGRESS, mProgress);
        bundle.putBoolean(KEY_ROUND_CAP, isRoundCap);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mStrokeWidth = bundle.getInt(KEY_STROKE_WIDTH, DEFAULT_STROKE_WIDTH);
            mStartAngle = bundle.getFloat(KEY_START_ANGLE, DEFAULT_START_ANGLE);
            mFinishedColor = bundle.getInt(KEY_FINISHED_COLOR, DEFAULT_FINISHED_COLOR);
            mUnfinishedColor = bundle.getInt(KEY_UNFINISHED_COLOR, DEFAULT_UNFINISHED_COLOR);
            setMax(bundle.getInt(KEY_MAX, 0));
            setProgress(bundle.getInt(KEY_PROGRESS, 0));
            isRoundCap = bundle.getBoolean(KEY_ROUND_CAP, true);
            super.onRestoreInstanceState(bundle.getParcelable(KEY_INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 自定义角度变化
     */
    public interface IAngleProvider {
        /**
         * 获取当前进度的角度
         * @param percent 当前进度
         * @return 当前角度
         */
        float getProgressAngle(float percent);

        /**
         * 获取起始角度
         * @param percent 当前进度
         * @return 起始角度
         */
        float getStartAngle(float percent);
    }


}
