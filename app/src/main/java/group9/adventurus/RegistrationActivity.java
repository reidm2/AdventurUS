/* CSCI 4176: Mobile Computing Group Project
Group #9

This Java class contains the logic for the registration page (the layout for which is defined in activity_registration.xml)
 */
package group9.adventurus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextUsername , editTextpassword, editTextconfirmpassword,editTextemail ;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Utilities util = Utilities.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextUsername = findViewById(R.id.usernameField);
        editTextpassword = findViewById(R.id.passwordField);
        editTextconfirmpassword = findViewById(R.id.confirmField);
        editTextemail = findViewById(R.id.emailField);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        findViewById(R.id.registerButton).setOnClickListener(this);
        findViewById(R.id.loginButton).setOnClickListener(this);

        //Initializing the Authentication Object via Firebase
        mAuth = FirebaseAuth.getInstance();
    }


    private void userRegister(){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextpassword.getText().toString().trim();
        String confirmPassword = editTextconfirmpassword.getText().toString().trim();
        final String email = editTextemail.getText().toString().trim();

        /* Validate that all of the inputs are correct.

        First, we check to make sure none of the fields have been left empty
         */
        if(username.isEmpty())
        {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if(email.isEmpty()){
            editTextemail.setError("Email is Required");
            editTextemail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editTextpassword.setError("Password is required");
            editTextpassword.requestFocus();
            return;
        }
        if(confirmPassword.isEmpty())
        {
            editTextconfirmpassword.setError("Password Confirmation is required");
            editTextconfirmpassword.requestFocus();
            return;
        }

        //Check that the email address is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editTextemail.setError("Invalid Email Address");
            editTextemail.requestFocus();
            return;
        }

        //Check that the passwords match, and that the password length is at least 7 characters in length
        if(!password.equals(confirmPassword)){
            editTextconfirmpassword.setError("Password does not match");
            editTextconfirmpassword.requestFocus();
            return;
        }

        if(password.length()<7){
            editTextpassword.setError("Minimum length of password should be 7 ");
            editTextpassword.requestFocus();
            return;
        }

        //Before starting the registration , set the progress bar
        progressBar.setVisibility(View.VISIBLE);

        //Attempt to register the user in Firebase, using the entered information from the text fields to create a new User object
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegistrationActivity.this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //Check if the registration is successful or not
                        if (task.isSuccessful()) {
                            User user = new User(username, password, email);

                            /*Set the user object to the Firebase database inside the users node.
                            Inside the node "Users" , a unique User ID is generated while creating a user and firebase authentication*/
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Hiding the progress bar on completion of task i.e. after saving the additional fields
                                    progressBar.setVisibility(View.GONE);

                                    if(task.isSuccessful()){
                                        Toast.makeText(RegistrationActivity.this,"Registration is Successful",Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(RegistrationActivity.this,"Registration Unsuccessful " +task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                        }
                        else {
                            //Display of error message
                            Toast.makeText(RegistrationActivity.this,"Registration Unsuccessful " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

        //Returns user to the login page
        public void openLoginPage(){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

    @Override
    /*If the "Register" button is tapped, begin the registration process.
    Otherwise, if the "Login here!" button is tapped, return back to the login page
     */
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerButton:
                if (util.checkConnection(getApplicationContext()) == false)
                {
                    Toast.makeText(this, "Error: network connection not detected. Check connection status and try again", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    userRegister();
                }
                break;

            case R.id.loginButton:
                openLoginPage();
                break;
        }
    }

}