/* CSCI 4176: Mobile Computing Group Project
Group #9

This class defines an Adapter that we use to display Activity information in the Search Results
 */
package group9.adventurus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActivityAdapter extends BaseAdapter{
    private final Context context;
    private final ArrayList<Activity> activities;
    private Utilities utils = Utilities.getInstance();

    public ActivityAdapter(Context context, ArrayList<Activity> activities){
        this.context = context;
        this.activities = activities;
    }

    @Override
    public int getCount(){
        return activities.size();
    }
    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public Activity getItem(int position) {
        return activities.get(position);
    }

    @Override
    public View getView(int position, View cell, ViewGroup parent) {
        final Activity activity = activities.get(position);

        if(cell == null){
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            cell = layoutInflater.inflate(R.layout.activity_grid_cell, null);
        }

        //Set values inside grid cell
        final ImageView img = cell.findViewById(R.id.image);

        //Load image from URL using the Picasso Library: http://square.github.io/picasso/
        String url = activity.getImage();
        Picasso.get().load(url).into(img);

        final TextView name = cell.findViewById(R.id.nameLabel);
        name.setText(activity.getName());

        final TextView price = cell.findViewById(R.id.priceLabel);
        price.setText("Cost: " + activity.getPrice());

        final TextView location = cell.findViewById(R.id.locationLabel);
        LatLng coordinates = utils.getCoordinates(context, activity.getLocation());
        double distance = utils.calculateDistanceFromMe(coordinates);

        DecimalFormat round = new DecimalFormat(".##");

        String result = round.format(distance/1000.0) + " km away";
        location.setText(result);

        return cell;
    }

}
