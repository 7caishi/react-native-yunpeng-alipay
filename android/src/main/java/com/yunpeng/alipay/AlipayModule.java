package com.yunpeng.alipay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

/**
 * Created by m2mbob on 16/5/6.
 */
public class AlipayModule extends ReactContextBaseJavaModule{
    private Context mContext;

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    private static final String TAG = "AlipayModule";


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(getReactApplicationContext().getMainLooper()) {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            try{
                Log.d(TAG, "msg.what: " + msg.what);
                switch (msg.what) {
                    case SDK_PAY_FLAG: {
                        String resultStatus = (String) msg.obj;
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            Toast.makeText(getCurrentActivity(), "支付成功", Toast.LENGTH_SHORT).show();
                        } else {
                            // 判断resultStatus 为非"9000"则代表可能支付失败
                            // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            if (TextUtils.equals(resultStatus, "8000")) {
                                Toast.makeText(getCurrentActivity(), "支付结果确认中", Toast.LENGTH_SHORT).show();
                            } else {
                                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                Toast.makeText(getCurrentActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    case SDK_AUTH_FLAG: {
                        @SuppressWarnings("unchecked")
                        String resultStatus = (String) msg.obj;
                        // 判断resultStatus 为“9000”且result_code
                        // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            // 获取alipay_open_id，调支付时作为参数extern_token 的value
                            // 传入，则支付账户为该授权账户
                            Toast.makeText(getCurrentActivity(),"授权成功", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getCurrentActivity(), "支付结果确认中", Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他状态值则为授权失败
                            Toast.makeText(getCurrentActivity(), "授权失败", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }catch (Exception e){
                Log.d(TAG, "error: " + e.toString());
            }
        };
    };

    public AlipayModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void pay(final String payInfo,final Promise promise) {
        final WritableNativeArray arr =new WritableNativeArray();
        final WritableNativeMap map = new WritableNativeMap();
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    PayTask alipay = new PayTask(getCurrentActivity());
                    String result = alipay.pay(payInfo, true);
                    PayResult payResult = new PayResult(result);

                    String resultInfo = payResult.getMemo();
                    String resultStatus = payResult.getResultStatus();
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = resultStatus;
                    mHandler.sendMessage(msg);
                    if(Integer.valueOf(resultStatus) >= 8000){
                        map.putString("resultStatus",resultStatus);
                        arr.pushMap(map);
                        promise.resolve(arr);
                    }else{
                        promise.reject(resultInfo, new RuntimeException(resultStatus+":"+resultInfo));
                    }
                } catch (Exception e) {
                    promise.reject(e.getLocalizedMessage(), e);
                }
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @ReactMethod
    public void login(final String authInfo, final Promise promise) {
        Runnable authRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // 构造AuthTask 对象
                    AuthTask authTask = new AuthTask(getCurrentActivity());
                    String result = authTask.auth(authInfo, true);
                    // 调用授权接口，获取授权结果
                    AuthResult authResult = new AuthResult(result, true);
                    String resultStatus = authResult.getResultStatus();
                    String resultInfo = authResult.getMemo();
                    Message msg = new Message();
                    msg.what = SDK_AUTH_FLAG;
                    msg.obj = resultStatus;
                    mHandler.sendMessage(msg);
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        promise.resolve(result);
                    }else{
                        promise.reject(resultInfo, new RuntimeException(resultStatus+":"+resultInfo));
                    }
                } catch (Exception e) {
                    promise.reject(e.getLocalizedMessage(), e);
                }
            }
        };
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }

    @Override
    public String getName() {
        return "AlipayModule";
    }

}
