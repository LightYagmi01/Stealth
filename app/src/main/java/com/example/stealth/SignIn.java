package com.example.stealth;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {
    TextView signup, txtForgetPass;
    Button btnSignIn;
    EditText edtEmail, edtPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null && currentUser.isEmailVerified()){
            if(BackendCommon.UserId==null) {
                BackendCommon.UserId = currentUser.getUid();
                BackendCommon.postsManager = new PostsManager(BackendCommon.UserId);
                BackendCommon.pollManager = new PollManager(BackendCommon.UserId);
                BackendCommon.myPosts = new MyPosts(BackendCommon.UserId);
                BackendCommon.myPoll = new MyPoll(BackendCommon.UserId);
            }
            Intent var = new Intent(SignIn.this, MainActivity.class);
            startActivity(var);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



//        networkMonitor = new NetworkMonitor(this);
//        networkMonitor.startMonitoring();

        signup = findViewById(R.id.txtSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        progressBar = findViewById(R.id.progressBar);
        txtForgetPass = findViewById(R.id.txtForgetPass);

        signup.setPaintFlags(signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtForgetPass.setPaintFlags(txtForgetPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent var = new Intent(SignIn.this, SignUp.class);
                startActivity(var);
                finish();
            }
        });

        txtForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent var = new Intent(SignIn.this, ForgotPassword.class);
                startActivity(var);
                edtEmail.setText("");
                edtPass.setText("");
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, confirmPass;
                email = edtEmail.getText().toString();
                password = edtPass.getText().toString();
                btnSignIn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                if (!validateCredentials(email, password)) {
                    btnSignIn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        if (user.isEmailVerified()) {
                                            // Email is verified, proceed with your app logic
                                            // For example, navigate to the main activity
                                            BackendCommon.UserId=user.getUid();
                                            BackendCommon.postsManager=new PostsManager(BackendCommon.UserId);
                                            BackendCommon.pollManager=new PollManager(BackendCommon.UserId);
                                            BackendCommon.myPosts=new MyPosts(BackendCommon.UserId);
                                            BackendCommon.myPoll=new MyPoll(BackendCommon.UserId);
                                            startActivity(new Intent(SignIn.this, MainActivity.class));
                                            finish();
                                        } else {
                                            btnSignIn.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                            // Email is not verified, display a message to the user
                                            LayoutInflater inflater = LayoutInflater.from(SignIn.this);
                                            View customView = inflater.inflate(R.layout.custom_alert_dialog, null);
                                            TextView titleTextView = customView.findViewById(R.id.dialog_title);
                                            TextView messageTextView = customView.findViewById(R.id.dialog_message);
                                            Button button = customView.findViewById(R.id.dialog_button);
                                            titleTextView.setText("Reminder");
                                            messageTextView.setText("Please verify your email before logging in.");
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
                                            builder.setView(customView);
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.setCanceledOnTouchOutside(false);
                                            alertDialog.show();
                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                    edtEmail.setText(null);
                                                    edtPass.setText(null);
                                                }
                                            });
                                            Toast.makeText(SignIn.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Exception exception = task.getException();
                                    if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                        // If the password is incorrect, display appropriate message
                                        edtEmail.setError("Incorrect email/password.");
                                        edtPass.setError("Incorrect email/password.");
                                    } else {
                                        // Other authentication failures
                                        Toast.makeText(SignIn.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    btnSignIn.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });

            }
        });


    }
    private boolean isValidEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    // Function to validate email and password
    private boolean validateCredentials(String email, String password) {
        boolean chk = true;
        if (!isValidEmail(email)) {
            edtEmail.setError("Email is Invalid.");
            chk = false;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is Required.");
            chk = false;
        }
        if (TextUtils.isEmpty(password)) {
            edtPass.setError("Password is Required.");
            chk = false;
        }
        return chk;
    }
}