package com.xch.choosephoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 调用拍照或打开相册获取图片的demo
 * 怎样获取缩略图及原图
 * 怎样打开相册
 * 怎样将图片添加到系统相册
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String AUTHORITIES = "com.xch.aaa";
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_TAKE_THUMBNAIL = 2;
    public static final int REQUEST_CHOOSE_IMAGE = 3;

    private File mImageFile;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.take_thumbnail).setOnClickListener(this);
        findViewById(R.id.take_photo).setOnClickListener(this);
        findViewById(R.id.choose_image).setOnClickListener(this);
        mImageView = findViewById(R.id.image);
    }

    @Override
    public void onClick(View v) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch (v.getId()) {
            case R.id.take_thumbnail:
                if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePhotoIntent, REQUEST_TAKE_THUMBNAIL);
                }
                break;
            case R.id.take_photo:
                try {
                    mImageFile = createImageFile();
                    Uri uri = FileProvider.getUriForFile(this, AUTHORITIES, mImageFile);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.choose_image:
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (pickImageIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(pickImageIntent, REQUEST_CHOOSE_IMAGE);
                }
                break;
            default:
                break;
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.getDefault()).format(new Date());
        String imageFileName = "photo_" + timestamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_THUMBNAIL:
                if (resultCode == RESULT_OK) {
                    //get thumbnail
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            mImageView.setImageBitmap(bitmap);
                        }
                    }
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //get raw file
                    Bitmap takenImage = BitmapFactory.decodeFile(mImageFile.getPath());
                    mImageView.setImageBitmap(takenImage);
                    addPicToGallery();
                }
                break;
            case REQUEST_CHOOSE_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri photoUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                        mImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addPicToGallery() {
        //如果图片放在私有目录下，会add失败，这里copy到公共目录下再add
        String destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + mImageFile.getName();
        copyFile(mImageFile.getPath(), destPath);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(destPath));
        intent.setData(contentUri);
        sendBroadcast(intent);
    }

    private void copyFile(String srcPath, String destPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcPath);
            outputStream = new FileOutputStream(destPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safeClose(inputStream);
            safeClose(outputStream);
        }
    }

    private void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
