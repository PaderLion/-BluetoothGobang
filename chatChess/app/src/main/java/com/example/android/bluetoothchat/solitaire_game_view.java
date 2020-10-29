package com.example.android.bluetoothchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;





public class solitaire_game_view extends View
{
    public int mPanelWidth;
    public float mLineHeight;
    public int MAX_LINE=10;
    public int Max_Count=5;

    public Paint mPaint =new Paint();

    public Bitmap mw;
    public Bitmap mb;

    public float cheessHeight=3 * 1.0f / 4;

    //white first or white turn
    public boolean miswhite=true;
    public List<Point> mwa=new ArrayList<>();
    public List<Point> mba=new ArrayList<>();

    public boolean misGameOver;
    public boolean misWhiteWin;

    public solitaire_game_view(Context context , AttributeSet attrs) {
        super(context , attrs);
        //setBackgroundResource(R.drawable.background01);

        setBackgroundColor(0x88000000);
        init();
    }

    public void init(){
       // mPaint.setColor(0xFF000000);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

        mb= BitmapFactory.decodeResource(getResources(),R.drawable.black);
        mw= BitmapFactory.decodeResource(getResources(),R.drawable.white);
    }


    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
    {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width,width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth=w;
        mLineHeight=mPanelWidth*1.0f/MAX_LINE;

        int cheeseWidth=(int)(mLineHeight * cheessHeight);

        mw=Bitmap.createScaledBitmap(mw,cheeseWidth,cheeseWidth,false);
        mb=Bitmap.createScaledBitmap(mb,cheeseWidth,cheeseWidth,false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(misGameOver)return false;

        int action=event.getAction();
        if (action == MotionEvent.ACTION_UP){

            int x=(int)event.getX();
            int y=(int)event.getY();

            Point p=getValidPoint(x,y);

            if(mwa.contains(p)||mba.contains(p))
            {
                return false;
            }

            if(miswhite)
            {
                mwa.add(p);
            }
            else
            {
                mba.add(p);
            }
            invalidate();

            miswhite=!miswhite;
        }

        return true;
    }

    public Point getValidPoint(int x,int y)
    {
        return new Point((int)(x / mLineHeight),(int)(y / mLineHeight));
    }
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        drawB(canvas);

        drawCheese(canvas);
        checkGameOver();
    }

    public void checkGameOver() {
        boolean whitewin =checkFiveInLine(mwa);
        boolean blackwin =checkFiveInLine(mba);

        if(whitewin || blackwin){
            misGameOver=true;
            misWhiteWin=whitewin;

            String text=misWhiteWin ? "白棋勝利" :"黑棋勝利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkFiveInLine(List<Point>points) {
        for(Point p : points){
            int x=p.x;
            int y=p.y;
            boolean win =checkHorizon(x,y,points);
            if(win)return true;

            win=checkVertical(x,y,points);
            if(win)return true;

            win=checkVerticalLeft(x,y,points);
            if(win)return true;

            win=checkVerticalRight(x,y,points);
            if(win)return true;

        }
        return false;
    }

    public boolean checkHorizon(int x,int y,List<Point>points){
        int count =1;
        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x-i,y))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;

        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x+i,y))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;
        return false;
    }

    public boolean checkVertical(int x,int y,List<Point>points){
        int count =1;
        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x,y-i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;

        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x,y+i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;
        return false;
    }

    public boolean checkVerticalLeft(int x,int y,List<Point>points){
        int count =1;
        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x-i,y+i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;

        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x+i,y-i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;
        return false;
    }

    public boolean checkVerticalRight(int x,int y,List<Point>points){
        int count =1;
        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x-i,y-i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;

        for(int i=1;i<Max_Count;i++){
            if(points.contains(new Point(x+i,y+i))){
                count++;
            }
            else
            {
                break;
            }
        }

        if(count == Max_Count)return true;
        return false;
    }


    public void drawCheese(Canvas canvas) {
        for(int i=0,n=mwa.size();i<n;i++){
            Point wp=mwa.get(i);
            canvas.drawBitmap(mw,(wp.x+(1-cheessHeight)/2)*mLineHeight,(wp.y+(1-cheessHeight)/2)*mLineHeight,null);

        }

        for(int i=0,n=mba.size();i<n;i++){
            Point bp=mba.get(i);
            canvas.drawBitmap(mb,(bp.x+(1-cheessHeight)/2)*mLineHeight,(bp.y+(1-cheessHeight)/2)*mLineHeight,null);

        }

    }

    public void drawB(Canvas canvas)
    {
        int w=mPanelWidth;
        float lineHeight =mLineHeight;

        for(int i=0;i<MAX_LINE;i++)
        {
            int startX=(int)(lineHeight/2);
            int endX= (int)(w-lineHeight/2);
            int y=(int)((0.5 + i)*lineHeight);
            canvas.drawLine(startX,y,endX,y,mPaint);
            canvas.drawLine(y,startX,y,endX,mPaint);
        }
    }

    public void start(){
        mwa.clear();
        mba.clear();
        misGameOver=false;
        misWhiteWin=false;
        invalidate();
    }


}