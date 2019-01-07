/* CSCI 4176: Mobile Computing Group Project
Group #9

This Java class defines the logic used in the Search Page.
In particular, it passes to the search results page the button/spinner selections, which are used
to help filter the search results
 */

package group9.adventurus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchPage extends AppCompatActivity {
    public Button searchButton, any, free, cheap, medium, expensive;
    public Spinner typeSpinner, distanceSpinner;
    //These String values are used to hold the button/spinner selections to be passed to the ActivityList class as search filters
    public String activity, cost, distanceSelected;
    private Utilities util = Utilities.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        //Initialize view objects
        searchButton = findViewById(R.id.searchButton);
        typeSpinner = findViewById(R.id.activityTypes);
        distanceSpinner = findViewById(R.id.distance);
        any = findViewById(R.id.anyButton);
        free = findViewById(R.id.freeButton);
        cheap = findViewById(R.id.cheapButton);
        medium = findViewById(R.id.mediumButton);
        expensive = findViewById(R.id.expensiveButton);

        /*Set the "any" button is being selected by default, as well as "Any" as the default cost.
        These are used as default values which can be passed in to the ActivityList class in case nothing else is selected.
         */
        any.setSelected(true);
        cost = any.getText().toString();

        //Set the Spinner values for the different Activity Types that can be selected
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,R.array.activityTypes, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        //Set the Spinner values for the different Distance values that can be selected
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,R.array.distanceValues, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        /* Set an OnClickListener which will be used for the cost buttons.
        In this method, the Switch statement will highlight whichever button is tapped on, and set that button's value to the cost string.
         */
        View.OnClickListener priceButtonClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.anyButton:
                        any.setSelected(true);
                        free.setSelected(false);
                        cheap.setSelected(false);
                        medium.setSelected(false);
                        expensive.setSelected(false);
                        cost = any.getText().toString();
                        break;

                    case R.id.freeButton:
                        any.setSelected(false);
                        free.setSelected(true);
                        cheap.setSelected(false);
                        medium.setSelected(false);
                        expensive.setSelected(false);
                        cost = free.getText().toString();
                        break;

                    case R.id.cheapButton:
                        any.setSelected(false);
                        free.setSelected(false);
                        cheap.setSelected(true);
                        medium.setSelected(false);
                        expensive.setSelected(false);
                        cost = cheap.getText().toString();
                        break;

                    case R.id.mediumButton:
                        any.setSelected(false);
                        free.setSelected(false);
                        cheap.setSelected(false);
                        medium.setSelected(true);
                        expensive.setSelected(false);
                        cost = medium.getText().toString();
                        break;

                    case R.id.expensiveButton:
                        any.setSelected(false);
                        free.setSelected(false);
                        cheap.setSelected(false);
                        medium.setSelected(false);
                        expensive.setSelected(true);
                        cost = expensive.getText().toString();
                        break;

                    default:
                        any.setSelected(false);
                        free.setSelected(false);
                        cheap.setSelected(false);
                        medium.setSelected(false);
                        expensive.setSelected(false);
                        break;
                }
            }
        };

        any.setOnClickListener(priceButtonClick);
        free.setOnClickListener(priceButtonClick);
        cheap.setOnClickListener(priceButtonClick);
        medium.setOnClickListener(priceButtonClick);
        expensive.setOnClickListener(priceButtonClick);

        //When the search button is clicked, pass the selected values (the cost, activity, and distanceSelected strings) from the buttons/spinners to the ActivityList class
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (util.checkConnection(getApplicationContext()) == false)
                {
                    Toast.makeText(SearchPage.this, "Error: Connection status not detected, please check your connection and try again", Toast.LENGTH_LONG).show();
                }
                else
                {
                    activity = typeSpinner.getSelectedItem().toString();
                    distanceSelected = distanceSpinner.getSelectedItem().toString();
                    Intent toResults = new Intent(SearchPage.this, ActivityList.class);
                    toResults.putExtra("Activity", activity);
                    toResults.putExtra("Cost", cost);
                    toResults.putExtra("Distance", distanceSelected);
                    startActivity(toResults);
                }

            }
        });
    }
}