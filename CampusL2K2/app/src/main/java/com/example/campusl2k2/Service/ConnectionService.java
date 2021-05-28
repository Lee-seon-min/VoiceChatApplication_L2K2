package com.example.campusl2k2.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionService extends Service {
    private static ConnectionService sInstance;
    private Handler mHandler;
    private Timer mTimer;
    private Core mCore;
    private CoreListenerStub mCoreListener;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));

        mHandler = new Handler();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {

                if (state == Call.State.IncomingReceived) {
                    UIdisplay.showMessage(ConnectionService.this,"회의에 연결중입니다...");
                    CallParams params = getCore().createCallParams(call);
                    params.enableVideo(false);
                    call.acceptWithParams(params);
                }
                else if(state==Call.State.Connected){
                    UIdisplay.showMessage(ConnectionService.this,"회의가 연결되었습니다!");
                    sendMessage();
                    call.setSpeakerVolumeGain(100);
                }
            }
        };


        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        configureCore();
    }
    private void sendMessage() {
        Intent intent = new Intent("connection_Recognizer");
        intent.putExtra("message", "ACK");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (sInstance != null) {
            return START_STICKY;
        }

        sInstance = this;
        mCore.start();
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCore != null) {
                                            mCore.iterate();
                                        }
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        if(mTimer!=null){
            mTimer.cancel();
        }
        mCore.stop();
        mCore = null;
        sInstance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.i("creation",userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

}
