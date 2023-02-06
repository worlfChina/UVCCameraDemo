package com.lyq.uvccamerademo;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.SystemClock;
import android.view.Surface;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UvcCameraDataCallBack;
import com.serenegiant.widget.CameraViewInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liuyq
 * @date 2019/7/2
 */
public class UvcCameraController {

    private static UvcCameraController mUvcCameraController;

    public static UvcCameraController getInstance() {
        if (null == mUvcCameraController) {
            synchronized (UvcCameraController.class) {
                if (null == mUvcCameraController) {
                    mUvcCameraController = new UvcCameraController();
                }
            }
        }
        return mUvcCameraController;
    }

    private static final String UVC_CAMERA_PHOTO = "/sdcard/hk_";
    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private List<UsbDevice> mUsbDeviceList = new ArrayList<UsbDevice>(); // 存放USB设备列表
    private ArrayList<UVCCameraHandler> mUvcCameraHandlerList = new ArrayList<UVCCameraHandler>(); // 存放UvcCameraHandler类
    private ArrayList<CameraViewInterface> mCameraViewInterface = new ArrayList<CameraViewInterface>(); // 存放interface
    private WeakReference<MainActivity> mWeakReference = null;

    public void init(MainActivity activity) {
        this.mWeakReference = new WeakReference(activity);
        mCameraViewInterface.clear();
        mUvcCameraHandlerList.clear();
        if (null != mWeakReference) {
            MainActivity mainActivity = mWeakReference.get();
            if (null != mainActivity) {
                // 参数初始化赋值
                mUSBMonitor = new USBMonitor(mainActivity, mOnDeviceConnectListener);
                List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(mainActivity, R.xml.device_filter);
                mUsbDeviceList = mUSBMonitor.getDeviceList(deviceFilters.get(0));
            }
        }
    }

    /**
     * 带有回调数据的初始化（带回调图像显示可靠）
     * 按顺序初始化展示区
     */
    public void initCamera(CameraViewInterface uvcCameraInterface) {
        if (null != mWeakReference) {
            MainActivity mainActivity = mWeakReference.get();
            if (null != mainActivity) {
                //设置显示宽高
                uvcCameraInterface.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / UVCCamera.DEFAULT_PREVIEW_HEIGHT);
                UVCCameraHandler cameraHandler = UVCCameraHandler.createHandler(mainActivity, uvcCameraInterface
                        , UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT
                        , BANDWIDTH_FACTORS[0], vmcDataCallBack);
                // 添加列表
                mCameraViewInterface.add(uvcCameraInterface);
                mUvcCameraHandlerList.add(cameraHandler);
            }
        }
    }

    /**
     * lambda => UVC摄像头图像回调
     */
    private UvcCameraDataCallBack vmcDataCallBack = data -> {

    };

    public void onStart() {
        if (null != mUSBMonitor) mUSBMonitor.register();
        for (CameraViewInterface cameraViewInterface : mCameraViewInterface) {
            if (null != cameraViewInterface) cameraViewInterface.onResume();
        }
    }

    public void onStop() {
        for (int i = 0; i < mCameraViewInterface.size(); i++){
            UVCCameraHandler uvcCameraHandler = mUvcCameraHandlerList.get(i);
            if (null != uvcCameraHandler) uvcCameraHandler.close();
            CameraViewInterface cameraViewInterface = mCameraViewInterface.get(i);
            if (null != cameraViewInterface) cameraViewInterface.onPause();
        }
        mUSBMonitor.unregister(); // usb管理器解绑
    }

    public void onDestroy() {
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    /**
     * 检查权限并打开摄像头
     */
    public void checkPermissionOpenCamera() {
        // 申请权限并打开摄像头
        for (int i = 0; i < mUsbDeviceList.size(); i++) {
            UsbDevice usbDevice = mUsbDeviceList.get(i);
            // TODO: 2019/7/2 这里可以根据不同USB设备的VendorId对特定摄像头USB放开
//            if (usbDevice.getVendorId() == 0xbda) { // 海康威视摄像头
            UVCCameraHandler uvcCameraHandler = mUvcCameraHandlerList.get(i);
            // 如果开启，关闭（在连接成功的时候会打开）
            if (uvcCameraHandler.isOpened()) {
                uvcCameraHandler.close();
            }
            // 申请权限并打开
            if (null != mUSBMonitor) mUSBMonitor.requestPermission(usbDevice);

            // 每一个摄像头需要间隔开启
            SystemClock.sleep(500);
//            }
        }
    }

    /**
     * 截图
     */
    public void screenshot() {
        try {
            for (int i = 0; i < mUvcCameraHandlerList.size(); i++) {
                mUvcCameraHandlerList.get(i).captureStill(UVC_CAMERA_PHOTO + i + ".png");
            }
        } catch (Exception e) {
            // 空指针处理
        }
    }


    /**
     * USB动作监听
     */
    private USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {


        @Override
        public void onAttach(UsbDevice device) {
            // Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show()
        }

        @Override
        public void onDettach(UsbDevice device) {
//            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
//            Log.d(TAG, "onConnect: 设备连接成功：" + device.getDeviceName())
            //设备连接成功
            for (int i = 0; i < mUvcCameraHandlerList.size(); i++) {
                UVCCameraHandler uvcCameraHandler = mUvcCameraHandlerList.get(i);
                if (!uvcCameraHandler.isOpened()) {
                    uvcCameraHandler.open(ctrlBlock);
                    CameraViewInterface cameraViewInterface = mCameraViewInterface.get(i);
                    SurfaceTexture st = cameraViewInterface.getSurfaceTexture();
                    uvcCameraHandler.startPreview(new Surface(st));
                    break;
                }
            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            for (int i = 0; i < mUvcCameraHandlerList.size(); i++) {
                UVCCameraHandler uvcCameraHandler = mUvcCameraHandlerList.get(i);
                if (!uvcCameraHandler.isEqual(device)) {
                    MainActivity mainActivity = mWeakReference.get();
                    mainActivity.queueEvent(uvcCameraHandler);
                }
            }
        }

        @Override
        public void onCancel(UsbDevice device) {
//            Toast.makeText(MainActivity.this, "USB_DEVICE_CANCEL", Toast.LENGTH_SHORT).show();
        }

    };
}
