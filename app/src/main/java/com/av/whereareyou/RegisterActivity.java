package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends Activity {

    @BindView(R.id.etFirstName)
    EditText editFirstName;
    @BindView(R.id.etLastName)
    EditText editLastName;
    @BindView(R.id.etEmail)
    EditText editTextEmail;
    @BindView(R.id.etPassword)
    EditText editTextPassword;
    @BindView(R.id.etRepeatPassword)
    EditText editRepeatPassword;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_signup)
    public void onSignUp(View view) {
        final String fName = editFirstName.getText().toString().trim();
        final String lName = editLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repeatPassword = editRepeatPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(getApplicationContext(), "Enter first name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(lName)) {
            Toast.makeText(getApplicationContext(), "Enter last name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repeatPassword)) {
            Toast.makeText(getApplicationContext(), "Confirm password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //create user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);

                        if (!task.isSuccessful()) {
                            Log.d("Authentication failed", task.getException().toString());
                            Toast.makeText(RegisterActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "You are registered successfully", Toast.LENGTH_SHORT).show();

                            String email = firebaseAuth.getCurrentUser().getEmail();
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference userDatabase = mDatabase.child("users").child(userId);

                            userDatabase.child("email").setValue(email);
                            userDatabase.child("firstName").setValue(fName);
                            userDatabase.child("lastName").setValue(lName);

                            Intent intent = new Intent(RegisterActivity.this, IntroActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @OnClick(R.id.btn_signin)
    public void onSignIn(View view) {
        finish();
    }
}
