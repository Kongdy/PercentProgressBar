package com.kongdy.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.kongdy.percentprogressbar.R;

/**
 * @author kongdy
 * @date 2017/12/4 14:03
 * @describe 百分比进度条
 **/
public class PercentProgressBar extends ViewGroup {

    // for progress value paint
    private Paint mainPaint;
    // for progress background paint
    private Paint backPaint;
    // no round end paint
    private Paint normalPaint;
    // data array
    private SparseArray<KProgressBarData> dataArray = new SparseArray<>();

    private int mWidth;
    private int mHeight;

    private int progressWidth = 40;
    private RectF barBounds = new RectF();
    // 进度条最大值
    private float maxProgressValue;
    // center view
    private View centerView;
    // animation
    private ValueAnimator animator;
    // animation phase
    private float phase = 0f;

    private int currentOpenType = OpenType.RESET_ANIMATION;

    public PercentProgressBar(Context context) {
        this(context, null);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWidget();
        applyAttr(attrs);
    }

    // fetch attr
    private void applyAttr(AttributeSet attrs) {

        int backPaintColor = Color.GRAY;

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PercentProgressBar);

        for (int i = 0; i < ta.getIndexCount(); i++) {
            int cursorAttr = ta.getIndex(i);
            if (cursorAttr == R.styleable.PercentProgressBar_ppb_progress_back) {
                backPaintColor = ta.getColor(R.styleable.PercentProgressBar_ppb_progress_back, Color.GRAY);
            } else if (cursorAttr == R.styleable.PercentProgressBar_ppb_progress_width) {
                progressWidth = (int) ta.getDimension(R.styleable.PercentProgressBar_ppb_progress_width, 40f);
            }
        }

        ta.recycle();
        mainPaint.setStrokeWidth(progressWidth);
        backPaint.setStrokeWidth(progressWidth);
        normalPaint.setStrokeWidth(progressWidth);

        backPaint.setColor(backPaintColor);
    }

    private void initWidget() {
        mainPaint = new Paint();
        backPaint = new Paint();
        normalPaint = new Paint();

        // enable paint high affect
        highAffect(mainPaint);
        highAffect(backPaint);
        highAffect(normalPaint);

        // round ends
        mainPaint.setStrokeJoin(Paint.Join.ROUND);
        mainPaint.setStrokeCap(Paint.Cap.ROUND);

        mainPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStyle(Paint.Style.STROKE);
        normalPaint.setStyle(Paint.Style.STROKE);
    }

    private void highAffect(Paint paint) {
        if (null == paint)
            return;
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        barBounds.left = getPaddingLeft() + progressWidth / 2;
        barBounds.top = getPaddingTop() + progressWidth / 2;
        barBounds.right = mWidth + getPaddingLeft() - progressWidth / 2;
        barBounds.bottom = mHeight + getPaddingTop() - progressWidth / 2;

        measureCenterView();
    }

    protected void measureCenterView() {
        if (null != centerView) {

            int centerViewWidth = mWidth - getPaddingLeft() - getPaddingRight() - progressWidth * 4;
            int centerViewHeight = mHeight - getPaddingTop() - getPaddingBottom() - progressWidth * 4;

            centerView.measure(MeasureSpec.makeMeasureSpec(centerViewWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(centerViewHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
   //     super.onLayout(b, i, i1, i2, i3);
        // layout center view
        if (null != centerView) {
            final int left = i + progressWidth * 2;
            final int top = i1 + progressWidth * 2;
            final int right = i2 - getPaddingRight() - progressWidth * 2;
            final int bottom = i3 - getPaddingBottom() - progressWidth * 2;
            centerView.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.saveLayer(0, 0, getMeasuredWidth(),
                getMeasuredHeight(), mainPaint, Canvas.ALL_SAVE_FLAG);
        // draw center
        drawCenter(canvas);
        // draw progress back
        canvas.drawArc(barBounds, 0, 360, false, backPaint);
        // draw progress
        drawProgress(canvas);

        canvas.restore();
    }

    private void drawProgress(Canvas canvas) {
        // 进度条两部分分开画，来达到潘洛斯阶梯的效果
        // draw front part
        float cursorStartAngle = -90;
        for (int i = 0; i < dataArray.size(); i++) {
            KProgressBarData data = dataArray.valueAt(i);
            float sweepAngle = 360 * data.getPercentValue();
            normalPaint.setColor(data.getColor());
            canvas.drawArc(barBounds, cursorStartAngle * phase, (sweepAngle * 4f / 5f) * phase, false, normalPaint);
            cursorStartAngle += sweepAngle;
        }
        // draw end part
        cursorStartAngle = -90;
        for (int i = 0; i < dataArray.size(); i++) {
            KProgressBarData data = dataArray.valueAt(i);
            float sweepAngle = 360 * data.getPercentValue();
            mainPaint.setColor(data.getColor());
            canvas.drawArc(barBounds, (cursorStartAngle + sweepAngle * 4f / 5f) * phase, sweepAngle * phase / 5f, false, mainPaint);
            cursorStartAngle += sweepAngle;
        }
    }

    private void drawCenter(Canvas canvas) {
        if (null != centerView) {
            // calc offset
            int offsetX = getPaddingLeft() + progressWidth * 2;
            int offsetY = getPaddingTop() + progressWidth * 2;
            canvas.translate(offsetX, offsetY);
            centerView.draw(canvas);
            canvas.translate(-offsetX, -offsetY);
        }
    }

    public void setDataArray(SparseArray<KProgressBarData> dataArray) {
        setDataArray(dataArray, OpenType.RESET_ANIMATION);
    }

    public void setDataArray(SparseArray<KProgressBarData> dataArray, int openType) {
        this.dataArray = dataArray;
        this.currentOpenType = openType;
        calcProperty();
    }

    private void calcProperty() {
        if (dataArray == null || dataArray.size() == 0)
            return;

        // calc max value
        maxProgressValue = 0;
        for (int i = 0; i < dataArray.size(); i++) {
            KProgressBarData data = dataArray.valueAt(i);
            maxProgressValue += data.getValue();
        }
        // calc percent value
        for (int i = 0; i < dataArray.size(); i++) {
            KProgressBarData data = dataArray.valueAt(i);
            final float value = data.getValue();
            data.setPercentValue(value / maxProgressValue);
        }

        if (currentOpenType == OpenType.RESET_LOAD) {
            phase = 1f;
            reDraw();
        } else {
            phase = 0f;
            if (animator != null && animator.isRunning())
                animator.cancel();
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setStartDelay(200);
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PercentProgressBar.this.phase = valueAnimator.getAnimatedFraction();
                    reDraw();
                }
            });
            animator.start();
        }

    }

    private void reDraw() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }


    public View getCenterView() {
        return centerView;
    }

    // set center view
    public void setCenterView(View centerView) {
        this.centerView = centerView;
        removeAllViews();
        addView(centerView);
    }

    public static final class OpenType {
        /**
         * 无动画，直接重置数据
         */
        public static final int RESET_LOAD = 0x000001;
        /**
         * 先重置数据，然后播放动画
         */
        public static final int RESET_ANIMATION = 0x000002;
        /**
         * 直接播放动画，会从当前进度播放到目标进度的动画
         */
        public static final int SMOOTH_ANIMATION = 0x000003;
    }
}
