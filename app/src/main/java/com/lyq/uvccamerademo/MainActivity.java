package com.lyq.uvccamerademo;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private int mUvcCameraSum = 6; // 默认4个摄像头（展示区）
    private Handler mDelayHandler = new Handler();
    private Runnable mCameraRunnable = () -> {
        // 申请权限并打开摄像头
        UvcCameraController.getInstance().checkPermissionOpenCamera();
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        Button btnScreenshot = (Button) findViewById(R.id.btn_screenshot);
        btnScreenshot.setOnClickListener(this);
        initCamera();
    }

    private void initCamera() {
        UvcCameraController.getInstance().init(this);
        // 初始化摄像头
        for (int i = 0; i < mUvcCameraSum; i++) {
            CameraViewInterface uvcCameraInterface = null;
            switch (i) {
                case 0:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_0);
                    break;
                case 1:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_1);
                    break;
                case 2:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_2);
                    break;
                case 3:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_3);
                    break;
                case 4:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_4);
                    break;
                case 5:
                    uvcCameraInterface = (CameraViewInterface) findViewById(R.id.uvc_camera_5);
                    break;
            }
            if (null != uvcCameraInterface) UvcCameraController.getInstance().initCamera(uvcCameraInterface);
        }
        // 需要时间初始化摄像头，Surface等，否则无返回数据（无画面）
        mDelayHandler.postDelayed(mCameraRunnable, 500);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UvcCameraController.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        UvcCameraController.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        UvcCameraController.getInstance().onDestroy();
        super.onDestroy();
    }

    public void queueEvent(UVCCameraHandler uvcCameraHandler){
        if (null != uvcCameraHandler) {
            queueEvent(uvcCameraHandler::close, 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_screenshot:
                UvcCameraController.getInstance().screenshot();
                break;
        }
    }
}
