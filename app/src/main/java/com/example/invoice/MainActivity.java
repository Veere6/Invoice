package com.example.invoice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import static com.example.invoice.App.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {


    private NotificationManagerCompat notificationManager;
    private ScrollView scrollView;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String FOLDER_NAME ="/ServiceInvoice/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollView=findViewById(R.id.scroll);
        StrictMode.VmPolicy.Builder builder=new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkFolder();


       notificationManager = NotificationManagerCompat.from(this);

        final TextView download=findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(MainActivity.this);

                takeScreenshot();
                sendOnChannel1();

            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionread = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        if (permissionread != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        ScrollView memecontentView =(ScrollView) findViewById(R.id.scroll);
        View view1 = memecontentView;

        Bitmap b = Bitmap.createBitmap(view1.getWidth(), view1.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        view1.draw(canvas);



        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/"  + FOLDER_NAME +  "/" + now + ".jpg";


        FileOutputStream fos = null;
        view1.setDrawingCacheEnabled(true);

        try {
            Toast.makeText(this, "Invoice saved", Toast.LENGTH_SHORT).show();
            fos = new FileOutputStream(mPath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), b,
                    mPath, "INVOICE");



        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //auto show to download the image

//        Intent intent=new Intent(Intent.ACTION_VIEW);
//        Uri uri=Uri.fromFile(new File(mPath));
//        intent.setDataAndType(uri,"image/jpeg");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);

    }

    public void checkFolder() {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
        File dir = new File(path);
        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            Toast.makeText(MainActivity.this, "Not Created", Toast.LENGTH_SHORT).show();
            isDirectoryCreated = dir.mkdir();
        }
        if (isDirectoryCreated) {
            // do something\
            Toast.makeText(MainActivity.this, "Already Created", Toast.LENGTH_SHORT).show();
            Log.d("Folder", "Already Created");

        }
    }


    public void sendOnChannel1(){

        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/"  + FOLDER_NAME +  "/" + now + ".jpg";

        Uri uri=Uri.fromFile(new File(mPath));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"image/jpeg");
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, intent, 0);

        PendingIntent actionIntent =
                PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Notification notification= new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notificationlogo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.id.scroll))
                .setContentTitle("Invoice saved")
                .setContentText("Tap here to open it in Gallery.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLACK)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.logo,"",actionIntent)
                .build();
                 notificationManager.notify(1,notification);
    }
}
