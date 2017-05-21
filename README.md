# Widget-FlowLayout
# Flowlayout流式布局的简单实现
#####前言
关于Flowlayout流式布局 github上随便一搜索就一大堆 而且Google也开源了一个[FlexboxLayout](https://github.com/google/flexbox-layout)布局非常强大 这里只是教大家如何实现的最基本的自定义 剩余的那些花俏就看大家天马行空的扩展就好了 同时懂的大神基本可以路过了 
#####效果
![](http://on96fbw9r.bkt.clouddn.com/fl01.png)

分析:
    1. 这是一个ViewGroup
    2. 当父容器宽度放不下子view的时候 自动换行
    3. 除了子View的宽高还要考虑子View的Margin属性
    
备注：**整个过程涉及的知识点 主要就是View的onMeasure和onLayout 基础偏弱的同学可能需要另外花点功夫学习下** 
#####自定义步骤

1.先自定义一个ViewGroup

```
public class Flowlayout extends ViewGroup{
```
2.获取子View的Margin属性(这个在ViewGroup中非常方便 重写generateLayoutParams便是)

```
/**
* 获取布局中的margin属性值
* @param attrs
* @return
*/
@Override
public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }
```
3.来到我们的重点之一 **针对子View的测量** 关于View的测量相关方面的知识 这里不做多介绍不明白的同学 可以去Google一下 同时我也把注释尽量写明白

```
    /**用于存放每一行的子View集合*/
    private List<List<View>> mViewLinesList=new ArrayList<>();
    /**用于存放每一行的高度*/
    private List<Integer> mLineHeights=new ArrayList<>();
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
                //如果是最后一个view的时候（特别针对最后一行view只有一两个的 因为他们的宽度是不可能超过父容器的宽度的）
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
```
4.重点之二 **View的布局** 上面一步是把View的宽高测量出来了 这个一步将确定子View在父容器中位置 重写onLayout 进行布局

```
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
```
5.到这里基本就完成了 使用在布局中看看效果

```
<com.example.widget_flowlayout.Flowlayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/flowlayout"
        android:background="#ededed">
        <TextView
            style="@style/flow_style"
            android:text="头条"/>
        <TextView
            style="@style/flow_style"
            android:text="态度公开课"/>
        <TextView
            style="@style/flow_style"
            android:text="房产"/>
        <TextView
            style="@style/flow_style"
            android:text="人工智能"/>
        <TextView
            style="@style/flow_style"
            android:text="军事"/>
        <TextView
            style="@style/flow_style"
            android:text="段子"/>
        <TextView
            style="@style/flow_style"
            android:text="营销态度"/>
        <TextView
            style="@style/flow_style"
            android:text="社会"/>
        <TextView
            style="@style/flow_style"
            android:text="航空"/>
       ...................
</com.example.widget_flowlayout.Flowlayout>     
```
6.扩展点击事件 其他的扩展看你们思想发挥了

```
    int index;//保存点击过的index
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
                        //保存点击过的index
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
```
7.最后效果
    
![](http://on96fbw9r.bkt.clouddn.com/fl.gif)


  


