package com.example.temp_test;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Класс реализовывает парсинг рисунка
 * Граница задается параметром COLOR_BORDER_MAX и может быть больше его
 * Теоретически, скорость вращения (градусов в секунду) равна:
 * 120 об/мин = 2 об/сек = 720 град/сек.
 * 720 град = 16000 сэмплов.
 * 1 сэмпл = 720/16000 = 0,045 град
 */
public class MyImageParser extends Thread{

    public final static int COLOR_BORDER_MAX = 60;//(100-79) * 256/100;

    final static float DEGREES_PER_STEP = 0.07f;//0.045f;
    final static int RADIUS_MAX_ALLOWED = 1500-20;

    final static int DATA_LENGTH_MAX = 300000;


    private Bitmap bitmap;


    private int currentRadius;
    private float currentAngle;
    //Current x and y coords while parsing
    private Point currentPoint;
    private Point center;


    Resources resources;
    int res;

    Activity activity;


    void ShowToast(final String text)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = activity.getApplicationContext();
                //CharSequence text = "Parsing started!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    MyImageParser(Resources resources, int res, Activity toastActivity)
    {
        this.resources = resources;
        this.res = res;
        activity = toastActivity;
    }

    void SaveBitmap(Bitmap bmp)
    {
        try {
            //FileOutputStream out = new FileOutputStream("/mnt/sdcard/test.png");
            File myFile = new File(Environment.getExternalStorageDirectory() + "/mysdfile.png");
            myFile.createNewFile();
            FileOutputStream out = new FileOutputStream(myFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AudioTrack ConvertArrayToAudioTrack(ByteArrayBuffer dataIn) {

        AudioTrack wave = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                                        AudioFormat.ENCODING_PCM_8BIT, dataIn.length(), AudioTrack.MODE_STREAM);

        return wave;
    }


    private Point GetXYFromRadial(int radius, float angle) {
        //convertation routine here

        currentPoint.x = center.x + (int) (radius * Math.cos(Math.toRadians(angle)));
        currentPoint.y = center.y + (int) (radius * Math.sin(Math.toRadians(angle)));

        return currentPoint;
    }

    private Point GetNextPoint() {
        // point calculation here

        /*
         * First, add STEP to angle;
         * Then, check if we're over border, and increase radius while we're on the line;
         */

        Point previousPoint = new Point(currentPoint);

        currentAngle += DEGREES_PER_STEP;


        currentPoint = GetXYFromRadial(currentRadius, currentAngle);
        if(currentPoint.equals(previousPoint))
        {
            return currentPoint;
        }

        if(true)
        bitmap.setPixel(GetXYFromRadial(currentRadius, currentAngle-DEGREES_PER_STEP).x, GetXYFromRadial(currentRadius, currentAngle-DEGREES_PER_STEP).y, 0xff000000);


        int outPixel = GetPixelDensity(GetXYFromRadial(currentRadius+2, currentAngle));
        int inPixel = GetPixelDensity(GetXYFromRadial(currentRadius-2, currentAngle));

        currentRadius = (outPixel > inPixel)? currentRadius+1 : currentRadius-1;

//        int temp = GetPixelDensity(GetXYFromRadial(currentRadius, currentAngle));
//        while ( temp <= COLOR_BORDER_MAX) {
//            currentRadius++;
//            temp = GetPixelDensity(GetXYFromRadial(currentRadius, currentAngle));
//        }


        currentPoint = GetXYFromRadial(currentRadius, currentAngle);



        if (currentRadius >= RADIUS_MAX_ALLOWED) {
            currentPoint.x = 0;
            currentPoint.y = 0;
        }


        return currentPoint;
    }


    public int GetPixelDensity(Point point) {
        int pixelDensity = 0;


        //TODO: pixel Density code here

        pixelDensity = bitmap.getPixel(point.x, point.y);

        pixelDensity &= 0xff;

        return pixelDensity;
    }


    AudioTrack track;

    public void ParseRecourseId(Resources resources, int res)
    {

        Bitmap temp = BitmapFactory.decodeResource(resources, res);
        ByteArrayBuffer array = ParseImageToByteArray(temp);
        ByteArrayBuffer array1 = new ByteArrayBuffer(array.length());

        for(int i=0; i<array.length();i++)
        {
            array1.append(array.byteAt(array.length()-i));
        }

        track = ConvertArrayToAudioTrack(array1);
        if (track!=null) {
            String s = new String(array.buffer());
            Log.d("tst", "track != null, playing" + s);
            track.play();
            track.write(array1.buffer(), 0, array1.length());
            //track.stop();
            //track.release();
            /*
            // Write the byte array to the track

            track.write(byteData, 0, byteData.length);
            track.stop();
            track.release();*/
        }
        else
        {
            Log.d("tst", "audio track is not initialised ");
        }
    }


    public ByteArrayBuffer ParseImageToByteArray(Bitmap bmp) {
        boolean parsed = false;

        bitmap = bmp;

        //bitmap1 = Bitmap.createBitmap(bitmap);

        ByteArrayBuffer data = new ByteArrayBuffer(0);


        //set center of image

        center = new Point(0, 0);
        center.x = bmp.getWidth() / 2;
        center.y = bmp.getHeight() / 2;

        //Let the parsing begin!

        currentRadius = 0;
        currentAngle = 0;

        //Move from center to out
        currentPoint = new Point(center);


        byte density = (byte)GetPixelDensity(GetXYFromRadial(currentRadius, currentAngle));

        while (density <= COLOR_BORDER_MAX) {
            currentRadius++;
            density = (byte)GetPixelDensity(GetXYFromRadial(currentRadius, currentAngle));
        }


        //now we're on the line:

        while (true) {
            //TODO: add parsing code here
            Point point = GetNextPoint();
            if (point.x == 0 && point.y == 0)
                break;

            data.append(GetPixelDensity(point));


            if(data.length() % 100000 == 0)
            {
                ShowToast(""+data.length());
                Log.e("tst", ""+data.length());
            }

            if(data.length() >= DATA_LENGTH_MAX)
            {
                Log.e("tst", "data.length() >=" + data.length());
                break;
            }


        }

        if(false)
            SaveBitmap(bitmap);

        return data;

    }

    @Override
    public void run() {
        super.run();

        ShowToast("parsing started!");


        ParseRecourseId(resources, res);

    }
}
