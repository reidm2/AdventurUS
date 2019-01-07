/* CSCI 4176: Mobile Computing Group Project
Group #9

This ActivityList class defines the logic used to display our search results in a grid-based UI.
This is also the class where the search filtering takes place.
The search filtering is based off of values passed in from the preceding activity (SearchPage) that navigates to this class.
 */
package group9.adventurus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActivityList extends AppCompatActivity {

    private GridView gridView;
    private TextView warning;
    private ActivityAdapter activityAdapter;
    private boolean typeFound = false;
    private double calculatedDistance = 0.0;
    private int enteredDistance = 0;
    private String activityType, cost, distanceSelected, result;
    private Utilities utils = Utilities.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();

        //Retrieve information passed to this intent
        try {
            activityType = (String) intent.getSerializableExtra("Activity");
            cost = (String) intent.getSerializableExtra("Cost");
            distanceSelected = (String) intent.getSerializableExtra("Distance");
            //Set integer value for distance depending on which Spinner option was selected in the previous screen
            switch (distanceSelected)
            {
                case "Within 1 KM":
                {
                    enteredDistance = 1;
                    break;
                }
                case "Within 2 KM":
                {
                    enteredDistance = 2;
                    break;
                }
                case "Within 5 KM":
                {
                    enteredDistance = 5;
                    break;
                }
                case "Within 10 KM":
                {
                    enteredDistance = 10;
                    break;
                }
            }
        }

        catch (Exception e) {
            Log.d("Deserialization Error", "An error occurred when fetching filter information:\n" + e.getMessage());
        }

        //Get activities from the database
        getAllResults();

        gridView = findViewById(R.id.gridView);
        warning = findViewById(R.id.noResultsWarning);

        activityAdapter = new ActivityAdapter(this, Activity.activities);
        gridView.setAdapter(activityAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Check for connectivity
                if (utils.checkConnection(getApplicationContext()) == false)
                {
                    Toast.makeText(ActivityList.this, "Error: network connection not detected, check your connection status.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(ActivityList.this, DetailedViewActivity.class);
                    Activity clicked = Activity.activities.get(position);
                    intent.putExtra("Activity", clicked);
                    startActivity(intent);
                }
            }
        });

    }

    public void getAllResults() {
        //Get entire "Activities" subsection of the database
        DatabaseReference coursesDataReference = Firebase.getRootDataReference().child("Activities/");

        coursesDataReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Activity> searchResults = new ArrayList<>();
                        Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                        for (DataSnapshot dsActivity : dataSnapshots) {
                            /* Create an Activity object fetched from the Data Snapshot.
                            Compare the price with the cost passed in from the previous intent

                            Distance filtering is also in place
                            For each activity fetched from the Database, use the getCoordinates and calculateDistanceFromMe
                            methods to determine how far away I am from any given activity.
                            */
                            Activity check = dsActivity.getValue(Activity.class);
                            LatLng coordinates = utils.getCoordinates(getApplicationContext(), check.getLocation());
                            calculatedDistance = utils.calculateDistanceFromMe(coordinates);

                            DecimalFormat round = new DecimalFormat(".##");

                            //Set the result based on the DecimalFormat, convert it to a double value
                            result = round.format(calculatedDistance/1000.0);
                            calculatedDistance = Double.parseDouble(result);

                            //If the activity's cost is equal to the selected value (OR "any" is selected), AND the distance is within range, then a possible match is found
                            if ((check.getPrice().equals(cost) || cost.equals("ANY")) && calculatedDistance <= enteredDistance)
                            {
                                //Fetch the different types of activities that "this" activity is listed under
                                ArrayList<String> types = check.getActivityType();
                                for (int i = 0; i < types.size(); i++)
                                {
                                    //If any of the activity types are a match with the filter from the spinner, then a match is found
                                    if (types.get(i).equals(activityType) || activityType.equals("Any"))
                                    {
                                        typeFound = true;
                                    }
                                }
                                //ONLY add an activity to the search results if both the cost and the activity filter are a match
                                if (typeFound == true)
                                {
                                    searchResults.add(check);
                                }
                                //Reset the activity type flag to "false" for the next activity check
                                typeFound = false;
                            }
                        }

                        dataChangeHandler(searchResults);

                        //Let the user know if their search criteria didn't yield any results
                        if (searchResults.isEmpty())
                        {
                            warning.setText(R.string.noResults);
                        }
                        else{
                            warning.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("Error", databaseError.toString());
                    }
                }
        );
    }
    public void dataChangeHandler(ArrayList<Activity> newData) {
        Activity.activities.clear();
        Activity.activities.addAll(newData);
        activityAdapter.notifyDataSetChanged();
    }
}
