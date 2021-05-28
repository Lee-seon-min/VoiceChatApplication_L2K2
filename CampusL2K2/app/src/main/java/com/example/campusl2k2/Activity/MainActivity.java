package com.example.campusl2k2.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.Adapter.RoomListAdapter;
import com.example.campusl2k2.Data.DatabaseRoomDetails;
import com.example.campusl2k2.Data.RoomData;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Service.ConnectionService;
import com.example.campusl2k2.Structure.Pair;
import com.example.campusl2k2.Util.UIdisplay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements RoomListAdapter.IThrowable {
    private String sipID,sipPw,disName;
    private Button createRoom,showDrawerBtn;
    private TextView displayName;
    private List<RoomData> RoomList=new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private ListenerRegistration registration;
    private RoomListAdapter roomListAdapter;
    private RecyclerView recyclerView;
    private ListView menuListView;
    private DrawerLayout drawerLayout;
    private View menuView;
    private AlertDialog alertDialog;
    static final String[] LIST_VAL={"마이페이지","캘린더","로그아웃"};
    public static int MAKEROOM_REQUESTCODE=100;
    public static int MYPAGE_UPDATE_NAME=101;


    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck_sip = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_SIP);
        int permissionCheck_audio=ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permissionCheck_sip != PackageManager.PERMISSION_GRANTED || permissionCheck_audio!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_SIP,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.RECORD_AUDIO},1);
        }

        setVal();
        setEvents();

        sipID=getIntent().getStringExtra("sipID");
        sipPw=getIntent().getStringExtra("sipPW");
        disName=getIntent().getStringExtra("disName");
        String hello=disName+"님! 안녕하세요!";
        displayName.setText(hello);

        ArrayAdapter adapter=new ArrayAdapter(MainActivity.this, android.R.layout.simple_expandable_list_item_1,LIST_VAL);
        menuListView.setAdapter(adapter);

        roomListAdapter=new RoomListAdapter(this,RoomList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(roomListAdapter);

        CollectionReference collectionReference=firebaseFirestore.collection("rooms");
        registration = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null || value==null){ //에러
                    return;
                }
                else{
                    RoomList.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        try {
                            String roomName = snapshot.getData().get("roomName").toString();
                            String roomId = snapshot.getId();
                            String makerId = snapshot.getData().get("maker").toString();
                            RoomList.add(new RoomData(roomName, roomId, makerId));
                        }
                        catch (NullPointerException e){
                            return;
                        }
                    }
                    roomListAdapter.updateList(RoomList);
                    roomListAdapter.notifyDataSetChanged();
                }
            }
        });

        if(!ConnectionService.isReady()){
            UIdisplay.showMessage(MainActivity.this,"서비스를 시작합니다.");
            startService(new Intent(MainActivity.this, ConnectionService.class));
        }

    }
    public void setVal(){
        drawerLayout=findViewById(R.id.drawerLayout);
        menuView=findViewById(R.id.drawer);
        showDrawerBtn=findViewById(R.id.showDrawer_Button);
        createRoom=findViewById(R.id.createRoom);
        displayName=findViewById(R.id.myDisplayName);
        recyclerView=findViewById(R.id.roomListRecyclerView);
        menuListView=findViewById(R.id.menuListView);
        alertDialog=getAlertDialog();
        firebaseFirestore=FirebaseFirestore.getInstance();
    }
    public void setEvents(){
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, RoomNameDecisionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent,MAKEROOM_REQUESTCODE);
            }
        });
        showDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(menuView);
            }
        });
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:{ //마이페이지
                        Intent intent=new Intent(MainActivity.this,MyPageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("disName",disName);
                        startActivityForResult(intent,MYPAGE_UPDATE_NAME);
                        break;
                    }
                    case 1:{ //캘린더
                        Intent intent=new Intent(MainActivity.this,CalendarActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;
                    }
                    case 2:{ //로그아웃
                        alertDialog.show();
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MAKEROOM_REQUESTCODE){
            if(resultCode== RoomNameDecisionActivity.MAKEROOM_RESCODE){
                final String rname = data.getStringExtra("targetRoomName");
                DatabaseRoomDetails details=new DatabaseRoomDetails(rname,new ArrayList<Pair>(),sipID);
                final DocumentReference reference = firebaseFirestore.collection("rooms").document();
                        reference.set(details)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                UIdisplay.showMessage(MainActivity.this,"방을 생성하였습니다.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                UIdisplay.showMessage(MainActivity.this,"방생성 오류");
                            }
                        });

            }
        }
        else if(requestCode==MYPAGE_UPDATE_NAME && resultCode==RESULT_OK){
            disName = data.getStringExtra("newName");
            String hello=disName+"님! 안녕하세요!";
            displayName.setText(hello);
        }

    }

    @Override
    public void callBack(final String roomID,final String roomName,final String maker) {
        final DocumentReference reference = firebaseFirestore.collection("rooms").document(roomID);
        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        boolean flag=true;
                        ArrayList<Pair> pairs=new ArrayList<>();
                        for(Object key:((ArrayList)documentSnapshot.getData().get("participants"))){
                            Map<String, String> map=((HashMap)key);
                            ArrayList<String> temp=new ArrayList<>();
                            for(Object info:map.keySet()){
                                temp.add(info.toString()); //disName, sid
                            }
                            if(temp.get(0).equals("disName")){
                                pairs.add(new Pair(map.get(temp.get(1)),map.get(temp.get(0)))); //참가자의 세션id와 닉네임
                            }
                            else{
                                pairs.add(new Pair(map.get(temp.get(0)),map.get(temp.get(1)))); //참가자의 세션id와 닉네임
                            }
                        }
                        for(Pair p:pairs){
                            if(p.getSid().equals(sipID)){
                                flag=false;
                                return;
                            }
                        }
                        if(!flag)
                            return; //이미 방에 참가되어있으면 못 들어가게끔 함
                        pairs.add(new Pair(sipID,disName));
                        DocumentReference reference1=firebaseFirestore.collection("rooms").document(roomID);
                        reference1.update("participants",pairs)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent=new Intent(MainActivity.this,VoiceChatActivity.class);
                                        //Intent intent=new Intent(MainActivity.this,LauncherActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.putExtra("thisRoomId",roomID);
                                        intent.putExtra("mySessionID",sipID);
                                        intent.putExtra("mySessionPW",sipPw);
                                        intent.putExtra("myDisplayName",disName);
                                        intent.putExtra("thisRoomName",roomName);
                                        intent.putExtra("makerSipId",maker);
                                        startActivity(intent);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        UIdisplay.showMessage(MainActivity.this,"참가자의 정보를 가져오는데 실패하였습니다.");
                    }
                });



    }
    public AlertDialog getAlertDialog(){ //저장하기 버튼 누를시, 안내문
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("로그아웃 알림");
        builder.setMessage("로그아웃 하시겠습니까?");
        builder.setIcon(getResources().getDrawable(R.drawable.ic_baseline_campaign_24));
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return; //그냥 종료
            }
        });
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() { //데이터베이스에 저장
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        return builder.create();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(menuView)){
            drawerLayout.closeDrawer(menuView);
            return;
        }
        alertDialog.show();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!ConnectionService.isReady()){
            UIdisplay.showMessage(MainActivity.this,"서비스를 시작합니다.");
            startService(new Intent(MainActivity.this, ConnectionService.class));
        }
    }
}
