package tgi.com.tgifreertobtdemo.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.ArgbEvaluator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.concurrent.Semaphore;

/**
 * Author: leo
 * Data: On 26/11/2018
 * Project: TgiFreeRtoBtDemo
 * Description:
 */
public class Led extends View {
    private static final int DEFAULT_WIDTH=20;
    private static final int DEFAULT_HEIGHT=20;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private Semaphore mSemaphore=new Semaphore(1);

    public Led(Context context) {
        this(context,null);
    }

    public Led(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Led(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint=new Paint(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width=calculateDimension(widthMeasureSpec,DEFAULT_WIDTH);
        int height = calculateDimension(heightMeasureSpec, DEFAULT_HEIGHT);
        setMeasuredDimension(width,height);
    }

    private int calculateDimension(int measureSpec, int defaultValue) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode){
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                size=defaultValue;
                break;

        }
        return size;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=w;
        mHeight=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mWidth/2,mHeight/2,10,mPaint);
    }

    public void animateColorChanging(){
        if(!mSemaphore.tryAcquire()){
            return;
        }
        ValueAnimator animator=ValueAnimator
                .ofObject(new android.animation.ArgbEvaluator(),Color.BLACK,Color.RED,Color.GREEN,Color.BLUE,Color.BLACK)
                .setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSemaphore.release();

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mSemaphore.release();

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                Log.e("animateColorChanging",value+"");
                mPaint.setColor(value);
                invalidate();
            }
        });
        animator.start();




    }
}
