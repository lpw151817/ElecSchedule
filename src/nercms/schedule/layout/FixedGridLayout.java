/**
 * ����ʵ�ֲ�����ӿؼ�ʱ���Զ�����
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
            //��ʼ����Ԫ���͸�
            mCellWidth = 100;
            mCellHeight = 100;
            mNumPerRow = 5;
            mMarginTop=5;
            mMarginRight=5;
            mMarginBottom=5;
            mMarginLeft=5;
        }        

        //���õ�Ԫ����
        public void setCellWidth(int w) {
    		mCellWidth = w;                
        //Call this when something has changed which has invalidated the layout of this view.
        //This will schedule a layout pass of the view tree
            requestLayout();
        }
        //���õ�Ԫ��߶�
        public void setCellHeight(int h) {
            mCellHeight = h;
            requestLayout();
        }

        //����ÿ����Ŀ
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
        //����MarginTop
        public void setMarginTop(int num) {
        	mMarginTop=num;
            requestLayout();
        }
        //����MarginRight
        public void setMarginRight(int num) {
        	mMarginRight = num;
            requestLayout();
        }
        //����MarginBottom
        public void setMarginBottom(int num) {
        	mMarginBottom = num;
            requestLayout();
        }
        //����MarginLeft
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
//                //��ȡ���ֿؼ����
//                int width = getWidth();
//                int height = getHeight();
//                //��������
//                Paint mPaint = new Paint();
//                //���û��ʵĸ�������
//                mPaint.setColor(Color.BLUE);
//                mPaint.setStyle(Paint.Style.STROKE);
//                mPaint.setStrokeWidth(10);
//                mPaint.setAntiAlias(true);
//                //�������ο�
//                Rect mRect = new Rect(0, 0, width, height);
//                //���Ʊ߿�
//                canvas.drawRect(mRect, mPaint);
                //��������ø���ķ���
                super.dispatchDraw(canvas);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                // TODO Auto-generated method stub
                //������������
                int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.AT_MOST);
                int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.AT_MOST);
                //��¼ViewGroup��Child���ܸ���
                int count = getChildCount();
                //�����ӿռ�Child�Ŀ��
                for (int i = 0; i < count; i++) {
                    View childView = getChildAt(i);
                    childView.measure(cellWidthSpec, cellHeightSpec);
                }
                //���������ؼ���ռ�����С
                //ע��setMeasuredDimension��resolveSize���÷�
                setMeasuredDimension(resolveSize((mCellWidth+(mMarginLeft+mMarginRight)) * count, widthMeasureSpec), resolveSize((mCellHeight+(mMarginLeft+mMarginRight)) * (count==0?0:(count-1)/mNumPerRow+1), heightMeasureSpec));
                //����Ҫ���ø���ķ���
                //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int cellWidth = mCellWidth;
                int cellHeight = mCellHeight;
                
                int count = getChildCount();
                //�����������ߵĿ�϶ ������
                int marginleft=(r - l-cellWidth*mNumPerRow-(mMarginLeft+mMarginRight)*mNumPerRow)/2;
                int x = 0;
                int y = 0;
                int i = 0;

                for (int j = 0; j < count; j++) {
                    final View childView = getChildAt(j);
                    //��ȡ�ӿؼ�Child�Ŀ��
                    int w = childView.getMeasuredWidth();
                    int h = childView.getMeasuredHeight();
                    
                    //���ֻ��һ���������ʾ
                    if(count<mNumPerRow)
                    {
                    	marginleft=(r - l-cellWidth*count-(mMarginLeft+mMarginRight)*count)/2;
                    }else
                    {
                    	marginleft=(r - l-cellWidth*mNumPerRow-(mMarginLeft+mMarginRight)*mNumPerRow)/2;
                    }
                    //�����ӿؼ��Ķ�������
                    int left = x + ((cellWidth - w)/2)+mMarginLeft+marginleft;
                    int top = y + ((cellHeight - h)/2)+mMarginTop;
                    //�����ӿؼ�
                    childView.layout(left, top, left + w, top + h);
                    
                    //ÿ��mNumPerRow��Ԫ��
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
