package mcc.proj2.androidocr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *  A login screen that offers login via username/password.
 */
public class LoginManagerActivity extends AppCompatActivity {

    private EditText tb_username;
    private EditText tb_password;
    private View m_progress_bar;
    private View m_login_form_view;

    /**
     *  Asynchronous task used to authenticate the user login
     */
    private UserLoginTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize and set up the login form
        tb_username = (EditText) findViewById(R.id.tb_username);
        tb_password = (EditText) findViewById(R.id.tb_password);

        Button login_button = (Button) findViewById(R.id.btn_login);
        if(login_button != null) {
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    attemptLogin();
                }
            });
        }

        m_login_form_view = findViewById(R.id.login_form);
        m_progress_bar = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (mAuthTask != null) {
            return;
        }

        // Reset errors to null
        tb_username.setError(null);
        tb_password.setError(null);

        // Store values at the time of the login attempt
        String username = tb_username.getText().toString();
        String password = tb_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {

            tb_password.setError(getString(R.string.error_invalid_password));
            focusView = tb_password;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {

            tb_password.setError(getString(R.string.error_field_required));
            focusView = tb_password;
            cancel = true;
        }

        if (cancel) {

            // There was an error; don't attempt login and
            // focus the first form field with an error.
            focusView.requestFocus();
        } else {

            // Show a progress spinner, and kick off a background
            // task to perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     *  Checks if the password is too short
     */
    private boolean isPasswordValid(String password) {

        return password.length() > 6;
    }

    /**
     *  Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_login_form_view.setVisibility(show ? View.GONE : View.VISIBLE);
            m_login_form_view.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_login_form_view.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            m_progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
            m_progress_bar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {

            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            m_progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
            m_login_form_view.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {

            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean status = false;

            // Parse the backend IP file and form the URLs first
            if( ((ConnectionHelper) getApplication()).formURLsToConnect() ) {

                // Checks if the network connection is available
                if (((ConnectionHelper) getApplication()).isNetworkAvailable()) {

                    // Hash the (username + password) and then authenticate it
                    String md5Hash = ((ConnectionHelper) getApplication()).computeMD5Hash(mUsername + mPassword);
                    status = ((ConnectionHelper) getApplication()).authenticateUser(md5Hash);
                } else {

                    Toast.makeText(getApplicationContext(), "Network connection lost", Toast.LENGTH_LONG).show();
                }
            } else {

                Toast.makeText(getApplicationContext(), "Failed to connect to backend", Toast.LENGTH_LONG).show();
            }

            return status;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mAuthTask = null;
            showProgress(false);

            if (success) {

                Intent intent = new Intent(LoginManagerActivity.this, OCRProcessorActivity.class);
                startActivity(intent);
            } else {

                tb_password.setError(getString(R.string.error_incorrect_password));
                tb_password.requestFocus();
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {

            // Display a message to the user
            Toast.makeText(getApplicationContext(), "Login cancelled", Toast.LENGTH_SHORT).show();
            mAuthTask = null;
            showProgress(false);
        }
    }
}
