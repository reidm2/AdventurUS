/* CSCI 4176: Mobile Computing Group Project
Group #9

Our Firebase class is used to handle all of the operations on the back-end (implemented via, of course, Firebase).
The class is implemented as a singleton, as we only ever need to instantiate one version of Firebase.
 */

package group9.adventurus;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;

public class Firebase {

    //Singleton instance
    private static Firebase instance;

    /**
     * Gets the singleton Firebase instance, instantiating if need be
     * @return Firebase Singleton
     */
    public static Firebase getFirebase(){
        if(instance == null){
            instance = new Firebase();
        }

        return instance;
    }

    /**
     *Initialize the database connection and references
     */
    public static void initializeFirebase(Context appContext){
        instance = new Firebase();
        FirebaseApp.initializeApp(appContext);
        instance.rootDataSource = FirebaseDatabase.getInstance();
        instance.rootDataReference = instance.rootDataSource.getReference();
    }

    //Database references
    private static FirebaseDatabase rootDataSource;
    private static DatabaseReference rootDataReference;
    private static FirebaseAuth firebaseAuth;

    public static FirebaseAuth getAuth() {
        return instance.firebaseAuth = FirebaseAuth.getInstance();
    }

    public static DatabaseReference getRootDataReference() {
        return instance.rootDataReference;
    }

    //Method for performing a user log in attempt
    public void signIn(final Context context, String email, String password, Activity callingActivity) {
        if(firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(context, "Authentication success.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(context, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
    }
}

