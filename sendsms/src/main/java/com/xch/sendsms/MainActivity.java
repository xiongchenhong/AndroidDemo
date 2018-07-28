package com.xch.sendsms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 一个调用系统发送短信能力的demo
 * 分通过隐式intent调用和直接调用两种
 * 绝大数情况下，使用隐式intent即可
 * 如果不希望界面跳出而直接发送，则使用smsManager的接口
 * <p>
 * demo没有考虑解析pdu数据这一块，如果要开发代替系统短信的app，需要深入学习这里
 * <p>
 * 由于android 8.0的bug，在8.0上静默发送短信必须要获取READ_PHONE_STATE权限
 * 详见https://issuetracker.google.com/issues/66979952
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    public static final String SMS_SCHEME = "smsto";
    public static final String SEND_SMS_PERMISSION = Manifest.permission.SEND_SMS;
    public static final String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    public static final int REQUEST_SMS_CODE = 100;
    public static final String ACTION_SEND_SMS = "com.xch.intent.action.SEND_SMS";
    public static final String ACTION_DELIVERY_SMS = "com.xch.intent.action.DELIVERY_SMS";
    public static final String NUMBER = "10086";
    public static final String MESSAGE = "10086";

    private EditText mNumber;
    private EditText mMessage;
    private BroadcastReceiver mSendReceiver;
    private BroadcastReceiver mDeliveryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNumber = findViewById(R.id.number);
        mMessage = findViewById(R.id.message);
        mNumber.setText(NUMBER);
        mMessage.setText(MESSAGE);
        Button sendToSystem = findViewById(R.id.send_to_system);
        Button sendOwn = findViewById(R.id.send_own);
        sendToSystem.setOnClickListener(this);
        sendOwn.setOnClickListener(this);
        initReceiver();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_to_system:
                sendToSystem();
                break;
            case R.id.send_own:
                checkPermission();
                break;
        }
    }

    private void initReceiver() {
        mSendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case RESULT_OK:
                        showToast("发送成功");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        showToast("Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        showToast("radio was turned off");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showToast("no pdu provided");
                        break;
                    default:
                        showToast("发送失败");
                }
            }
        };
        mDeliveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /*
                这里收到回调即表明信息已经送达成功，可以从pdu中获取更多详细信息
                关于pdu数据的解析，涉及到手机通信的知识，这里不再深究
                一般只是调用发送短信接口的话不需要在这里深入，如果想开发一个代替系统短信的app，就要深入学习这一块了
                * */
                showToast("接收成功");
                byte[] pdu = intent.getByteArrayExtra("pdu");
                SmsMessage smsMessage = SmsMessage.createFromPdu(pdu);
                Log.d(TAG, "onReceive: " + smsMessage.getStatus());
            }
        };
        registerReceiver(mSendReceiver, new IntentFilter(ACTION_SEND_SMS));
        registerReceiver(mDeliveryReceiver, new IntentFilter(ACTION_DELIVERY_SMS));
    }

    /**
     * 标准的隐式intent发送短信的方式
     * 这里不用ACTION_SEND而用ACTON_SEND_TO的原因是SEND一般用于接收方不明确的情况，使用createChooser交给用户选择
     * 而SEND_TO可以配合Uri可以明确的指定交给短信app处理
     * <p>
     * 这里同样可以用于发送彩信，不过随着微信的普及，彩信的使用量已经微乎其微，这里不再深究
     * <p>
     * 参见https://developer.android.com/guide/components/intents-common#Messaging
     */
    private void sendToSystem() {
        String number = mNumber.getText().toString();
        String message = mMessage.getText().toString();
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(message)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(SMS_SCHEME + ":" + number));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, SEND_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{SEND_SMS_PERMISSION, READ_PHONE_STATE_PERMISSION}, REQUEST_SMS_CODE);
        } else {
            sendMessage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showToast("permission denied");
                    return;
                }
                sendMessage();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSendReceiver != null) {
            unregisterReceiver(mSendReceiver);
        }
        if (mDeliveryReceiver != null) {
            unregisterReceiver(mDeliveryReceiver);
        }
    }

    /**
     * 关于smsManager的sendTextMessage方法，第一个参数号码，第三个参数信息内容，很清晰
     * 第二个参数srcAddress是指短信服务中心号码（SMSC）,我们平时使用的短信，都是先发送到短信服务中心，由服务中心在基站之间传送，
     * 再发送到最终的客户端，SMSC必须要指定才能正常发送短信，当然手机制造商都会设置好默认值，用户不用关心，调用这个api时，传入null使用
     * 默认的SMSC即可
     * <p>
     * 第四个参数是用于短信成功发送到服务中心的回调
     * 第五个参数是短信发送到接收人的回调
     */
    private void sendMessage() {
        String number = mNumber.getText().toString();
        String message = mMessage.getText().toString();

        SmsManager smsManager = SmsManager.getDefault();

// 双卡支持
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
//            if (subscriptionManager != null) {
//                List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//                for (SubscriptionInfo info : activeSubscriptionInfoList) {
//                    int subId = info.getSubscriptionId();
//                    SmsManager smsManager1 = SmsManager.getSmsManagerForSubscriptionId(subId);
//                }
//            }
//        }

        Intent intentSend = new Intent(ACTION_SEND_SMS);
        PendingIntent sendIntent = PendingIntent.getBroadcast(this, 0, intentSend, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDelivery = new Intent(ACTION_DELIVERY_SMS);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0, intentDelivery, PendingIntent.FLAG_UPDATE_CURRENT);
        smsManager.sendTextMessage(number, null, message, sendIntent, deliveryIntent);
    }

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
}
