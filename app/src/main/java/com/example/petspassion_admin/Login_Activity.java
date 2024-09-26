package com.example.petspassion_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Activity extends AppCompatActivity {
    private EditText email,password;
    private Button login;
    private CheckBox show_password;
    private TextView forgot_password;
    private FirebaseAuth firebaseAuth;
    private ALodingDialog aLodingDialog;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(ContextCompat.getColor(Login_Activity.this, R.color.adminHome));

        email = findViewById(R.id.loge_in_email);
        password = findViewById(R.id.login_password);
        login = findViewById(R.id.log_in);
        show_password = findViewById(R.id.login_password_show_hide);
        forgot_password = findViewById(R.id.forgot_password);

        aLodingDialog = new ALodingDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        handler = new Handler();






        //...................................................Show/hide password functionality............................................................................

        show_password.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else{
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        password.setTransformationMethod(show_password.isChecked()? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());





        //  ............................................Password reset .....................................................
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_Activity.this, ResetPassword_Activity.class);
                startActivity(intent);
            }
        });



        // .........................................Login admin........................................................

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateUseremail()){
                    return;
                } else if (!validatePassword()) {
                    return;
                }

                else{
                    aLodingDialog.show();
                    loginAdmin();
                    // Set a timeout of 30 seconds
                    handler.postDelayed(() -> {
                        if (aLodingDialog.isShowing()) {
                            aLodingDialog.dismiss();  // Hide the loading dialog if still showing
                            Toast.makeText(Login_Activity.this, "Login timed out. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }, 30000); // 30 seconds

                }
            }
        });


    }



    //   .............................check email edittext field is empty and validation or not.......................................

    public boolean validateUseremail(){
        String user_email = email.getText().toString();
        if(user_email.isEmpty()){
            email.requestFocus();
            email.setError("Email cannot be empty");
            return false;
        }
        // Check if the email format is valid
        else if (!user_email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !user_email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
            email.requestFocus();
            email.setError("Invalid email format");
            return false;
        }
        else {
            email.setError(null);
            return true;
        }
    }



    //   .............................check password edittext field is empty and validation or not.......................................

    public Boolean validatePassword() {
        String pass = password.getText().toString();

        if (pass.isEmpty()) {
            password.requestFocus();
            password.setError("Password cannot be empty");
            return false;
        }

        else if (!pass.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,16}$")) {
            password.requestFocus();
            password.setError("Password must be between 6 to 16 characters long, " + "include at least one digit, one lowercase letter, " + "one uppercase letter, and one special character.");
            return false;
        }
        else {
            password.setError(null);
            return true;
        }
    }



    //  .........................admin login functionality...............................................

    public void loginAdmin(){
        String admin_email = email.getText().toString().trim();
        String admin_password = password.getText().toString().trim();
        firebaseAuth.signInWithEmailAndPassword(admin_email,admin_password).addOnCompleteListener(task -> {
            handler.removeCallbacksAndMessages(null);
            aLodingDialog.dismiss();

            if(task.isSuccessful()){
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    String userEmail = firebaseUser.getEmail();
                    if("nazrulislamnayon991@gmail.com".equalsIgnoreCase(userEmail)){
                        Intent intent = new Intent(Login_Activity.this, AdminHome_Activity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(Login_Activity.this,"Please try again",Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                    }
                }
                else{
                    Toast.makeText(Login_Activity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();

                }
            }


        }).addOnFailureListener(e -> {
            handler.removeCallbacksAndMessages(null);
            aLodingDialog.dismiss();
            Toast.makeText(Login_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();  // Show error message
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);  // Remove any pending timeout callbacks on activity destroy
    }
}