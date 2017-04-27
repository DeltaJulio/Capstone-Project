package io.github.deltajulio.pantrybank.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.github.deltajulio.pantrybank.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_REGISTRATION = 0;

    /**
     * Firebase Objects
     */
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    /**
     * View Objects
     */
    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private TextView registerButton;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        setContentView(R.layout.activity_login);
        // Set up the login form.
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.button_login);
        registerButton = (TextView) findViewById(R.id.button_register);

        // Set up the progress dialog
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));

        // setup on click listeners
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AttemptLogin();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Start the registration activity
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivityForResult(intent, REQUEST_REGISTRATION);
            }
        });

        // auto login on "enter"
        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    AttemptLogin();
                    return true;
                }
                return false;
            }
        });;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (authListener != null)
        {
            auth.removeAuthStateListener(authListener);
        }
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void AttemptLogin()
    {
        if (auth == null)
        {
            return;
        }

        /**
         * Validate login forms
         */
        // Reset errors.
        emailText.setError(null);
        passwordText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !Authentication.IsPasswordValid(password))
        {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            emailText.setError(getString(R.string.error_field_required));
            focusView = emailText;
            cancel = true;
        } else if (!Authentication.IsEmailValid(email))
        {
            emailText.setError(getString(R.string.error_invalid_email));
            focusView = emailText;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            Toast.makeText(getBaseContext(), R.string.error_login_failed, Toast.LENGTH_LONG).show();
        } else
        {
            /**
             * Try to log in with the provided credentials
             */

            loginButton.setEnabled(false);

            progressDialog.show();

            // Perform login attempt in background task
            LoginWithEmail(email, password);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_REGISTRATION && resultCode == RESULT_OK)
        {
            // Successful sign-up
            this.finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        // Disable going back to MainActivity
        moveTaskToBack(true);
    }

    public void OnLoginSuccess()
    {
        finish();
    }

    private void LoginWithEmail(String email, String password)
    {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        progressDialog.dismiss();
                        loginButton.setEnabled(true);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "LoginWithEmail:failed", task.getException());

                            Toast.makeText(LoginActivity.this, R.string.error_email_login_failed,
                                    Toast.LENGTH_LONG).show();
                        } else
                        {
                            OnLoginSuccess();
                        }
                    }
                });
    }

    private void LoginWithGoogle()
    {

    }

}

