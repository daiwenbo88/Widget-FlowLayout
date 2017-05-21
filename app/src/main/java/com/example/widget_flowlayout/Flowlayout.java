package com.example.widget_flowlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * author : daiwenbo
 * e-mail : daiwwenb@163.com
 * date   : 2017/5/21
 * description   : xxxx描述
 */

public class Flowlayout extends ViewGroup{
    /**用于存放每一行的子View集合*/
    private List<List<View>> mViewLinesList=new ArrayList<>();
    /**用于存放每一行的高度*/
    private List<Integer> mLineHeights=new ArrayList<>();
    int index;
    public Flowlayout(Context context) {
        this(context,null);
    }

    public Flowlayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Flowlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Flowlayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 获取布局中的margin属性值
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }


    /**
     * 测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthmMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWith = 0;
        int measuredHeight = 0;
        int curLineW=0;
        int curLineH=0;

        if(widthmMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.EXACTLY){
            measuredHeight=heightSize;
            measuredWith=widthsize;
        }else {
            int childWidth;
            int childHeight;
            //获取所有的子View
            int childCount = getChildCount();
            //创建一个集合用于存放每一行的子View
            List<View> viewList=new ArrayList<>();
            //遍历
            for(int i=0;i<childCount;i++){
                View childAt = getChildAt(i);
                //遍历 测量子view
                measureChild(childAt,widthMeasureSpec,heightMeasureSpec);
                //获取子View的margin属性
                MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                //获取子view的最终宽度
                childWidth=layoutParams.rightMargin+layoutParams.leftMargin+childAt.getMeasuredWidth();
                //获取子view的最终高度
                childHeight=layoutParams.topMargin+layoutParams.bottomMargin+childAt.getMeasuredHeight();
                //子View累计宽度是否大于父容器宽度
                if(curLineW+childWidth>widthsize){
                    //记录当前最大宽度
                    measuredWith=Math.max(measuredWith,curLineW);
                    //高度累加
                    measuredHeight+=curLineH;
                    //将该View添加到List集合
                    mViewLinesList.add(viewList);
                    //行高添加至总行高
                    mLineHeights.add(curLineH);
                    //记录新一行的信息
                    curLineW=childWidth;
                    curLineH=childHeight;
                    //新建一行的ViewList 添加一行新view
                    viewList=new ArrayList<>();
                    viewList.add(childAt);
                }else {
                    //累加宽度
                    curLineW+=childWidth;
                    //高度比较 获取最大的高度
                    curLineH=Math.max(curLineH,childHeight);
                    //添加到该行集合
                    viewList.add(childAt);
                }
                //如果是最后一个view的时候
                if(i==childCount-1){
                    measuredWith=Math.max(measuredWith,curLineW);
                    measuredHeight+=curLineH;
                    //将该View添加到List集合
                    mViewLinesList.add(viewList);
                    //行高添加至总行高
                    mLineHeights.add(curLineH);
                }
            }
        }
        setMeasuredDimension(measuredWith,measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left,top,right,bottom;
        int curTop=0;
        int curLeft=0;
        int size = mViewLinesList.size();
        //遍历每一行的view集合
        for(int i=0;i<size;i++){
            List<View> views = mViewLinesList.get(i);
            int lineViewSize=views.size();
            //遍历每一行的所有view
            for(int j=0;j<lineViewSize;j++){
                View view = views.get(j);
                //计算每个View四个点坐标
                MarginLayoutParams layoutParams= (MarginLayoutParams) view.getLayoutParams();
                left=curLeft+layoutParams.leftMargin;
                top=curTop+layoutParams.topMargin;
                right=left+view.getMeasuredWidth();
                bottom=top+view.getMeasuredHeight();
                //布局
                view.layout(left,top,right,bottom);
                //宽度累加 作为下一个View的起始点
                curLeft+=view.getMeasuredWidth()+layoutParams.leftMargin+layoutParams.rightMargin;
            }
            //换行 宽度数据重新开始
            curLeft=0;
            //高度累加一次
            curTop+=mLineHeights.get(i);
        }
        mViewLinesList.clear();
        mLineHeights.clear();
    }

    interface OnItemClickListener{
        void onItemClick(String text);
    }
    public void setOnItemClickListener(final OnItemClickListener onItemClickListener){
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            final TextView childAt = (TextView) getChildAt(i);
            final int finalI = i;
            childAt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(index!=finalI){
                        //修改点击以前的item(复原)
                        TextView view  = (TextView) getChildAt(index);
                        Drawable drawable = ContextCompat.getDrawable(getContext(),R.drawable.text_backgroundcolor);
                        changeContent(drawable, view,Color.BLACK);
                        //修该点击的item
                        drawable = ContextCompat.getDrawable(getContext(),R.drawable.selector_text);
                        changeContent(drawable, childAt,Color.WHITE);
                        index= finalI;
                    }
                    onItemClickListener.onItemClick(childAt.getText().toString());
                }
            });

        }
    }

    private void changeContent(Drawable drawable, TextView childAt,int textColor) {
        childAt.setBackground(drawable);
        childAt.setTextColor(textColor);
    }
}
