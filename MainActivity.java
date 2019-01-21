package com.example.a10008881.dodgegame;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    public float xPosition,xEnemy,xAcceleration,xVelocity = 0.0f;
    public float yPosition, yEnemy, yAcceleration,yVelocity = 0.0f;
    public float xmax,ymax;
    private Bitmap mBitmap;
    private Bitmap mWood;
    private Bitmap mEnemy;
    private Bitmap mHit;
    public float frameTime = 0.666f;
    private SensorManager sensorManager = null;
    public float dX,dY;
    double distance;
    Boolean collision = false;
    int score=0;
    public static int timer=0;
    public static int gametimer=0;
    static boolean touch = false;
    SoundPool soundPool;
    int chimesID,chordID,dingID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mySensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_GAME);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Display screenDisplay = getWindowManager().getDefaultDisplay();
        xmax = (float)screenDisplay.getWidth() - 50;
        ymax = (float)screenDisplay.getHeight() - 50;

        xEnemy = (int)(Math.random()*1000);

        setContentView(gameSurface);

        SoundPool.Builder builder = new SoundPool.Builder(); //creates builder
        builder.setMaxStreams(3); // how many sound effects can be played at the same time?
        soundPool = builder.build(); //create a SoundPool using the builder

        //https://developer.android.com/reference/android/media/SoundPool.html#load(android.content.Context, int, int)
        chimesID = soundPool.load(this,R.raw.chimes,1);
        chordID = soundPool.load(this,R.raw.chord,1);
        dingID = soundPool.load(this,R.raw.ding,1);




    }

    private void updateBall() {


        //Calculate new speed
        xVelocity += (xAcceleration * frameTime);
        yVelocity += (yAcceleration * frameTime);

        //Calc distance travelled in that time
        float xS = (xVelocity/2)*frameTime;
        float yS = (yVelocity/2)*frameTime;

        //Add to position negative due to sensor
        //readings being opposite to what we want!
        xPosition -= xS;
        yPosition -= yS;

        Log.d("x pos",""+xPosition);
        Log.d("y pos",""+yPosition);


        if (xPosition > xmax) {
            xPosition = xmax;
        } else if (xPosition < 0) {
            xPosition = 0;
        }
        if (yPosition > ymax) {
            yPosition = ymax;
        } else if (yPosition < 0) {
            yPosition = 0;
        }


        Log.d("x vel",""+xVelocity);
        Log.d("y vel",""+yVelocity);

    }

    private void updateEnemy(){

        if (yEnemy>=1500){
            xEnemy = (int)(Math.random()*1000);
            yEnemy=0;
            score++;
            soundPool.play(dingID,1,0,1,0,1);
        }

        if (touch==true)
            yEnemy+=10;
        else
        yEnemy+=5;



    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("Test","I am in on sensor changed");

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Set sensor values as acceleration
            xAcceleration = event.values[0];
            yAcceleration = event.values[1];
            updateBall();
            updateEnemy();

            dX = xPosition - xEnemy;
            dY = 1500 - yEnemy;
            distance = Math.sqrt(Math.pow(dX,2) + Math.pow(dY,2));

            if (distance<=50) {

                collision = true;
                xEnemy = (int)(Math.random()*1000);
                yEnemy=0;

                Log.d("TAG","hit");

                soundPool.play(chimesID,1,1,1,0,1);
            }


            Log.d("x acc",""+xAcceleration);
            Log.d("y acc",""+yAcceleration);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause(){
       // gameSurface.pause();
        //super.onPause();
    }

    @Override
    protected void onStop() {

        sensorManager.unregisterListener(this);
        super.onStop();
        Log.d("End","Game over");


    }

    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);



    }

    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends View {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap myImage;
        Bitmap myEnemy;
        Bitmap myHit;
        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.doodle);
            myEnemy = BitmapFactory.decodeResource(getResources(),R.drawable.enemy);
            myHit = BitmapFactory.decodeResource(getResources(),R.drawable.doodlehit);
            final int dstWidth=180;
            final int dstHeight=180;

            mBitmap = Bitmap.createScaledBitmap(myImage, dstWidth, dstHeight, true);
            mEnemy = Bitmap.createScaledBitmap(myEnemy, dstWidth,dstHeight,true);
            mHit = Bitmap.createScaledBitmap(myHit,dstWidth,dstHeight,true);
            mWood = BitmapFactory.decodeResource(getResources(), R.drawable.bg);


           // Point sizeOfScreen = new Point();
            //screenDisplay.getSize(sizeOfScreen);
            //screenWidth=sizeOfScreen.x;
            //screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(70);



        }

        public boolean onTouchEvent(MotionEvent event){
            int action = event.getActionMasked();
            Log.d("Touch",String.valueOf(action));

            if (touch==false)
                touch=true;
            else
                touch=false;

            return super.onTouchEvent(event);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final Bitmap bitmap = mBitmap;
            final Bitmap monster = mEnemy;
            final Bitmap doodleHit = mHit;
            gametimer++;

            canvas.drawBitmap(mWood,0,0,null);

            canvas.drawText("Score: "+score,50,200,paintProperty);


            if (collision==true && timer<500){
                canvas.drawBitmap(doodleHit,xPosition,1500,null);
                timer++;
            }
            else
            {
                canvas.drawBitmap(bitmap,xPosition,1500,null);
                timer=0;
                collision=false;
            }


            canvas.drawBitmap(monster,xEnemy,yEnemy,null);

            invalidate();

            if (gametimer>6000) {
               // for(int x=0;x<10000;x++)
                    canvas.drawText("Game Over",50,300,paintProperty);
                soundPool.play(chordID,0,1,1,0,1);
                finish();
             }
        }


    }//GameSurface
}//Activity
