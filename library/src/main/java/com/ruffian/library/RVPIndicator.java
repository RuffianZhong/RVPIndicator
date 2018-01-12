package com.ruffian.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * RVPIndicator
 * <p>
 * ViewPager指示器 实现联动，自身滚动
 * 支持类型 : 下滑线，三角形，全背景，图片
 * <p>
 * https://github.com/RuffianZhong/RVPIndicator
 *
 * @author ZhongDaFeng
 */
public class RVPIndicator extends LinearLayout {

    // 指示器风格-图标
    private static final int STYLE_BITMAP = 0;
    // 指示器风格-下划线
    private static final int STYLE_LINE = 1;
    // 指示器风格-三角形
    private static final int STYLE_TRIANGLE = 2;

    /**
     * 系统默认:Tab数量
     */
    private static final int D_TAB_COUNT = 3;

    /**
     * 系统默认:文字正常时颜色
     */
    private static final int D_TEXT_COLOR_NORMAL = Color.parseColor("#000000");

    /**
     * 系统默认:文字选中时颜色
     */
    private static final int D_TEXT_COLOR_HIGHLIGHT = Color
            .parseColor("#FF0000");

    /**
     * 系统默认:指示器颜色
     */
    private static final int D_INDICATOR_COLOR = Color.parseColor("#f29b76");

    /**
     * tab上的内容
     */
    private List<String> mTabTitles = new ArrayList<String>();

    /**
     * 可见tab数量
     */
    private int mTabVisibleCount = D_TAB_COUNT;

    /**
     * 与之绑定的ViewPager
     */
    public ViewPager mViewPager;

    /**
     * 文字大小
     */
    private int mTextSize;

    /**
     * 文字正常时的颜色
     */
    private int mTextColorNormal = D_TEXT_COLOR_NORMAL;

    /**
     * 文字选中时的颜色
     */
    private int mTextColorHighlight = D_TEXT_COLOR_HIGHLIGHT;

    /**
     * 指示器正常时的颜色
     */
    private int mIndicatorColor = D_INDICATOR_COLOR;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 矩形
     */
    private Rect mRectF;

    /**
     * bitmap
     */
    private Bitmap mBitmap;

    /**
     * 指示器高
     */
    private int mIndicatorHeight = -1;

    /**
     * 指示器宽
     */
    private int mIndicatorWidth = -1;

    /**
     * 手指滑动时的偏移量
     */
    private float mTranslationX;

    /**
     * 指示器风格
     */
    private int mIndicatorStyle = STYLE_LINE;

    /**
     * 曲线path
     */
    private Path mPath;

    /**
     * viewPage初始下标
     */
    private int mPosition = 0;

    /**
     * 线性指示器左右padding，默认根据数量均分屏幕
     */
    private int mStyleLinePadding = 0;

    public RVPIndicator(Context context) {
        this(context, null);
    }

    public RVPIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER_VERTICAL);
        // 获得自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RVPIndicator);
        mTabVisibleCount = a.getInt(R.styleable.RVPIndicator_indicator_visible_count, D_TAB_COUNT);
        mTextColorNormal = a.getColor(R.styleable.RVPIndicator_text_color_normal, D_TEXT_COLOR_NORMAL);
        mTextColorHighlight = a.getColor(R.styleable.RVPIndicator_text_color_selected, D_TEXT_COLOR_HIGHLIGHT);
        mTextSize = a.getDimensionPixelSize(R.styleable.RVPIndicator_text_size, dip2px(context, 16));
        mIndicatorColor = a.getColor(R.styleable.RVPIndicator_indicator_color, D_INDICATOR_COLOR);
        mIndicatorStyle = a.getInt(R.styleable.RVPIndicator_indicator_style, STYLE_LINE);
        mStyleLinePadding = a.getDimensionPixelSize(R.styleable.RVPIndicator_style_line_padding, 0);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.RVPIndicator_indicator_height, -1);
        mIndicatorWidth = a.getDimensionPixelSize(R.styleable.RVPIndicator_indicator_width, -1);
        Drawable drawable = a.getDrawable(R.styleable.RVPIndicator_style_bitmap_src);

        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                mBitmap = ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof NinePatchDrawable) {
                // .9图处理
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                mBitmap = bitmap;
            }
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        }

        a.recycle();

        /**
         * 设置画笔
         */
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mIndicatorColor);
        mPaint.setStyle(Style.FILL);

    }

    /**
     * 初始化指示器
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        switch (mIndicatorStyle) {
            case STYLE_LINE:
                /**
                 * 下划线指示器:宽与item相等,高:0 没有指示器；未设置：item的1/10 ；具体值
                 */
                mIndicatorWidth = mIndicatorWidth < 0 ? (w / mTabVisibleCount) - (mStyleLinePadding * 2) : mIndicatorWidth;
                mIndicatorHeight = mIndicatorHeight < 0 ? h / 10 : mIndicatorHeight;
                mTranslationX = 0;
                mRectF = new Rect(mStyleLinePadding, 0, mIndicatorWidth + mStyleLinePadding, mIndicatorHeight);
                break;
            case STYLE_BITMAP:
                /**
                 * 方形/图标指示器:宽,高与item相等
                 */
                mIndicatorWidth = w / mTabVisibleCount;
                mIndicatorHeight = h;
                mTranslationX = 0;
                mRectF = new Rect(0, 0, mIndicatorWidth, mIndicatorHeight);
                break;
            case STYLE_TRIANGLE:
                /**
                 * 三角形指示器:宽与item(1/5)相等,高item的1/5
                 */
                mIndicatorWidth = mIndicatorWidth < 0 ? w / mTabVisibleCount / 5 : mIndicatorWidth;
                mIndicatorHeight = mIndicatorHeight < 0 ? h / 5 : mIndicatorHeight;
                mTranslationX = 0;
                break;
        }
        // 初始化tabItem
        setTabItem();

        // 高亮
        setSelectedTextView(mPosition);
    }

    /**
     * 绘制指示器(子view)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 保存画布
        canvas.save();
        switch (mIndicatorStyle) {
            case STYLE_BITMAP:
                canvas.translate(mTranslationX, 0);
                canvas.drawBitmap(mBitmap, null, mRectF, mPaint);
                break;
            case STYLE_LINE:
                canvas.translate(mTranslationX, getHeight() - mIndicatorHeight);
                canvas.drawRect(mRectF, mPaint);
                break;
            case STYLE_TRIANGLE:
                canvas.translate(mTranslationX, 0);
                // 笔锋圆滑度
                // mPaint.setPathEffect(new CornerPathEffect(10));
                mPath = new Path();
                int midOfTab = getWidth() / mTabVisibleCount / 2;
                mPath.moveTo(midOfTab, getHeight() - mIndicatorHeight);
                mPath.lineTo(midOfTab - mIndicatorWidth / 2, getHeight());
                mPath.lineTo(midOfTab + mIndicatorWidth / 2, getHeight());
                mPath.close();
                canvas.drawPath(mPath, mPaint);
                break;
        }
        // 恢复画布
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    /**
     * 设置titles
     *
     * @param titleList
     */
    public void setTitleList(List<String> titleList) {
        mTabTitles.clear();
        mTabTitles.addAll(titleList);
        setTabItem();
    }

    /**
     * 设置title
     *
     * @param title
     * @param pos
     */
    public void setTitle(String title, int pos) {
        int size = mTabTitles.size();
        if (pos >= size) {
            return;
        }
        for (int i = 0; i < size; i++) {
            if (pos == i) {
                mTabTitles.set(i, title);
            }
        }
        setTabItem();
    }

    /**
     * 设置关联viewPager
     *
     * @param viewPager
     * @param pos
     */
    @SuppressWarnings("deprecation")
    public void setViewPager(ViewPager viewPager, int pos) {
        this.mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 设置文本高亮
                setSelectedTextView(position);

                // 回调
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(position);
                }

                //回调
                if (mOnIndicatorSelected != null) {
                    mOnIndicatorSelected.setOnIndicatorSelected(position,
                            mTabTitles == null ? "" : mTabTitles.get(position));
                }

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // scoll
                onScoll(position, positionOffset);
                // 回调
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrolled(position,
                            positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 回调
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });

        // 设置当前页
        mViewPager.setCurrentItem(pos);
        mPosition = pos;

        //回调
        if (mOnIndicatorSelected != null) {
            mOnIndicatorSelected.setOnIndicatorSelected(pos,
                    mTabTitles == null ? "" : mTabTitles.get(pos));
        }

    }

    /**
     * 线性指示器左右padding
     *
     * @param styleLinePadding
     */
    public void setStyleLinePadding(int styleLinePadding) {
        this.mStyleLinePadding = dip2px(getContext(), styleLinePadding);
        /**
         * 重绘指示器
         */
        mIndicatorWidth = mIndicatorWidth < 0 ? (getWidth() / mTabVisibleCount) - (mStyleLinePadding * 2) : mIndicatorWidth;
        mIndicatorHeight = mIndicatorHeight < 0 ? getHeight() / 10 : mIndicatorHeight;
        mRectF = new Rect(mStyleLinePadding, 0, mIndicatorWidth + mStyleLinePadding, mIndicatorHeight);
        invalidate();
    }

    /**
     * 设置tabItem
     */
    private void setTabItem() {
        if (mTabTitles != null && mTabTitles.size() > 0) {
            if (this.getChildCount() != 0) {
                this.removeAllViews();
            }
            for (String title : mTabTitles) {
                addView(createTextView(title));
                measure(0, 0);
            }

            // 设置点击事件
            setItemClickEvent();
        }

    }

    /**
     * 设置点击事件
     */
    private void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     * 设置文本高亮
     *
     * @param position
     */
    private void setSelectedTextView(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                if (i == position) {
                    ((TextView) view).setTextColor(mTextColorHighlight);
                } else {
                    ((TextView) view).setTextColor(mTextColorNormal);
                }
            }
        }
    }

    /**
     * 滚动
     *
     * @param position
     * @param offset
     */
    private void onScoll(int position, float offset) {

        // 不断改变偏移量，invalidate
        mTranslationX = getWidth() / mTabVisibleCount * (position + offset);

        int tabWidth = getWidth() / mTabVisibleCount;

        // 容器滚动，当移动到倒数第二个的时候，开始滚动
        if (offset > 0 && position >= (mTabVisibleCount - 2)
                && getChildCount() > mTabVisibleCount
                && position < (getChildCount() - 2)) {
            if (mTabVisibleCount != 1) {

                int xValue = (position - (mTabVisibleCount - 2)) * tabWidth
                        + (int) (tabWidth * offset);
                this.scrollTo(xValue, 0);

            } else {
                // 为count为1时 的特殊处理
                this.scrollTo(position * tabWidth + (int) (tabWidth * offset),
                        0);
            }
        }

        invalidate();
    }

    /**
     * 创建标题view
     *
     * @param text
     * @return
     */
    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.width = getWidth() / mTabVisibleCount;
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(mTextColorNormal);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        tv.setLayoutParams(lp);
        return tv;
    }


    public interface PageChangeListener {

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    }

    public interface OnIndicatorSelected {
        void setOnIndicatorSelected(int position, String title);
    }

    private PageChangeListener onPageChangeListener;

    private OnIndicatorSelected mOnIndicatorSelected;

    public void setOnPageChangeListener(PageChangeListener pageChangeListener) {
        this.onPageChangeListener = pageChangeListener;
    }

    public void setOnIndicatorSelected(OnIndicatorSelected mOnIndicatorSelected) {
        this.mOnIndicatorSelected = mOnIndicatorSelected;
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
