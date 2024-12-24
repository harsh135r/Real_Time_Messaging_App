package com.example.realtimemessaging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realtimemessaging.utils.AndroidUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String phoneNumber;
    long timeoutseconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    EditText otpinput;
    Button nextbutton;
    ProgressBar progressBar;
    TextView resendotp;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otpinput = findViewById(R.id.login_otp);
        nextbutton = findViewById(R.id.login_verifybutton);
        progressBar = findViewById(R.id.login_progressbar);
        resendotp = findViewById(R.id.resend_otp_textview);

        phoneNumber = getIntent().getExtras().getString("phone");

        // Check if Google Play Services is available
        if (checkPlayServices()) {
            sendOtp(phoneNumber, false);
        }
        resendotp.setOnClickListener((v)->{
            sendOtp(phoneNumber,true);
        });
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        startResendTimer();
        setInProgress(true);

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);//for test phone numbers

        // sending otp
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeoutseconds, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signIn(phoneAuthCredential);
                                setInProgress(false);
                                AndroidUtil.showToast(getApplicationContext(), "OTP Verification passed");
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                // chat-gpt generated to check error

                                Log.e("FirebaseAuth", "Verification failed", e);

                                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                    AndroidUtil.showToast(getApplicationContext(), "Invalid phone number format");
                                    Log.e("FirebaseAuth", "Invalid phone number format.");
                                }
                                else if (e instanceof FirebaseTooManyRequestsException) {
                                    AndroidUtil.showToast(getApplicationContext(), "Quota exceeded for this project");
                                    Log.e("FirebaseAuth", "Quota exceeded for this project.");
                                }
                                else if (e instanceof FirebaseNetworkException) {
                                    AndroidUtil.showToast(getApplicationContext(), "Network error. Please check your connection.");
                                    Log.e("FirebaseAuth", "Network error. Please check your connection.");
                                }
                                else {
                                    AndroidUtil.showToast(getApplicationContext(), "Unknown error: " + e.getMessage());
                                    Log.e("FirebaseAuth", "Unknown error: " + e.getMessage());
                                }
                                setInProgress(false);
                            }

                            // it should run even with physical phone number
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verificationCode = s;
                                resendingToken = forceResendingToken;
                                AndroidUtil.showToast(getApplicationContext(), "OTP sent successfully ");
                                setInProgress(false);

                                nextbutton.setOnClickListener((v)->{
                                    String enteredOtp= otpinput.getText().toString();
                                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
                                    signIn(credential);
                                });
                            }
                        });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            nextbutton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            nextbutton.setVisibility(View.VISIBLE);
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential) {
        // Login and go to the next activity
            // Use the PhoneAuthCredential to sign in with Firebase Authentication
        setInProgress(true);
            mAuth.signInWithCredential(phoneAuthCredential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // OTP Verification successful, proceed to the next activity
                            FirebaseUser user = task.getResult().getUser();
                            AndroidUtil.showToast(getApplicationContext(), "OTP verification successful!");

                            // You can now proceed with the logged-in user, for example, navigate to the next activity
                             Intent intent = new Intent(LoginOtpActivity.this, LoginUsernameActivity.class);
                            intent.putExtra("phone",phoneNumber);
                            AndroidUtil.showToast(getApplicationContext(), "new activity will open");
                            startActivity(intent);
                            finish();  // Optional: finish the current activity if you don't need to return back

                        } else {
                            // OTP verification failed
                            AndroidUtil.showToast(getApplicationContext(), "OTP verification failed. Please try again.");
                            Log.e("FirebaseAuth", "Verification failed", task.getException());
                        }
                    });

    }

    void startResendTimer() {
        resendotp.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutseconds--;
                resendotp.setText("Resend OTP in " + timeoutseconds + " seconds");
                if (timeoutseconds <= 0) {
                    timeoutseconds = 60L;
                    resendotp.setText(" click to Resend OTP");
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendotp.setEnabled(true);
                    });
                }
            }
        }, 0, 1000);
    }

    /**
     * Check if Google Play Services is installed and updated
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
}
