package com.example.campusl2k2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPageActivity extends Activity {
    private Button changeDisplayNameButton;
    private EditText changeDisplayText;
    private String disName;
    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    FirebaseUser current_user=firebaseAuth.getCurrentUser();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        setVal();
        setEvents();

        disName=getIntent().getStringExtra("disName");
        changeDisplayText.setText(disName);
    }
    private void setVal(){
        changeDisplayText=findViewById(R.id.changeDisplayName_EditText);
        changeDisplayNameButton=findViewById(R.id.changeDisplayName_Button);
    }
    private void setEvents(){
        changeDisplayNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chDisplayName();
            }
        });
    }
    public void chDisplayName(){
        final String targetName=changeDisplayText.getText().toString();
        DocumentReference reference=firebaseFirestore.collection("users").document(current_user.getUid());
        reference.update("disPlayName",targetName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        UIdisplay.showMessage(MyPageActivity.this,"대화명을 변경하였습니다.");
                        Intent intent=new Intent();
                        intent.putExtra("newName",targetName);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        UIdisplay.showMessage(MyPageActivity.this,"대화명을 변경하는데 실패하였습니다.");
                    }
                });
    }
}
