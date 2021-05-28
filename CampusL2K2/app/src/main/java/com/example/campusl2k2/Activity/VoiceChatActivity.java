package com.example.campusl2k2.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.Adapter.ParticipantsListAdapter;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Service.ConnectionService;
import com.example.campusl2k2.Structure.Pair;
import com.example.campusl2k2.Util.AudioReader;
import com.example.campusl2k2.Util.UIdisplay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Conference;
import org.linphone.core.ConferenceParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VoiceChatActivity extends Activity {
    private RecyclerView recyclerView;
    private TextView roomName,tellerShower;
    private EditText displayName;
    private Button hangUpButton,initiateCallButton,speakerButton,fixDisplayName;
    private ArrayList<String> disList,savePartsSid;
    private String thisRoomId,thisRoomName,sid,disName,sipPw,maker;
    public AudioManager am;

    private AudioReader audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;

    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private ListenerRegistration registration;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;

   private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() { //버튼 비활성화
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if(message.equals("ACK")){
                if(initiateCallButton!=null && initiateCallButton.getVisibility()!=View.GONE) {
                    initiateCallButton.setEnabled(false);
                    hangUpButton.setEnabled(false);
                }
                recMicStart(); //화자 분석시작.
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicechat);
        thisRoomId=getIntent().getStringExtra("thisRoomId");
        sid=getIntent().getStringExtra("mySessionID");
        sipPw=getIntent().getStringExtra("mySessionPW");
        disName=getIntent().getStringExtra("myDisplayName");
        thisRoomName=getIntent().getStringExtra("thisRoomName");
        maker=getIntent().getStringExtra("makerSipId");

        int permissionCheck_audio= ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck_audio!=PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1
                );
            }
        }

        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setVal(); //객체매칭
        setEvents(); //버튼 등의 이벤트 설정

        tellerShower.setText("");

        roomName.setText(thisRoomName);;//방이름 설정
        if(!maker.equals(sid)){
            initiateCallButton.setVisibility(View.GONE);
        }

        if(am.isSpeakerphoneOn()){
            am.setMode(AudioManager.MODE_IN_CALL);
            am.setSpeakerphoneOn(false);
        }
        startSipStack(); //SIP세션 인증


        ParticipantsListAdapter participantsListAdapter=new ParticipantsListAdapter(disList);
        RecyclerView.LayoutManager layoutManager=new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(participantsListAdapter);


        DocumentReference reference=firebaseFirestore.collection("rooms").document(thisRoomId);
        registration = reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null || value==null || value.getData()==null){
                    return;
                }
                try {
                    String teller = value.getData().get("teller").toString();
                    String teller_text = teller + "님이 말하고 있습니다.";
                    tellerShower.setText(teller_text);

                    disList.clear();
                    savePartsSid.clear();
                    for (Object key : ((ArrayList) value.getData().get("participants"))) {
                        Map<String, String> map = ((HashMap) key);
                        ArrayList<String> temp = new ArrayList<>();
                        for (Object info : map.keySet()) {
                            temp.add(info.toString()); //disName, sid
                        }
                        if(temp.get(0).equals("disName")){
                            disList.add(map.get(temp.get(0))); //참가자의 닉네임
                            String tSid = map.get(temp.get(1));
                            if (!tSid.equals(sid)) { //나를 제외한 사람들의 세션아이디
                                savePartsSid.add(tSid); //들어온 사람의 세션아이디 저장
                            }
                        }
                        else{ //temp.get(1)이 disName
                            disList.add(map.get(temp.get(1))); //참가자의 닉네임
                            String tSid = map.get(temp.get(0));
                            if (!tSid.equals(sid)) { //나를 제외한 사람들의 세션아이디
                                savePartsSid.add(tSid); //들어온 사람의 세션아이디 저장
                            }
                        }
                    }
                    if (savePartsSid.size() == 0 && !hangUpButton.isEnabled()) {
                        hangUpButton.setEnabled(true);
                        audioReader.stopReader();
                    }
                    ParticipantsListAdapter participantsListAdapter = new ParticipantsListAdapter(disList);
                    recyclerView.setAdapter(participantsListAdapter);
                    participantsListAdapter.notifyDataSetChanged();
                }
                catch(NullPointerException e){
                    return;
                }
            }
        });
    }
    public void setVal(){
        roomName=findViewById(R.id.thisRoomName);
        tellerShower=findViewById(R.id.teller_TextView);
        initiateCallButton=findViewById(R.id.initiateCallButton);
        initiateCallButton.setEnabled(true);
        speakerButton=findViewById(R.id.SpeakerModeButton);
        recyclerView=findViewById(R.id.chattingRoomList);
        hangUpButton=findViewById(R.id.hangUpButton);
        fixDisplayName=findViewById(R.id.fixDisplayName_Button);
        displayName=findViewById(R.id.fixDisplayName_EditText);
        disList=new ArrayList<>();
        savePartsSid=new ArrayList<>();
        am=(AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mAccountCreator = ConnectionService.getCore().createAccountCreator(null);
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    UIdisplay.showMessage(VoiceChatActivity.this, "서버 연결 성공!");
                    ConnectionService.getCore().removeListener(mCoreListener); //한번만 실행
                }
            }
        };
    }
    public void setEvents(){
        fixDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference reference = firebaseFirestore.collection("rooms").document(thisRoomId);
                reference.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                ArrayList<Pair> pairs=new ArrayList<>();
                                String newName=displayName.getText().toString();
                                for(Object key:((ArrayList)documentSnapshot.getData().get("participants"))){
                                    Map<String, String> map=((HashMap)key);
                                    ArrayList<String> temp=new ArrayList<>();
                                    for(Object info:map.keySet()){
                                        temp.add(info.toString()); //disName, sid
                                    }
                                    if(temp.get(0).equals("disName")){
                                        if(map.get(temp.get(0)).equals(newName)){ //변경하고자 하는 대화명이 이미 존재하면,
                                            UIdisplay.showMessage(VoiceChatActivity.this,"이미 존재하는 대화명입니다.");
                                            return;
                                        }
                                        if(disName.equals(map.get(temp.get(0)))&&sid.equals(map.get(temp.get(1)))){ //바꾸고자하는 대화명의 대상이라면,
                                            pairs.add(new Pair(map.get(temp.get(1)),newName));
                                            UIdisplay.showMessage(VoiceChatActivity.this,newName);
                                        }
                                        else {
                                            pairs.add(new Pair(map.get(temp.get(1)), map.get(temp.get(0)))); //참가자의 세션id와 닉네임
                                        }
                                    }
                                    else{ //temp.get(1)이 disName
                                        if(map.get(temp.get(1)).equals(newName)){ //변경하고자 하는 대화명이 이미 존재하면,
                                            UIdisplay.showMessage(VoiceChatActivity.this,"이미 존재하는 대화명입니다.");
                                            return;
                                        }
                                        if(disName.equals(map.get(temp.get(1)))&&sid.equals(map.get(temp.get(0)))){ //바꾸고자하는 대화명의 대상이라면,
                                            pairs.add(new Pair(map.get(temp.get(0)),newName));
                                            UIdisplay.showMessage(VoiceChatActivity.this,newName);
                                        }
                                        else {
                                            pairs.add(new Pair(map.get(temp.get(0)), map.get(temp.get(1)))); //참가자의 세션id와 닉네임
                                        }
                                    }
                                }
                                DocumentReference reference1=firebaseFirestore.collection("rooms").document(thisRoomId);
                                reference1.update("participants",pairs);
                                disName=newName;
                                displayName.setText("");
                                UIdisplay.showMessage(VoiceChatActivity.this,"대화명을 변경하였습니다.");
                            }
                        });
            }
        });
        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                am.setMode(AudioManager.MODE_IN_CALL);
                if(am.isSpeakerphoneOn()){
                    am.setSpeakerphoneOn(false);
                }
                else{
                    am.setSpeakerphoneOn(true);
                }
            }
        });
        initiateCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateCall();
            }
        });
        hangUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               hangUp();
            }
        });
    }
    public void startSipStack(){
        mAccountCreator.setUsername(sid);
        mAccountCreator.setDomain("xxx.xxx.xx.xx:xxxx");
        mAccountCreator.setPassword(sipPw);
        mAccountCreator.setTransport(TransportType.Udp);

        // This will automatically create the proxy config and auth info and add them to the Core
        ProxyConfig cfg = mAccountCreator.createProxyConfig(); //proxyConfig
        ConnectionService.getCore().setDefaultProxyConfig(cfg); //signing Address

        //Make a conference that has multiple calls (Need a test)
        ConferenceParams conferenceParams= ConnectionService.getCore().createConferenceParams();
        conferenceParams.enableLocalParticipant(true);
        ConnectionService.getCore().createConferenceWithParams(conferenceParams);

    }
    public void hangUp(){
        audioReader.stopReader();
        final ArrayList<Pair> pairs=new ArrayList<>();
        firebaseFirestore.collection("rooms").document(thisRoomId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        for(Object key:((ArrayList)documentSnapshot.getData().get("participants"))){
                            Map<String, String> map=((HashMap)key);
                            ArrayList<String> temp=new ArrayList<>();
                            for(Object info:map.keySet()){
                                temp.add(info.toString()); //disName, sid
                            }
                            if(temp.get(0).equals("disName")){
                                if(map.get(temp.get(0)).equals(disName) && map.get(temp.get(1)).equals(sid)){
                                    continue;
                                }
                                pairs.add(new Pair(map.get(temp.get(1)),map.get(temp.get(0))));
                            }
                            else{
                                if(map.get(temp.get(1)).equals(disName) && map.get(temp.get(0)).equals(sid)){
                                    continue;
                                }
                                pairs.add(new Pair(map.get(temp.get(0)),map.get(temp.get(1))));
                            }
                        }
                        firebaseFirestore.collection("rooms").document(thisRoomId)
                                .update("participants",pairs).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Core core = ConnectionService.getCore();
                                if (core.getCallsNb() > 0) {//통화중,

                                    Call call = core.getCurrentCall();
                                    if (call == null) {
                                        // Current call can be null if paused for example
                                        call = core.getCalls()[0];
                                    }
                                    call.terminate(); //자신만 끊는다.

                                }
                                setResult(201);
                                finish();
                            }
                        });
                    }
                });
    }
    public void initiateCall(){
        //ArrayList<Address> addresses=new ArrayList<>();
        Core core = ConnectionService.getCore();
        CallParams params=core.createCallParams(null);
        //ConferenceParams params = core.createConferenceParams();
        //params.enableLocalParticipant(true);
        Conference conference=core.getConference();
        //String string="";
        for(String peer:savePartsSid) {
            Address address=core.interpretUrl(peer);
            conference.inviteParticipants(new Address[]{address},params);
            //addresses.add(core.interpretUrl(peer));
            //string+=peer+" ";
        }
        //UIdisplay.showMessage(VoiceChatActivity.this,string);
        //Address[] part_addresses=addresses.toArray(new Address[0]);
        //CallParams callParams=core.createCallParams(null);
                //To have a group that for VoiceTalking (Need a test)
        //conference.inviteParticipants(part_addresses,callParams);
                //core.getConference().inviteParticipants(addresses,params); //음성채팅 초대?
        //core.inviteAddressWithParams(addressToCall, params);

    }

    public void recMicStart()
    {
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB)
            {
                if(dB>-30) {
                    DocumentReference reference = firebaseFirestore.collection("rooms").document(thisRoomId);
                    reference.update("teller", disName);
                }
            }

            @Override
            public void onReadError(int error)
            {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionService.getCore().addListener(mCoreListener);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("connection_Recognizer"));
    }
    @Override
    protected void onPause() {
        ConnectionService.getCore().removeListener(mCoreListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ConnectionService.getCore().removeListener(mCoreListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}