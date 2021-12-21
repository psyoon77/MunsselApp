package com.munsellapp.munsellcolorrecognitionapp;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* THis activity displays the main screen of the app, with three buttons (Take picture, Select from gallery and calibrate camera)
NOTE calibrate camera button does not work yet, will have to be integrated when ImageSelction and Calibration activities work
as expected. Take Picture Button and Select from Gallery Button will prompt the camera intent, which will pass the image to Image
Activity. NOTE select from gallery option currently only works on images that were not previously taken by the camera, but does
work for images that are downloaded. */
public class MainActivity extends Activity implements View.OnClickListener {


    private static int TAKE_PIC = 0;
    private static int CALIBRATE_PIC = 2;
    private static int SELECT_FILE = 1;
    final int CROP_PIC = 3;
    final int CROPCALI_PIC = 4;
    private Button chooseImage;
    private Uri photo;
    private Uri caliPhoto;
    public Uri resultProvider;
    private static final String PHOTOS = "photos";
    private File output = null;
    protected final static String TAG = "ColorUtils";
    private static final String FILENAME = "CameraContentDemo.jpeg";
    private static final String EXTRA_FILENAME = "com.munsellapp.munsellcolorrecognitionapp.EXTRA_FILENAME";
    private static final int PICK_FROM_GALLERY = 1;
    private static final String AUTHORITY = "com.munsellapp.munsellcolorrecognitionapp.provider";
    Uri intermediateProvider;
    public String resultName = "2.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chooseImage = (Button) findViewById(R.id.ChooseImage);
        chooseImage.setOnClickListener(this);

        /* new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert")
                .setMessage("If you would like to use the location feature of this app, please turn your" +
                        " location on.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })

                .show(); */

        if (savedInstanceState == null) {
            output = new File(new File(getFilesDir(), PHOTOS), FILENAME); //createImageFile();//

            if (output.exists()) {
                output.delete();
            } else {
                output.getParentFile().mkdirs();
            }


        } else {
            output = (File) savedInstanceState.getSerializable(EXTRA_FILENAME);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }

    }


    /*Starts camera Intent -JB*/
    public void CameraClick(View v) {
        /*
        Intent intent2 = new Intent(this, Calibrate.class);
        startActivity(intent2);

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_PIC);
        } catch (ActivityNotFoundException anfe) {
            Toast toast = Toast.makeText(this, "This device doesn't support the crop action!",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
         */
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri outputUri = FileProvider.getUriForFile(this, AUTHORITY, output);

        i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip =
                    ClipData.newUri(getContentResolver(), "A photo", outputUri);

            i.setClipData(clip);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            List<ResolveInfo> resInfoList =
                    getPackageManager()
                            .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, outputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }

        try {
            startActivityForResult(i, TAKE_PIC);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_no_camera, Toast.LENGTH_LONG).show();
            finish();
        }

    }

    /*Starts camera Intent -JB*/
    public void CalibrateCameraClick(View v) {
        Intent my = new Intent(this, CalibrateHome.class);
        startActivity(my);

    }

    /*Opens gallery view, then sets Result Code signaling that
    image has been selected -JB
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }


    /*Gets result code from camera and gallery intents
    if result is from camera intent, sends image to ImageActivity;
    if result is from gallery intent, calls function to send image to
    ImageActivity -JB
      */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TAKE_PIC && resultCode == RESULT_OK) {
            // photo = data.getData();
            // performCrop();
            photo = FileProvider.getUriForFile(this, AUTHORITY, output);
            performCrop(photo, output);
            // PassBitmapToNextActivity(photo,ImageActivity.class,"CameraImage");
        }

        if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
            // Bitmap bm;
            if (data != null) {
                // try {
                // bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                PassBitmapToNextActivity(data.getData(), ImageActivity.class, "GalleryImage");
                // } catch (IOException e) {
                // e.printStackTrace();
            }
        }// }

        if (requestCode == CALIBRATE_PIC && resultCode == RESULT_OK) {
            caliPhoto = data.getData();
            // performCalibrationCrop();
        }
        if (requestCode == CROPCALI_PIC) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap theCaliPic = extras.getParcelable("data");
            // PassBitmapToNextActivity(theCaliPic,Calibrate.class,"CalibrateImage");
        } else if (requestCode == CROP_PIC) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap thePic = extras.getParcelable("data");
            // PassBitmapToNextActivity(thePic,ImageActivity.class,"CameraImage");

        }
    }

    /*Passes Bitmap from any intent (camera, gallery, or calibrate camera) and passes it to specified activity)*/
    public void PassBitmapToNextActivity(Uri uri, Class myClass, String extraName) {
        Intent intent = new Intent(this, myClass);
        // ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        intent.putExtra(extraName, resultProvider);
        intent.setData(uri);
        startActivity(intent);

    }

    /*
    Either calls galleryIntent when ChooseImage button is clicked
    or opens cameraIntent -JB
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ChooseImage) {
            galleryIntent();
        } else {
            Intent intent = new Intent(this, ImageActivity.class);
            startActivity(intent);

        }


    }

    private void performCrop(Uri hPic, File hfile) {
        // take care of exceptions
        /*
        try {
            // call the standard crop action intent (the user device may not
            // support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(photo, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 2);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PIC);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }
        */
    }
}
