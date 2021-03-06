package com.example.campusl2k2.Activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campusl2k2.Data.Member;
import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends Activity {
    private EditText id,pw,chpw,disName;
    private Button signUp;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout loader;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setVal();
        setEvents();
    }
    private void setVal(){
        id=findViewById(R.id.idEditText_signUpActivity);
        pw=findViewById(R.id.pwEditText_signUpActivity);
        chpw=findViewById(R.id.chkPwEditText_signUpActivity);
        disName=findViewById(R.id.displayNameEditText_signUpActivity);
        signUp=findViewById(R.id.signUpButton_signUpActivity);
        firebaseAuth=FirebaseAuth.getInstance();
        loader=findViewById(R.id.viewLoader);
    }
    private void setEvents(){
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.getText().toString().length()<1 || pw.getText().toString().length()<1){
                    UIdisplay.showMessage(SignUpActivity.this,"ID?????? PW??? ??????????????????.");
                    return;
                }
                else{
                    if(pw.getText().toString().equals(chpw.getText().toString())){
                        loader.setVisibility(View.VISIBLE);
                        trySignUp();
                    }
                    else{
                        UIdisplay.showMessage(SignUpActivity.this,"PW???????????? ???????????? ????????????.");
                    }
                }
            }
        });
    }
    private void trySignUp() {
        firebaseAuth.createUserWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser(); //????????? ??????
                        final FirebaseFirestore firestore = FirebaseFirestore.getInstance(); //?????????????????? ??????
                        CollectionReference collectionReference=firestore.collection("users"); //users ?????????
                        collectionReference.get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        int s = queryDocumentSnapshots.size()+1; //?????????+1
                                        Member member=new Member(s+"000",s+"000",disName.getText().toString());
                                        firestore.collection("users").document(user.getUid()).set(member)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        firebaseAuth.signOut();

                                                        ContentValues values=new ContentValues();
                                                        values.put("email",id.getText().toString());
                                                        Uri uri=getContentResolver().insert(DataHandler.CONTENT_URI_AUTH,values);

                                                        Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                        startActivity(intent);
                                                        loader.setVisibility(View.GONE);

                                                        UIdisplay.showMessage(SignUpActivity.this,"???????????? ??????");
                                                        UIdisplay.showMessage(SignUpActivity.this,"????????? ????????? ?????????????????????.");

                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loader.setVisibility(View.GONE);
                        UIdisplay.showMessage(SignUpActivity.this,"??????????????? ?????????????????????.");
                    }
                });
    }
}
