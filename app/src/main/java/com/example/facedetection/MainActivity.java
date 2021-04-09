package com.example.facedetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

import static android.text.TextUtils.concat;

public class MainActivity extends AppCompatActivity {
    private Button cameraButton;
    private final static int REQUEST_IMAGE_CAPTURE = 124;
    private FirebaseVisionImage image;
    private FirebaseVisionFaceDetector detector;
    //int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
                }
            }
        });

}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            detectFace(bitmap);

        }

    }


    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options;
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();

        try {
            image = FirebaseVisionImage.fromBitmap(bitmap);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText = "";
                int i = 1;
                for (FirebaseVisionFace face : firebaseVisionFaces){
                    resultText = resultText.concat("\n"+i+".")
                            .concat("\nSmile: " + face.getSmilingProbability()*100+"%")
                            .concat("\nLeftEye "+ face.getLeftEyeOpenProbability()*100+"%")
                            .concat("\nRightEye "+ face.getRightEyeOpenProbability()*100+"%");
                    i++;
                }
                if(firebaseVisionFaces.size()==0)
                {
                    Toast.makeText(MainActivity.this, "NO FACES", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(LCOFaceDetection.RESULT_TEXT, resultText);
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(), LCOFaceDetection.RESULT_DIALOG);

                }
            }
        });
                

    }
}