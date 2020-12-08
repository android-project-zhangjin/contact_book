package com.example.contact_book;

/**
 * 有关WheelView实现的代码大部分参考以下Github开源项目，有源代码的直接使用部分
 * https://github.com//ycuwq/DatePicker
 */


import android.content.Context;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.text.AttributedCharacterIterator;
import java.text.Format;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/*
    *滚动选择器
 */

public class WheelView<T> extends View {

    //数据列表
    private List<T> mDataList;

    //数据转换器
    private Format mDataFormat;

    //Item中Text的外观
    private int mTextColor;
    private int mTextSize;
    private Paint mTextPaint;

    //字体渐变标记
    private boolean mIsTextGradual;

    //被选中Item的外观
    private int mSelectedItemTextColor;
    private int mSelectedItemTextSize;
    private Paint mSelectedItemPaint;

    //指示器外观
    private String mIndicatorText;
    private int mIndicatorTextColor;
    private int mIndicatorTextSize;
    private Paint mIndicatorPaint;


    /**
     * Item外观
     * 用于设置Item
     */
    //最大Item的文本宽高
    private int mTextMaxWidth, mTextMaxHeight;

    //测量mTextMaxWidth的文字
    private String mItemMaximumWidthText;

    //显示Item一半的数量
    private int mHalfVisibleItemCount;

    //两个Item之间的高度
    private int mItemHeightSpace, mItemWidthSpace;
    //Item高度
    private int mItemHeight;

    //当前Item的位置
    private int mCurrentPosition;

    //中间Item放大标记
    private boolean mIsZoomInSelectedItem;

    //中心被选中的Item的坐标矩形
    private Rect mSelectedItemRect;

    //第一个Item绘制的Text坐标
    private int mFirstItemDrawX, mFirstItemDrawY;

    //中心Item绘制Text的Y轴坐标
    private int mCenterItemDrawY;


    /**
     * 幕布外观
     * 中央Item会遮盖一个颜色
     */
    //显示幕布标记
    private boolean mIsShowCurtain;
    private boolean mIsShowCurtainBorder;
    private int mCurtainColor;
    private int mCurtainBorderColor;


    //整个控件的可绘制面积
    private Rect mDrawRect;

    //滚动控件
    private Scroller mScroller;

    //滚动的距离
    private int mTouchSlop;

    //一次滑动只产生一个Item移动标志
    private boolean mTouchSlopFlag;

    //VelocityTracker是用于追踪滑动数据的类，实现fling操作
    private VelocityTracker mTracker;

    //触摸位置的Y坐标
    private int mTouchDownY;

    //Y轴Scroll滚动的位移
    private int mScrollOffsetY;

    //触摸手指离开的Y坐标
    private int mLastDownY;

    //滚动循环标志
    private boolean mIsCyclic=true;

    //fling的最大/最小距离
    private int mMaxFlingY, mMinFlingY;

    //滚轮滚动最大/最小速度
    private int mMaximumVelocity=50, mMinimumVelocity=1000;

    //手动停止滚动标志
    private boolean mIsAbortScroller;

    private Paint mPaint;


    //渐变类
    private LinearGradient mLinearGradient;


    //MessageQuery处理
    private android.os.Handler mHandler=new android.os.Handler();

    private OnWheelChangeListener<T> mOnWheelChangeListener;

    private Runnable mScrollerRunnable=new Runnable() {
        @Override
        public void run() {
            /*
             * 检查是否正在滚动，并获取滚动中新的坐标
             * Scroller.computeScrollOffset() 返回一个boolean，如果滚动未完成，返回True
             */
            if(mScroller.computeScrollOffset()){
                mScrollOffsetY=mScroller.getCurrY();
                postInvalidate();
                mHandler.postDelayed(this,16);
            }
            if(mScroller.isFinished() || (mScroller.getFinalY()==mScroller.getCurrY() && mScroller.getFinalX()==mScroller.getCurrX())){
                if(mItemHeight==0)
                    return;
                int position=-mScrollOffsetY/mItemHeight;
                position=fixItemPosition(position);
                //滚动后item位于正中
                if(mCurrentPosition != position){
                    mCurrentPosition=position;
                    if(mOnWheelChangeListener == null){
                        return;
                    }

                    mOnWheelChangeListener.onWheelSelected(mDataList.get(position),position);
                }
            }
        }
    };

    public WheelView(Context context){this(context,null);}

    public WheelView(Context context, AttributeSet attrs){this(context,attrs,0);}

    public WheelView(Context context,AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        initAttrs(context,attrs);
        initPaint();
        mLinearGradient=new LinearGradient(mTextColor,mSelectedItemTextColor);
        mDrawRect=new Rect();
        mSelectedItemRect=new Rect();
        mScroller=new Scroller(context);

        ViewConfiguration configuration=ViewConfiguration.get(context);
        mTouchSlop=configuration.getScaledTouchSlop();
    }


    /**
     * 为WheelView布局的参数设置默认参数
     * @param context 上下文
     * @param attrs 布局参数
     */
    private void initAttrs(Context context,AttributeSet attrs){
        if(attrs==null){
            return;
        }
        TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.WheelView);
        //原代码为14sp，但是发现函数要求的参数为int
        mTextSize=a.getDimensionPixelSize(R.styleable.WheelView_itemTextSize,getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize));
        mTextColor=a.getColor(R.styleable.WheelView_itemTextColor, Color.BLUE);
        mIsTextGradual=a.getBoolean(R.styleable.WheelView_textGradual,true);
        mIsCyclic=a.getBoolean(R.styleable.WheelView_wheelCyclic,false);
        mHalfVisibleItemCount=a.getInteger(R.styleable.WheelView_halfVisibleItemCount,2);
        mItemMaximumWidthText=a.getString(R.styleable.WheelView_itemMaximumWidthText);
        mSelectedItemTextColor=a.getColor(R.styleable.WheelView_selectedTextColor,Color.BLACK);
        mSelectedItemTextSize=a.getDimensionPixelSize(R.styleable.WheelView_selectedTextSize,getResources().getDimensionPixelSize(R.dimen.WheelSelectedItemTextSize));
        mCurrentPosition=a.getInteger(R.styleable.WheelView_currentItemPosition,0);
        mItemWidthSpace=a.getDimensionPixelSize(R.styleable.WheelView_itemWidthSpace,getResources().getDimensionPixelSize(R.dimen.WheelItemWidthSpace));
        mItemHeightSpace=a.getDimensionPixelSize(R.styleable.WheelView_itemHeightSpace,getResources().getDimensionPixelSize(R.dimen.WheelItemHeightSpace));
        mIsZoomInSelectedItem=a.getBoolean(R.styleable.WheelView_zoomInSelectedItem,true);
        mIsShowCurtain=a.getBoolean(R.styleable.WheelView_wheelCurtain,true);
        mCurtainColor=a.getColor(R.styleable.WheelView_wheelCurtainColor,Color.parseColor("#00000000"));
        mIsShowCurtainBorder=a.getBoolean(R.styleable.WheelView_wheelCurtainBorder,true);
        mCurtainBorderColor=a.getColor(R.styleable.WheelView_wheelCurtainBorderColor,Color.WHITE);
        mIndicatorText=a.getString(R.styleable.WheelView_indicatorText);
        mIndicatorTextColor=a.getColor(R.styleable.WheelView_indicatorTextColor,mSelectedItemTextColor);
        mIndicatorTextSize=a.getDimensionPixelSize(R.styleable.WheelView_indicatorTextSize,mTextSize);

        a.recycle();
    }

    public void computeTextSize(){
        mTextMaxWidth=mTextMaxHeight=0;
        if(mDataList.size()==0)
            return;

        //这里使用最大的，防止文字大小超过布局大小
        mPaint.setTextSize(Math.max(mSelectedItemTextSize, mTextSize));

        if(!TextUtils.isEmpty(mItemMaximumWidthText)){
            mTextMaxWidth=(int)mPaint.measureText(mItemMaximumWidthText);
        }
        else{
            mTextMaxWidth=(int)mPaint.measureText(mDataList.get(0).toString());
        }
        Paint.FontMetrics metrics=mPaint.getFontMetrics();
        mTextMaxHeight=(int)(metrics.bottom-metrics.top);
    }

    private void initPaint(){
        //ANTI_ALIAS_FLAG 抗锯齿
        //DITHER_FLAG 抖动
        //LINEAR_TEXT_FLAG 文本平滑线性缩放

        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mSelectedItemPaint=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mSelectedItemPaint.setStyle(Paint.Style.FILL);
        mSelectedItemPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedItemPaint.setColor(mSelectedItemTextColor);
        mSelectedItemPaint.setTextSize(mSelectedItemTextSize);

        mIndicatorPaint=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        mIndicatorPaint.setColor(mIndicatorTextColor);
        mIndicatorPaint.setTextSize(mIndicatorTextSize);

    }

    /**
     * 计算实际大小
     * @param specMode 测量模式
     * @param specSize  测量的大小
     * @param size  需要的大小
     * @return 返回实际大小
     */
    private int measureSize(int specMode, int specSize, int size){
        if(specMode==MeasureSpec.EXACTLY){
            return specSize;
        } else {
            return Math.min(specSize,size);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthSize=MeasureSpec.getSize(widthMeasureSpec);
        int specWidthMode=MeasureSpec.getMode(widthMeasureSpec);
        int specHeightSize=MeasureSpec.getSize(heightMeasureSpec);
        int specHeightMode=MeasureSpec.getMode(heightMeasureSpec);

        int width=mTextMaxWidth+mItemWidthSpace;
        int height=(mTextMaxHeight + mItemHeightSpace) * getVisibleItemCount();

        width += getPaddingLeft()+getPaddingRight();
        height += getPaddingBottom()+getPaddingTop();
        setMeasuredDimension(measureSize(specWidthMode,specWidthSize,width),measureSize(specHeightMode,specHeightSize,height));
    }

    /**
     * 计算Fling极限
     * 如果可循环则取Integer的极限值，否则为数据集的上下限
     */
    private void computeFlingLimitY(){
        if(mDataList==null)
            return;
        mMinFlingY=mIsCyclic ? Integer.MIN_VALUE : (-mItemHeight*(mDataList.size()-1));
        mMaxFlingY=mIsCyclic ? Integer.MAX_VALUE : 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawRect.set(getPaddingLeft(),getPaddingTop(),getWidth()-getPaddingRight(),getHeight()-getPaddingBottom());
        mItemHeight=mDrawRect.height() / getVisibleItemCount();
        mFirstItemDrawX=mDrawRect.centerX();
        mFirstItemDrawY=(int) ((mItemHeight - (mSelectedItemPaint.ascent() + mSelectedItemPaint.descent())) / 2);

        mSelectedItemRect.set(getPaddingLeft(),mItemHeight*mHalfVisibleItemCount,getWidth()-getPaddingRight(),mItemHeight+mItemHeight*mHalfVisibleItemCount);
        computeFlingLimitY();
        mCenterItemDrawY=mFirstItemDrawY+mItemHeight*mHalfVisibleItemCount;
        mScrollOffsetY=-mItemHeight*mCurrentPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setTextAlign(Paint.Align.CENTER);
        if(mIsShowCurtain){
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCurtainColor);
            canvas.drawRect(mSelectedItemRect,mPaint);
        }
        if(mIsShowCurtainBorder){
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCurtainBorderColor);
            canvas.drawRect(mSelectedItemRect,mPaint);
            canvas.drawRect(mDrawRect,mPaint);
        }

        int drawnSelectedPos=-mScrollOffsetY/mItemHeight;
        mPaint.setStyle(Paint.Style.FILL);

        //首尾各多绘制一个作为缓冲
        for(int drawDataPos=drawnSelectedPos-mHalfVisibleItemCount-1;drawDataPos<=drawnSelectedPos+mHalfVisibleItemCount+1;drawDataPos++){
            int position=drawDataPos;
            if(mIsCyclic){
                position=fixItemPosition(position);
            } else {



                if(mDataList==null)
                    continue;
                if(position<0 || position > mDataList.size()-1){
                    continue;
                }
            }

            T data=mDataList.get(position);
            int itemDrawY=mFirstItemDrawY+(drawDataPos+mHalfVisibleItemCount)*mItemHeight+mScrollOffsetY;

            int distanceY=Math.abs(mCenterItemDrawY-itemDrawY);

            //渐变
            if(mIsTextGradual){
                //计算文字的颜色渐变
                if(distanceY<mItemHeight){
                    float colorRatio=1-(distanceY/(float)mItemHeight);
                    mSelectedItemPaint.setColor(mLinearGradient.getColor(colorRatio));
                } else {
                    mSelectedItemPaint.setColor(mSelectedItemTextColor);
                    mTextPaint.setColor(mTextColor);
                }

                //计算透明度渐变
                float alphaRatio;
                if(itemDrawY>mCenterItemDrawY){
                    alphaRatio=(mDrawRect.height()-itemDrawY)/(float)(mDrawRect.height()-(mCenterItemDrawY));
                } else {
                    alphaRatio=itemDrawY/(float)mCenterItemDrawY;
                }

                alphaRatio=alphaRatio < 0 ? 0 : alphaRatio;
                mSelectedItemPaint.setAlpha((int)(alphaRatio*255));
                mTextPaint.setAlpha((int)(alphaRatio*255));
            }

            //中心项放大
            if(mIsZoomInSelectedItem){
                if(distanceY<mItemHeight){
                    float addedSize=(mItemHeight-distanceY)/(float)mItemHeight*(mSelectedItemTextSize-mTextSize);
                    mSelectedItemPaint.setTextSize(mTextSize+addedSize);
                    mTextPaint.setTextSize(mTextSize+addedSize);
                } else {
                    mSelectedItemPaint.setTextSize(mTextSize);
                    mTextPaint.setTextSize(mTextSize);
                }
            } else {
                mSelectedItemPaint.setTextSize(mTextSize);
                mTextPaint.setTextSize(mTextSize);
            }

            String drawText=mDataFormat == null ? data.toString() : mDataFormat.format(data);
            //在中间位置的Item作为被选中的
            if(distanceY<mItemHeight/2){
                canvas.drawText(drawText,mFirstItemDrawX,itemDrawY,mSelectedItemPaint);
            } else {
                canvas.drawText(drawText,mFirstItemDrawX,itemDrawY,mTextPaint);
            }
        }

        if(!TextUtils.isEmpty(mIndicatorText)){
            canvas.drawText(mIndicatorText,mFirstItemDrawX+(int)(mTextMaxWidth/2),mCenterItemDrawY,mIndicatorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mTracker==null){
            mTracker=VelocityTracker.obtain();
        }

        mTracker.addMovement(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                    mIsAbortScroller=true;
                } else {
                    mIsAbortScroller=false;
                }
                mTracker.clear();
                mTouchDownY=mLastDownY=(int)event.getY();
                mTouchSlopFlag=true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchSlopFlag && Math.abs(mTouchDownY-event.getY())<mTouchSlop){
                    break;
                }
                mTouchSlopFlag=false;
                float move=event.getY()-mLastDownY;
                mScrollOffsetY += move;
                mLastDownY=(int)event.getY();
                invalidate();;
                break;
            case MotionEvent.ACTION_UP:
                if(!mIsAbortScroller && mTouchDownY==mLastDownY){
                    performClick();
                    if(event.getY()>mSelectedItemRect.bottom){
                        int scrollItem=(int)(event.getY()-mSelectedItemRect.bottom)/mItemHeight+1;
                        mScroller.startScroll(0,mScrollOffsetY,0,-scrollItem*mItemHeight);
                    } else if(event.getY()<mSelectedItemRect.top){
                        int scrollItem=(int)(mSelectedItemRect.top-event.getY())/mItemHeight +1;
                        mScroller.startScroll(0,mScrollOffsetY,0,scrollItem*mItemHeight);
                    }
                } else {
                    mTracker.computeCurrentVelocity(1000,mMaximumVelocity);
                    int velocity=(int)mTracker.getYVelocity();
                    if(Math.abs(velocity)>mMinimumVelocity){
                        mScroller.fling(0,mScrollOffsetY,0,velocity,0,0,mMinFlingY,mMaxFlingY);
                        mScroller.setFinalY(mScroller.getFinalY()+computeDistanceToEndPoint(mScroller.getFinalY()%mItemHeight));
                    } else {
                        mScroller.startScroll(0,mScrollOffsetY,0,computeDistanceToEndPoint(mScrollOffsetY%mItemHeight));
                    }
                }
                if(!mIsCyclic){
                    if(mScroller.getFinalY()>mMaxFlingY){
                        mScroller.setFinalY(mMaxFlingY);
                    } else if(mScroller.getFinalY()<mMinFlingY){
                        mScroller.setFinalY(mMinFlingY);
                    }
                }
                mHandler.post(mScrollerRunnable);
                mTracker.recycle();
                mTracker=null;
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int computeDistanceToEndPoint(int remainder){
        if(Math.abs(remainder)>mItemHeight/2){
            if(mScrollOffsetY<0){
                return -mItemHeight-remainder;
            } else {
                return mItemHeight-remainder;
            }
        } else {
            return -remainder;
        }
    }

    public void setOnWheelChangeListener(OnWheelChangeListener<T> onWheelChangeListener) {
        mOnWheelChangeListener = onWheelChangeListener;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getSelectedItemPaint() {
        return mSelectedItemPaint;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public Paint getIndicatorPaint() {
        return mIndicatorPaint;
    }

    public List<T> getDataList() {
        return mDataList;
    }

    public void setDataList(List<T> dataList) {
        mDataList = dataList;
        if (dataList.size() == 0) {
            return;
        }
        Log.d("My","B11");
        computeTextSize();
        Log.d("My","B12");
        computeFlingLimitY();
        Log.d("My","B13");
        requestLayout();
        Log.d("My","B14");
        postInvalidate();
        Log.d("My","B15");
    }

    public int getTextColor() {
        return mTextColor;
    }

    /**
     * 一般列表的文本颜色
     * @param textColor 文本颜色
     */
    public void setTextColor(int textColor){
        if (mTextColor == textColor) {
            return;
        }
        mTextPaint.setColor(textColor);
        mTextColor = textColor;
        mLinearGradient.setStartColor(textColor);
        postInvalidate();
    }

    public int getTextSize() { return mTextSize; }

    /**
     * 一般列表的文本大小
     * @param textSize 文字大小
     */
    public void setTextSize(int textSize) {
        if (mTextSize == textSize) {
            return;
        }
        mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        computeTextSize();
        postInvalidate();
    }

    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    /**
     * 设置被选中时候的文本颜色
     * @param selectedItemTextColor 文本颜色
     */
    public void setSelectedItemTextColor(int selectedItemTextColor) {
        if (mSelectedItemTextColor == selectedItemTextColor) {
            return;
        }
        mSelectedItemPaint.setColor(selectedItemTextColor);
        mSelectedItemTextColor = selectedItemTextColor;
        mLinearGradient.setEndColor(selectedItemTextColor);
        postInvalidate();
    }

    public int getSelectedItemTextSize() {
        return mSelectedItemTextSize;
    }

    /**
     * 设置被选中时候的文本大小
     * @param selectedItemTextSize 文字大小
     */
    public void setSelectedItemTextSize(int selectedItemTextSize) {
        if (mSelectedItemTextSize == selectedItemTextSize) {
            return;
        }
        mSelectedItemPaint.setTextSize(selectedItemTextSize);
        mSelectedItemTextSize = selectedItemTextSize;
        computeTextSize();
        postInvalidate();
    }

    public String getItemMaximumWidthText() {
        return mItemMaximumWidthText;
    }

    /**
     * 设置输入的一段文字，用来测量 mTextMaxWidth
     * @param itemMaximumWidthText 文本内容
     */
    public void setItemMaximumWidthText(String itemMaximumWidthText) {
        mItemMaximumWidthText = itemMaximumWidthText;
        requestLayout();
        postInvalidate();
    }

    public int getHalfVisibleItemCount() {
        return mHalfVisibleItemCount;
    }

    /**
     * 设置显示数据量的个数的一半。
     * 为保证总显示个数为奇数,这里将总数拆分，总数为 mHalfVisibleItemCount * 2 + 1
     * @param halfVisibleItemCount 总数量的一半
     */
    public void setHalfVisibleItemCount(int halfVisibleItemCount) {
        if (mHalfVisibleItemCount == halfVisibleItemCount) {
            return;
        }
        mHalfVisibleItemCount = halfVisibleItemCount;
        requestLayout();
    }

    /**
     * 显示的个数等于上下两边Item数加中间
     * @return 可见Item数
     */
    public int getVisibleItemCount(){
        return mHalfVisibleItemCount*2+1;
    }

    public int getItemWidthSpace() {
        return mItemWidthSpace;
    }

    public void setItemWidthSpace(int itemWidthSpace) {
        if (mItemWidthSpace == itemWidthSpace) {
            return;
        }
        mItemWidthSpace = itemWidthSpace;
        requestLayout();
    }

    public int getItemHeightSpace() {
        return mItemHeightSpace;
    }

    /**
     * 设置两个Item之间的间隔
     * @param itemHeightSpace 间隔值
     */
    public void setItemHeightSpace(int itemHeightSpace) {
        if (mItemHeightSpace == itemHeightSpace) {
            return;
        }
        mItemHeightSpace = itemHeightSpace;
        requestLayout();
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * 设置当前选中的列表项,将滚动到所选位置
     * @param currentPosition 设置的当前位置
     */
    public void setCurrentPosition(int currentPosition) {
        setCurrentPosition(currentPosition, true);
    }

    /**
     * 设置当前选中的列表位置
     * @param currentPosition 设置的当前位置
     * @param smoothScroll 是否平滑滚动
     */
    public synchronized void setCurrentPosition(int currentPosition,boolean smoothScroll){
        if(currentPosition > mDataList.size()-1){
            currentPosition=mDataList.size()-1;
        }
        if(currentPosition < 0){
            currentPosition =0;
        }
        if(mCurrentPosition == currentPosition){
            return;
        }
        if(!mScroller.isFinished()){
            mScroller.abortAnimation();
        }

        //如果mItemHeight=0代表还没有绘制完成，这时平滑滚动没有意义
        if(smoothScroll && mItemHeight>0){
            mScroller.startScroll(0,mScrollOffsetY,0,(mCurrentPosition-currentPosition)*mItemHeight);
            /*
            mScroller.setFinalY(mScroller.getFinalY()+computeDistanceToEndPoint(mScroller.getFinalY() % mItemHeight));
             */
            int finalY=-currentPosition*mItemHeight;
            mScroller.setFinalY(finalY);
            mHandler.post(mScrollerRunnable);
        } else {
            mCurrentPosition=currentPosition;
            mScrollOffsetY=-mItemHeight*mCurrentPosition;
            postInvalidate();
            if(mOnWheelChangeListener!=null){
                mOnWheelChangeListener.onWheelSelected(mDataList.get(currentPosition),currentPosition);
            }
        }

    }

    public boolean isZoomInSelectedItem(){return mIsZoomInSelectedItem;}

    public void setZoomInSelectedItem(boolean zoomInSelectedItem) {
        if (mIsZoomInSelectedItem == zoomInSelectedItem) {
            return;
        }
        mIsZoomInSelectedItem = zoomInSelectedItem;
        postInvalidate();
    }

    public boolean isCyclic() {
        return mIsCyclic;
    }

    /**
     * 设置是否循环滚动。
     * @param cyclic 上下边界是否相邻
     */
    public void setCyclic(boolean cyclic) {
        if (mIsCyclic == cyclic) {
            return;
        }
        mIsCyclic = cyclic;
        computeFlingLimitY();
        requestLayout();
    }

    public int getMinimumVelocity() {
        return mMinimumVelocity;
    }

    /**
     * 设置最小滚动速度,如果实际速度小于此速度，将不会触发滚动。
     * @param minimumVelocity 最小速度
     */
    public void setMinimumVelocity(int minimumVelocity) {
        mMinimumVelocity = minimumVelocity;
    }

    public int getMaximumVelocity() {
        return mMaximumVelocity;
    }

    /**
     * 设置最大滚动的速度,实际滚动速度的上限
     * @param maximumVelocity 最大滚动速度
     */
    public void setMaximumVelocity(int maximumVelocity) {
        mMaximumVelocity = maximumVelocity;
    }

    public boolean isTextGradual() {
        return mIsTextGradual;
    }

    /**
     * 设置文字渐变，离中心越远越淡。
     * @param textGradual 是否渐变
     */
    public void setTextGradual(boolean textGradual) {
        if (mIsTextGradual == textGradual) {
            return;
        }
        mIsTextGradual = textGradual;
        postInvalidate();
    }

    public boolean isShowCurtain() {
        return mIsShowCurtain;
    }

    /**
     * 设置中心Item是否有幕布遮盖
     * @param showCurtain 是否有幕布
     */
    public void setShowCurtain(boolean showCurtain) {
        if (mIsShowCurtain == showCurtain) {
            return;
        }
        mIsShowCurtain = showCurtain;
        postInvalidate();
    }

    public int getCurtainColor() {
        return mCurtainColor;
    }

    /**
     * 设置幕布颜色
     * @param curtainColor 幕布颜色
     */
    public void setCurtainColor(int curtainColor) {
        if (mCurtainColor == curtainColor) {
            return;
        }
        mCurtainColor = curtainColor;
        postInvalidate();
    }

    public boolean isShowCurtainBorder() {
        return mIsShowCurtainBorder;
    }

    /**
     * 设置幕布是否显示边框
     * @param showCurtainBorder 是否有幕布边框
     */
    public void setShowCurtainBorder(boolean showCurtainBorder) {
        if (mIsShowCurtainBorder == showCurtainBorder) {
            return;
        }
        mIsShowCurtainBorder = showCurtainBorder;
        postInvalidate();
    }

    public int getCurtainBorderColor() {
        return mCurtainBorderColor;
    }

    /**
     * 幕布边框的颜色
     * @param curtainBorderColor 幕布边框颜色
     */
    public void setCurtainBorderColor(int curtainBorderColor) {
        if (mCurtainBorderColor == curtainBorderColor) {
            return;
        }
        mCurtainBorderColor = curtainBorderColor;
        postInvalidate();
    }

    public void setIndicatorText(String indicatorText) {
        mIndicatorText = indicatorText;
        postInvalidate();
    }

    public void setIndicatorTextColor(int indicatorTextColor) {
        mIndicatorTextColor = indicatorTextColor;
        mIndicatorPaint.setColor(mIndicatorTextColor);
        postInvalidate();
    }

    public void setIndicatorTextSize(int indicatorTextSize) {
        mIndicatorTextSize = indicatorTextSize;
        mIndicatorPaint.setTextSize(mIndicatorTextSize);
        postInvalidate();
    }

    /**
     * 设置数据集格式
     * @param dataFormat 格式
     */
    public void setDataFormat(Format dataFormat) {
        mDataFormat = dataFormat;
        postInvalidate();
    }

    public Format getDataFormat() {
        return mDataFormat;
    }

    /**
     * 修正坐标值，使滚轮在停止时Item位于正中
     * @param position 实际结束位置
     * @return 修正后的位置
     */
    private int fixItemPosition(int position){
        if(position<0){
            //将数据集限定在0`mDataList.size()-1之间
            position=mDataList.size()+(position % mDataList.size());

        }
        if(position >= mDataList.size()){
            position=position % mDataList.size();
        }
        return position;
    }

    public interface OnWheelChangeListener<T> {
        void onWheelSelected(T item,int position);
    }
}