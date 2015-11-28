/**
 * 用于实现布局添加控件时，自动换行
 */
package nercms.schedule.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
 
public class FixedGridLayout extends ViewGroup {

        private int mCellWidth;
        private int mCellHeight;
        private int mNumPerRow;
        private int mMarginTop;
        private int mMarginRight;
        private int mMarginBottom;
        private int mMarginLeft;
        public FixedGridLayout(Context context) {
                super(context);
                // TODO Auto-generated constructor stub
        }
 
        public FixedGridLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
            //初始化单元格宽和高
            mCellWidth = 100;
            mCellHeight = 100;
            mNumPerRow = 5;
            mMarginTop=5;
            mMarginRight=5;
            mMarginBottom=5;
            mMarginLeft=5;
        }        

        //设置单元格宽度
        public void setCellWidth(int w) {
    		mCellWidth = w;                
        //Call this when something has changed which has invalidated the layout of this view.
        //This will schedule a layout pass of the view tree
            requestLayout();
        }
        //设置单元格高度
        public void setCellHeight(int h) {
            mCellHeight = h;
            requestLayout();
        }

        //设置每行数目
        public void setNumPerRow(int num) {
        	mNumPerRow = num;
            requestLayout();
        }
        
        public void setMargin(int top,int right,int bottom,int left)
        {
        	mMarginTop=top;
        	mMarginRight = right;
        	mMarginBottom = bottom;
        	mMarginBottom = left;
        }
        //设置MarginTop
        public void setMarginTop(int num) {
        	mMarginTop=num;
            requestLayout();
        }
        //设置MarginRight
        public void setMarginRight(int num) {
        	mMarginRight = num;
            requestLayout();
        }
        //设置MarginBottom
        public void setMarginBottom(int num) {
        	mMarginBottom = num;
            requestLayout();
        }
        //设置MarginLeft
        public void setMarginLeft(int num) {
        	mMarginLeft = num;
            requestLayout();
        }

        @Override
	public void addView(View child) {
		// TODO Auto-generated method stub
		super.addView(child);
	}

	@Override
        protected void dispatchDraw(Canvas canvas) {
                // TODO Auto-generated method stub
//                //获取布局控件宽高
//                int width = getWidth();
//                int height = getHeight();
//                //创建画笔
//                Paint mPaint = new Paint();
//                //设置画笔的各个属性
//                mPaint.setColor(Color.BLUE);
//                mPaint.setStyle(Paint.Style.STROKE);
//                mPaint.setStrokeWidth(10);
//                mPaint.setAntiAlias(true);
//                //创建矩形框
//                Rect mRect = new Rect(0, 0, width, height);
//                //绘制边框
//                canvas.drawRect(mRect, mPaint);
                //最后必须调用父类的方法
                super.dispatchDraw(canvas);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                // TODO Auto-generated method stub
                //创建测量参数
                int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.AT_MOST);
                int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.AT_MOST);
                //记录ViewGroup中Child的总个数
                int count = getChildCount();
                //设置子空间Child的宽高
                for (int i = 0; i < count; i++) {
                    View childView = getChildAt(i);
                    childView.measure(cellWidthSpec, cellHeightSpec);
                }
                //设置容器控件所占区域大小
                //注意setMeasuredDimension和resolveSize的用法
                setMeasuredDimension(resolveSize((mCellWidth+(mMarginLeft+mMarginRight)) * count, widthMeasureSpec), resolveSize((mCellHeight+(mMarginLeft+mMarginRight)) * (count==0?0:(count-1)/mNumPerRow+1), heightMeasureSpec));
                //不需要调用父类的方法
                //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int cellWidth = mCellWidth;
                int cellHeight = mCellHeight;
                
                int count = getChildCount();
                //计算左右两边的空隙 即居中
                int marginleft=(r - l-cellWidth*mNumPerRow-(mMarginLeft+mMarginRight)*mNumPerRow)/2;
                int x = 0;
                int y = 0;
                int i = 0;

                for (int j = 0; j < count; j++) {
                    final View childView = getChildAt(j);
                    //获取子控件Child的宽高
                    int w = childView.getMeasuredWidth();
                    int h = childView.getMeasuredHeight();
                    
                    //如何只有一行则居中显示
                    if(count<mNumPerRow)
                    {
                    	marginleft=(r - l-cellWidth*count-(mMarginLeft+mMarginRight)*count)/2;
                    }else
                    {
                    	marginleft=(r - l-cellWidth*mNumPerRow-(mMarginLeft+mMarginRight)*mNumPerRow)/2;
                    }
                    //计算子控件的顶点坐标
                    int left = x + ((cellWidth - w)/2)+mMarginLeft+marginleft;
                    int top = y + ((cellHeight - h)/2)+mMarginTop;
                    //布局子控件
                    childView.layout(left, top, left + w, top + h);
                    
                    //每行mNumPerRow个元素
                    if(i == mNumPerRow-1) {
                            i = 0;
                            x = 0;
                            y += cellHeight+mMarginBottom;
                    } else {
                            i++;
                            x += cellWidth+mMarginRight;
                    }
                }
        }
}
