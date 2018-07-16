package com.example.divyanshu.samosa;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tensorflow.lite.Interpreter;


public class MainActivity extends AppCompatActivity {
    int REQUEST_IMAGE_CAPTURE = 1;
    int WRITE_EXTERNAL_STORAGE = 2;
    String mCurrentPhotoPath;
    TextView tv1;
    TextView tv2;
    TextView result;
    public ImageView imageView;
    Uri file;
    Bitmap bitmap = null;
    Bitmap bm = null;
    Interpreter tflite;
    String MODEL_FILE = "model_new.tflite";
    int pich = 256;
    int picw = 256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_IMAGE_CAPTURE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                3);
        Button button = (Button) findViewById(R.id.launch);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    //    imageView = (ImageView) findViewById(R.id.imageView);
        TextView tv1 = (TextView)findViewById(R.id.tv1);
        TextView tv2 = (TextView)findViewById(R.id.tv2);
        result = (TextView)findViewById(R.id.result);

        //Load the Tensorflow Interpreter

        {
            try {
                tflite = new Interpreter(loadModelFile(MainActivity.this,MODEL_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }

    private File getOutputMediaFile(){
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    public void dispatchTakePictureIntent() {
        final int REQUEST_TAKE_PHOTO = 1;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            file = FileProvider.getUriForFile(this, "com.example.android.fileprovider", getOutputMediaFile());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);

            startActivityForResult(takePictureIntent, 100);
            }

        }
    @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int[] pix = new int[picw * pich];
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                //imageView.setImageURI(file);
                try {
                    int pich = 256;
                    int picw = 256;
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
                    tv1 = (TextView)findViewById(R.id.tv1);
                    ImageView im = (ImageView)findViewById(R.id.imview);
                    im.setImageBitmap(bitmap);
                    if(bitmap != null)
                    {
                        tv1.setText("Not Null");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tv2 = (TextView)findViewById(R.id.tv2);
                bm = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
                bm.getPixels(pix, 0, picw, 0, 0, picw, pich);
                if(bm != null)
                {

                    tv2.setText("NotNull");
                }
      /*          Utils.bitmapToMat(bmp32, mat);
                Mat dst = new Mat();
                ArrayList<Mat> m = new ArrayList<Mat>();
                Core.split(mat, m);
                ArrayList<Mat> mx = new ArrayList<Mat>();
                mx.add(m.get(1));
                mx.add(m.get(2));
                mx.add(m.get(3));
                Core.merge(mx, dst);
                Mat dst1 = new Mat();
                Imgproc.cvtColor(dst, dst1, Imgproc.COLOR_RGB2BGR );
                if(dst1 == null)
                {
                    Log.e("cvtColor", "onActivityResult: Not Converted");
                }
//                ArrayList <Mat> matlist = new ArrayList<Mat>();
//                Core.split(mat, matlist);
//                ArrayList <Mat> matRGB = new ArrayList <Mat>();
//                Mat rgb =  new Mat(mat.size(), mat.type());
//                matRGB.add(matlist.get(1));
//                matRGB.add(matlist.get(2));
//                matRGB.add(matlist.get(3));
//                Core.merge(matRGB, rgb);

*/               int R = 0, G = 0, B = 0;

                int[][] Red = new int[256][256];
                int[][] Green = new int[256][256];
                int[][] Blue = new int[256][256];

                float [][][][] image = new float[1][256][256][3];
                for (int y = 0; y < pich; y++) {
                    for (int x = 0; x < picw; x++) {
                        int index = y * picw + x;
                        R = (pix[index] >> 16) & 0xff;     //bitwise shifting
                        Red[y][x] = R;
                        G = (pix[index] >> 8) & 0xff;
                        Green[y][x] = G;
                        B = pix[index] & 0xff;
                        Blue[y][x] = B;
                    }
                }
                //For Red
                for(int x = 0; x < 256; x++)
                {
                    for(int y = 0; x < 256; x++)
                    {
                        image[0][x][y][2] = Red[x][y];
                    }
                }
                //For Green
                for(int x = 0; x < 256; x++)
                {
                    for(int y = 0; x < 256; x++)
                    {
                        image[0][x][y][1] = Green[x][y];
                    }
                }
                //For Blue
                for(int x = 0; x < 256; x++)
                {
                    for(int y = 0; x < 256; x++)
                    {
                        image[0][x][y][0] = Blue[x][y];
                    }
                }
                for(int x = 0; x < 256; x++)
                {
                    for(int y = 0; x < 256; x++)
                    {
                        for(int z = 0; z < 3; z++)
                        {
                            image[0][x][y][z] = (image[0][x][y][z]/255);
                        }

                    }
                }
                float[][][][] inp = image;
                float[][] out = new float[1][2];
                for(int x = 0; x < 256; x++)
                {
                    for(int y = 0; x < 256; x++) {
                        for (int z = 0; z < 3; z++) {
                         String s =   Float.toString(image[0][x][y][z]);
                                tv1.append(s);
                                tv1.append("  ");
                            }
                        }
                    }

                tflite.run(inp,out);
                double prob1 = out[0][0];
                double prob2 = out[0][1];
                //double prob1 =  (Math.pow(2.718, out[0][0])/(Math.pow(2.718, out[0][0])+ Math.pow(2.718, out[0][1])));

                //double prob2 =  (Math.pow(2.718, out[0][1])/(Math.pow(2.718, out[0][0])+ Math.pow(2.718, out[0][1])));
                String p1 = Double.toString(prob1);
                String p2 = Double.toString(prob2);

                //tv1.setText(p1);
                tv2.setText(p2);

                if(prob1 > prob2)
                {
                    result.setText("This is a samosa!");
                }
                else
                {
                    result.setText("This is not a Samosa");
                }
            }
        }
    }
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}
