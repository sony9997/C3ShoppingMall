package com.c3.jbz.comp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.c3.jbz.R;
import com.c3.jbz.activity.MainActivity;
import com.c3.jbz.util.ImageFilePath;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hedong on 2017/10/16.
 */

public class C3WebChromeClient extends WebChromeClient {
    private Activity activity;
    private ProgressBar pbMain;

    public C3WebChromeClient(Activity activity,ProgressBar pbMain){
        this.activity=activity;
        this.pbMain=pbMain;
    }
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 2;
    private ValueCallback<Uri[]> mFilePathCallback;

    private String mCameraPhotoPath;

    // android 5.0
    public boolean onShowFileChooser(
            WebView webView, ValueCallback<Uri[]> filePathCallback,
            WebChromeClient.FileChooserParams fileChooserParams) {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("WebViewSetting", "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.image_chooser));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        activity.startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

        return true;
    }
    private static final String cameraPath=Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM+File.separator+"Camera"+File.separator;
    private File createImageFile() throws IOException {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        Date date=new Date(System.currentTimeMillis());
        String fileName=simpleDateFormat.format(date);
        File file=new File(cameraPath+fileName+".jpg");
        file.createNewFile();
        return file;
    }

    //The undocumented magic method override
    //Eclipse will swear at you if you try to put @Override here
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {

        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(i, activity.getString(R.string.image_chooser)), FILECHOOSER_RESULTCODE);

    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        activity.startActivityForResult(
                Intent.createChooser(i, activity.getString(R.string.image_chooser)),
                FILECHOOSER_RESULTCODE);
    }

    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(i, activity.getString(R.string.image_chooser)), FILECHOOSER_RESULTCODE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null
                    : data.getData();
            if (result != null) {
                String imagePath = ImageFilePath.getPath(activity, result);
                if (imagePath!=null&&imagePath.trim().length()>0) {
                    result = Uri.parse("file:///" + imagePath);
                }
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == INPUT_FILE_REQUEST_CODE && mFilePathCallback != null) {
            // 5.0的回调
            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,results[0]));
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,results[0]));
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        }
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

        new AlertDialog.Builder(activity).setTitle(R.string.alert_title).setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                }).setCancelable(false).create().show();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {

        new AlertDialog.Builder(activity).setTitle(R.string.alert_title).setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                result.cancel();
            }
        }).setCancelable(false).create().show();
        return true;

    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        // TODO 自动生成的方法存根
        if (newProgress == 100) {
            pbMain.setVisibility(View.GONE);//加载完网页进度条消失
        } else {
            pbMain.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
            pbMain.setProgress(newProgress);//设置进度值
        }
    }
}
