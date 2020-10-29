package com.example.android.bluetoothchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    BluetoothChatService  mChatService;
    String TAG="GameView";
    int width;
    int cheeseWidth;
    public int mPanelWidth;
    public float mLineHeight;
    public int MAX_LINE=10;
    public int Max_Count=5;
    public int returnchessX;
    public int returnchessY;
    public int getReturnchessX;
    public int getReturnchessY;
    int white=0;
    int black=0;
    public Paint mPaint =new Paint();
    public Bitmap mw;
    public Bitmap mb;
    public float cheessHeight=3 * 1.0f / 4;
    //white first or white turn
    public boolean miswhite=true;
    public boolean Myturn=true;
    public boolean aaaaaaaa=true;
    public boolean returnchess=true;
    public List<Point> mwa=new ArrayList<>();
    public List<Point> mba=new ArrayList<>();
    public boolean misGameOver;
    public boolean misWhiteWin;
    ChessActivity mChess;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mChess=(ChessActivity) context;

        setBackgroundResource(R.drawable.background01);
       // setBackgroundColor(0x55000000);

        init();

    }

    public void init(){

        mPaint.setColor(0xFF000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        white=0;
        black=0;

        mb= BitmapFactory.decodeResource(getResources(),R.drawable.black);
        mw= BitmapFactory.decodeResource(getResources(),R.drawable.white);
        invalidate();
    }
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
    {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width,width);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth=w;
        mLineHeight=mPanelWidth*1.0f/MAX_LINE;

        cheeseWidth=(int)(mLineHeight * cheessHeight);

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
            returnchessX=x;
            returnchessY=y;
            if(Myturn){
                Point p=getValidPoint(x,y);
                if(mwa.contains(p)||mba.contains(p))
                {
                    return false;
                }
                putChess(x,y);
                if(miswhite)
                {
                    mwa.add(p);
                }
                else
                {
                    mba.add(p);
                }
                Myturn=false;
                miswhite=!miswhite;
                returnchess=true;
            }
            invalidate();
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

        invalidate();
        checkGameOver();
    }


    public void checkGameOver() {
        boolean whitewin =checkFiveInLine(mwa);
        boolean blackwin =checkFiveInLine(mba);

        if(aaaaaaaa){
            if(whitewin || blackwin){
                misGameOver=true;
                misWhiteWin=whitewin;

                String text=misWhiteWin ? "白棋勝利" :"黑棋勝利";
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                aaaaaaaa=false;
            }
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

            canvas.drawBitmap(mw,(wp.x+(1-cheessHeight)/2)*mLineHeight, (wp.y+(1-cheessHeight)/2)*mLineHeight,null);

        }

        for(int i=0,n=mba.size();i<n;i++){
            Point bp=mba.get(i);
            canvas.drawBitmap(mb,(bp.x+(1-cheessHeight)/2)*mLineHeight, (bp.y+(1-cheessHeight)/2)*mLineHeight,null);

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

    public interface IWrite{
      void onWrite(String str);
    }

    public void setCallBack(BluetoothChatService mChat) {
        this.mChatService = mChat;
    }


    //下棋子,並發送給對方
    private void putChess(int x, int y){
        String command = "";
        String temp ="xy" +";" +  x + ";" + y + ";";
        mChatService.onWrite(temp);
    }


    //接收對方傳送信息
    public void getCommand(String command) {
        if ("msg;".equals(command.substring(0, 4))) {
            final String finalCommand = command.substring(4);
            Toast.makeText(getContext(), finalCommand, Toast.LENGTH_SHORT).show();
        } else if ("return".equals(command)) {
            //Toast.makeText(getContext(), "對方悔棋!", Toast.LENGTH_SHORT).show();
            //Log.e("white "+white+" black "+black, "getCommand: " );
            returnUp();
            Myturn=false;
            /*if(white <2) {
                if(black<2)
                    Log.e("whiteeeeeeee   "+white+"  "+black, "getCommand: " );
                    Myturn=false;
            }
            else if (black < 2) {
                if (white < 2) {
                    Log.e("blackkkkkkkk   "+white+"  "+black,"getCommand: " );
                    Myturn = false;
                }
            }*/
        } else if ("restart".equals(command)) {
            Toast.makeText(getContext(), "對方重玩遊戲!", Toast.LENGTH_SHORT).show();
            restartGame();
        } else {
            String[] data = command.split(";");
            int x = Integer.parseInt(data[1]);
            int y = Integer.parseInt(data[2]);
            getReturnchessX= Integer.parseInt(data[1]);
            getReturnchessY = Integer.parseInt(data[2]);
            Point p=getValidPoint(x,y);

            if(miswhite)
            {
                mwa.add(p);
            }
            else
            {
                mba.add(p);
            }

            miswhite=!miswhite;

            invalidate();

            Canvas canvas=new Canvas();
            for(int i=0,n=mwa.size();i<n;i++){
                Point wp=mwa.get(i);
                canvas.drawBitmap(mw,(wp.x+(1-cheessHeight)/2)*mLineHeight,(wp.y+(1-cheessHeight)/2)*mLineHeight,null);

            }

            for(int i=0,n=mba.size();i<n;i++){
                Point bp=mba.get(i);
                canvas.drawBitmap(mb,(bp.x+(1-cheessHeight)/2)*mLineHeight,(bp.y+(1-cheessHeight)/2)*mLineHeight,null);

            }



            invalidate();
            Myturn=true;
        }
    }

    public void restartGame(){
        miswhite=true;
        Myturn=true;
        aaaaaaaa=true;
        misGameOver=false;
        mwa=new ArrayList<>();
        mba=new ArrayList<>();
        mChess.sum=0;
        init();
        setMeasuredDimension(width,width);
        mw=Bitmap.createScaledBitmap(mw,cheeseWidth,cheeseWidth,false);
        mb=Bitmap.createScaledBitmap(mb,cheeseWidth,cheeseWidth,false);
    }

    public void returnUp() {
            Point ap = getValidPoint(returnchessX, returnchessY);
            Point bp = getValidPoint(getReturnchessX, getReturnchessY);
            if (!miswhite) {
                if(returnchess){
                    Log.e("returnchess "+ returnchess+"  Mytrun "+Myturn, "returnUp: " );
                    if (white < 1) {
                        mwa.remove(ap);
                        mwa.remove(bp);
                        white++;
                        Myturn = true;
                        miswhite = !miswhite;
                        returnchess=false;
                        Toast.makeText(getContext(), "白棋悔棋!", Toast.LENGTH_SHORT).show();
                    } else {
                        white++;
                        Toast.makeText(getContext(), "白棋只能悔一次棋!", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (miswhite) {
                if(returnchess){
                    Log.e("returnchess "+ returnchess+"  Mytrun "+Myturn, "returnUp: " );
                    if (black < 1) {
                        mba.remove(bp);
                        mba.remove(ap);
                        black++;
                        Myturn = true;
                        miswhite = !miswhite;
                        returnchess=false;
                        Toast.makeText(getContext(), "黑棋悔棋!", Toast.LENGTH_SHORT).show();
                    } else{
                        black++;
                        Toast.makeText(getContext(), "黑棋只能悔一次棋!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            invalidate();
    }

}
