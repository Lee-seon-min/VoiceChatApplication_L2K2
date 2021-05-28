package com.example.campusl2k2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends Activity {
    EditText id,pw;
    Button login,chSignUpPage;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout loader;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setVal();
        setEvents();
    }
    private void setVal(){
        firebaseAuth= FirebaseAuth.getInstance();
        id=findViewById(R.id.idEditText_loginActivity);
        pw=findViewById(R.id.pwEditText_loginActivity);
        login=findViewById(R.id.loginButton_loginActivity);
        chSignUpPage=findViewById(R.id.signUpButton_loginActivity);
        loader=findViewById(R.id.viewLoader);
    }
    private void setEvents(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = null;
                if(id.getText().toString().length()<1 || pw.getText().toString().length()<1){
                    UIdisplay.showMessage(LoginActivity.this,"ID 또는 PW를 입력해주세요.");
                }
                else{
                    Cursor cursor=getContentResolver().query(DataHandler.CONTENT_URI_AUTH,new String[]{"email"},null,null,null); //해당 URI와 매칭하며, 해당하는 테이블에서 조작한다.
                    if(cursor!=null) {
                        while (cursor.moveToNext()) { //한 행씩 받아옴
                            email = cursor.getString(0); //0번째 컬럼
                        }
                    }
                    if(email==null){
                        UIdisplay.showMessage(LoginActivity.this,"먼저 기기에 계정을 등록하세요.");
                        return;
                    }
                    else if(email!=null && !email.equals(id.getText().toString())){
                        //기기에 등록된 계정과 아이디가 일치하지 않는다면,
                        UIdisplay.showMessage(LoginActivity.this,"check your ID or PW");
                        return;
                    }
                    loader.setVisibility(View.VISIBLE);
                    startLogin();
                }
            }
        });
        chSignUpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag=false;
                Cursor cursor=getContentResolver().query(DataHandler.CONTENT_URI_AUTH,new String[]{"email"},null,null,null); //해당 URI와 매칭하며, 해당하는 테이블에서 조작한다.
                if(cursor!=null) {
                    while (cursor.moveToNext()) { //한 행씩 받아옴
                        flag = true; //이미 계정이 존재한다.
                    }
                }
                if(cursor==null || !flag){
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                else{
                    UIdisplay.showMessage(LoginActivity.this,"이미 이 기기에 등록된 계정이 존재합니다.");
                    loader.setVisibility(View.GONE);
                }
            }
        });
    }
    private void startLogin(){
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(),pw.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        final Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                        FirebaseUser user=firebaseAuth.getCurrentUser();
                        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
                        firestore.collection("users").document(user.getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        intent.putExtra("sipID",documentSnapshot.getData().get("sipSessionID").toString());
                                        intent.putExtra("sipPW",documentSnapshot.getData().get("sipSessionPW").toString());
                                        intent.putExtra("disName",documentSnapshot.getData().get("disPlayName").toString());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        loader.setVisibility(View.GONE);
                                        finish();
                                    }
                                });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        UIdisplay.showMessage(LoginActivity.this,"check your ID or PW");
                        loader.setVisibility(View.GONE);
                    }
                });
    }
}
