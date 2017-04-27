package io.github.deltajulio.pantrybank.auth;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.github.deltajulio.pantrybank.R;

public class RegistrationActivity extends AppCompatActivity
{
    private static final String TAG = "RegistrationActivity";

    /**
     * Firebase Objects
     */
    private FirebaseAuth auth;

    /**
     * View Objects
     */
    private EditText emailText;
    private EditText passwordText;
    private Button registerButton;
    private TextView loginButton;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_registration);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        registerButton = (Button) findViewById(R.id.button_register);
        loginButton = (TextView) findViewById(R.id.button_login);

        // Set up progress dialog
        progressDialog = new ProgressDialog(RegistrationActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getText(R.string.authenticating));

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AttemptRegistration();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Return to LoginActivity
                finish();
            }
        });
    }

    /**
     * Attempts to register the account specified by the registration form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual registration attempt is made.
     */
    private void AttemptRegistration()
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
            emailText.setError(getText(R.string.error_field_required));
            focusView = emailText;
            cancel = true;
        } else if (!Authentication.IsEmailValid(email))
        {
            emailText.setError(getText(R.string.error_invalid_email));;
        }

        if (cancel)
        {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
            Toast.makeText(getBaseContext(), R.string.error_login_failed, Toast.LENGTH_LONG).show();
        } else
        {
            /**
             * Try to register the provided credentials
             */

            registerButton.setEnabled(false);

            progressDialog.show();

            // Perform login attempt in background task
            RegisterWithEmail(email, password);

            new android.os.Handler().postDelayed(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // On complete call either OnRegistrationSuccess or OnRegistrationFailed
                            OnRegistrationSuccess();
                            // OnRegistrationFailed();
                            progressDialog.dismiss();
                            registerButton.setEnabled(true);
                        }
                    }, 3000);
        }
    }

    public void OnRegistrationSuccess()
    {
        setResult(RESULT_OK, null);
        finish();
    }

    private void RegisterWithEmail(String email, String password)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        progressDialog.dismiss();

                        // If registration fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handle in the listener.
                        if (!task.isSuccessful())
                        {
                            Log.v(TAG, "RegisterWithEmail:failed", task.getException());

                            registerButton.setEnabled(true);

                            Toast.makeText(RegistrationActivity.this,
                                    R.string.error_email_registration_failed, Toast.LENGTH_LONG).show();
                        } else
                        {
                            OnRegistrationSuccess();
                        }
                    }
                });
    }
}
