/* CSCI 4176: Mobile Computing Group Project
Group #9

This Java class contains the logic for submitting a new activity to Firebase.

This is where users can enter their activity details, and use the camera to upload a photo of the activity.
 */

package group9.adventurus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

public class SuggestionsPage extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{

    //Public variables
    public static final int CHOOSE_PHOTOS = 1;
    public static final int CLICK_PHOTOS = 2;
    public Spinner sActivityTypes;

    //Static variables
    private static final int MAX_IMAGE_WIDTH = 256;
    private static final int MAX_IMAGE_HEIGHT = 256;

    //private variables
    private Button btnSubmit;
    private Button btnAddPhotos;
    private Button btnDelImage;
    private ImageButton btnCamera;
    private ImageView ivImage;
    private EditText etActivityName;
    private EditText etActivityDesc;
    private SeekBar sbPrice;
    private PermissionManager permissionManager = PermissionManager.getInstance();
    private Bitmap activityPhoto;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String activityPrice;
    private Utilities utils = Utilities.getInstance();

    //For a reference to this activity
    Activity thisActivity;

    /* This class is used for uploading data to Firebase.
    * Once all the parameters are set, an object of the class
    * is passed to Firebase.*/
    @IgnoreExtraProperties
    public class ActivityForUpload
    {
        public ArrayList<String> activityType;
        public String name;
        public String price;
        public String location;
        public String image;
        public String description;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions_page);

        //Initialize all the variables with their corresponding views
        btnSubmit = findViewById(R.id.btnSubmitActivity);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        ivImage = findViewById(R.id.imageView);
        sActivityTypes = findViewById(R.id.spActivityType);
        etActivityDesc = findViewById(R.id.descInputField);
        etActivityName = findViewById(R.id.nameInputField);
        btnCamera = findViewById(R.id.btnCamera);
        sbPrice = findViewById(R.id.seekBarPrice);
        btnDelImage = findViewById(R.id.deleteImage);

        //Initialize Firebase references
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //The default value of price is set
        activityPrice = getString(R.string.priceFree);

        //An adapter to display different activity types
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource
                (this,R.array.suggestedTypes, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sActivityTypes.setAdapter(typeAdapter);

        thisActivity = this;

        btnSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Do not proceed if Name and Description are empty
                if(TextUtils.isEmpty(etActivityDesc.getText()))
                {
                    etActivityDesc.setError("Please Enter a description");
                    return;
                }
                if(TextUtils.isEmpty(etActivityName.getText()))
                {
                    etActivityName.setError("Please Enter a Name");
                    return;
                }
                //Save the image to gallery and obtain its uri
                String imagePath = MediaStore.Images.Media.insertImage(getContentResolver(),
                        activityPhoto, etActivityDesc.getText().toString(), null);
                uploadPhoto(imagePath);
            }
        });

        btnAddPhotos.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(permissionManager.getStoragePermission(getApplicationContext(), thisActivity))
                {
                    selectPhoto();
                }
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(permissionManager.getCameraPermission(getApplicationContext(), thisActivity))
                {
                    clickPhoto();
                }
            }
        });

        //A listener that updates the price based on user selection
        sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                switch (i)
                {
                    case 0:
                        activityPrice = getString(R.string.priceFree);
                        break;
                    case 1:
                        activityPrice = getString(R.string.priceLow);
                        break;
                    case 2:
                        activityPrice = getString(R.string.priceMedium);
                        break;
                    case 3:
                        activityPrice = getString(R.string.priceHigh);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //Do nothing
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CHOOSE_PHOTOS && resultCode == RESULT_OK && data != null)
        {
            //Get selected image from gallery and display it to the user
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ivImage.setImageBitmap(photo);
            btnDelImage.setVisibility(View.VISIBLE);
            activityPhoto = photo;
        }
        else if (requestCode == CLICK_PHOTOS && resultCode == RESULT_OK && data != null)
        {
            //Get the image taken by the phone's camera
            Bitmap raw = (Bitmap) data.getExtras().get("data");
            int width = raw.getWidth();
            int height = raw.getHeight();
            Bitmap photo;

            //Resize the image to fit the standard size used by the application and
            //make sure the new image is not larger than raw image as this will fail.
            if(width> MAX_IMAGE_WIDTH && height > MAX_IMAGE_HEIGHT)
                photo = Bitmap.createBitmap(raw, 0, 0, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            else
            {
                int minDimension = (width<height)? width: height;
                photo = Bitmap.createBitmap(raw, 0, 0, minDimension, minDimension);
            }
            ivImage.setImageBitmap(photo);
            btnDelImage.setVisibility(View.VISIBLE);
            activityPhoto = photo;
        }
    }

    /*Handles user's acceptance or rejection of permission. If the user grants permissions,
    * then perform the tasks that requested thos permissions*/
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case PermissionManager.PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    permissionManager.setStoragePermissionGranted(true);
                    selectPhoto();
                }
                else
                {
                    permissionManager.setStoragePermissionGranted(false);
                }
                break;
            case PermissionManager.PERMISSIONS_REQUEST_CAMERA:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    permissionManager.setCameraPermissionGranted(true);
                    clickPhoto();
                }
                else
                {
                    permissionManager.setCameraPermissionGranted(false);
                }
                break;
        }
        return;
    }

    //Opens the phone's gallery and lets the user pick an image
    private void selectPhoto()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Set the image size and aspect ratio
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("outputX", MAX_IMAGE_WIDTH);
        intent.putExtra("outputY", MAX_IMAGE_HEIGHT);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);

        Intent chooserIntent = Intent.createChooser(intent, "Select Photos");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent});

        startActivityForResult(chooserIntent, CHOOSE_PHOTOS);
    }

    //Launches the phone's camera to take a photo
    private void clickPhoto()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CLICK_PHOTOS);
    }

    //Uploads an image to firebase and call the function to upload rest of the activity
    private void uploadPhoto(String path)
    {
        //Check for connection status before uploading to Firebase
        if (utils.checkConnection(getApplicationContext()) == false)
        {
            Toast.makeText(thisActivity, "Error: No network connection, check network connectivity and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(path);
        //<Begin> Source used for reference: https://code.tutsplus.com/tutorials/image-upload-to-firebase-in-android-application--cms-29934
        final ProgressDialog progressDialog = new ProgressDialog(thisActivity);
        final String remotePath = "images/" +UUID.randomUUID().toString();

        progressDialog.setTitle("Uploading Photo...");
        progressDialog.show();

        final StorageReference reference = storageReference.child(remotePath);
        reference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        progressDialog.dismiss();

                        //Get image uri from firebase, we will set this as the uri for activity images
                        reference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                imageUri = uri;
                                //Upload activity once we have the image uri
                                uploadActivity();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(getApplicationContext(), "Failed to get image uri", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Toast.makeText(getApplicationContext(), "Upload Successful!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Upload Failed!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        //Show the completed percentage while uploading the photo to Firebase
                        double progress = (taskSnapshot.getBytesTransferred()/
                                taskSnapshot.getTotalByteCount()) * 100.0;
                        progressDialog.setMessage("Uploading: " + (int)progress + "%");
                    }
                });
        //<End>
    }

    void uploadActivity()
    {
        //Check for connection status before uploading to Firebase
        if (utils.checkConnection(getApplicationContext()) == false)
        {
            Toast.makeText(thisActivity, "Error: No network connection, check network connectivity and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(thisActivity);
        progressDialog.setTitle("Uploading Activity...");
        progressDialog.show();
        ActivityForUpload activity = new ActivityForUpload();
        ArrayList<String> types = new ArrayList<>();
        types.add(sActivityTypes.getSelectedItem().toString());
        activity.name = etActivityName.getText().toString();
        activity.price = activityPrice;
        activity.location = utils.getLocationName();
        activity.activityType = types;
        activity.image = imageUri.toString();
        activity.description = etActivityDesc.getText().toString();

        //Create a new record in the Firebase database and upload data present in our ActivityForUpload object
        databaseReference.child("Activities").push().setValue(activity)
                .addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                Toast.makeText(getApplicationContext(), "Activity Submitted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                //Close the Submissions Page. The activity has been submitted successfully.
                finish();
            }
        })
        .addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to Upload To Database",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //Deletes the user selected image. It is called on button click.
    public void resetImage(View v)
    {
        ivImage.setImageResource(R.drawable.imgplaceholder);
        btnDelImage.setVisibility(View.INVISIBLE);
    }
}
