package com.example.facerecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.facerecognition.utility.BitmapUtil;
import com.example.facerecognition.utility.ImageUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    protected Interpreter tflite;
    private  int imageSizeX;
    private  int imageSizeY;

    private Context context;
    PreviewView mPreviewView;
    Button register;
    FaceDetector detector = null;
    Bitmap cropped = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        detector =  FaceDetection.getClient();
        mPreviewView = findViewById(R.id.camera_signup);
        register = findViewById(R.id.register);

        getSupportActionBar().setTitle("Register");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageUtil(context).save(cropped);
                Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        context = this;
        startCamera();
        initComponents();
    }

    private Executor executor = Executors.newSingleThreadExecutor();
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                try {
                    Image mediaImage = image.getImage();
                    if (mediaImage != null) {
                        InputImage image2 = InputImage.fromMediaImage(mediaImage, rotationDegrees);
                        //pass this image to ML
                        detector.process(image2)
                                .addOnSuccessListener(
                                        faces -> {
//                                                // Task completed successfully
//                                                for (Face face : faces) {
//
//                                                }
                                            if(faces.size() > 0){
                                                Log.d("Mohan","Face Detected");
                                                if(faces.size() > 1){
                                                    Toast.makeText(SignUpActivity.this, "Please stand alone, Only one face is required", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }else{
                                                    Rect bounds = faces.get(0).getBoundingBox();
                                                    Bitmap bitmap = BitmapUtil.getBitmap(image);
                                                    cropped = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
                                                    register.setEnabled(true);
                                                    register.setBackground(getResources().getDrawable(R.color.colorGreen));
                                                }
                                                imageAnalysis.clearAnalyzer();
                                            }else{
                                                register.setEnabled(false);
                                                register.setBackground(getResources().getDrawable(R.color.colorGray));
                                                Log.d("Mohan" , "Face is not detected in the image");
                                            }

//                                            image.close();
                                        })
                                .addOnFailureListener(
                                        e -> {
                                            register.setEnabled(false);
                                            register.setBackground(getResources().getDrawable(R.color.colorGray));
                                            Log.d("Mohan","Error is detecting face : " + e.getMessage());
                                        }).addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Face>> task) {
                                image.close();
                            }
                        });
                    }
                }catch (Exception e){
                    Log.d("Mohan",e.getMessage());
                }
            }
        });

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);

    }



    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor=activity.getAssets().openFd("Qfacenet.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }

    private void initComponents() {
        try{
            tflite=new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}