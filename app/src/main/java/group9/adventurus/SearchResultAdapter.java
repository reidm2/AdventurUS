package group9.adventurus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.CustomVH>{
    private ArrayList<Activity> activities;

    //Individual cell object
    public class CustomVH extends RecyclerView.ViewHolder{
        public TextView nameLabel, priceLabel, locationLabel;
        public ImageView image;

        public CustomVH(View view){
            super(view);
            nameLabel = view.findViewById(R.id.nameLabel);
            priceLabel = view.findViewById(R.id.priceLabel);
            locationLabel = view.findViewById(R.id.locationLabel);
            image = view.findViewById(R.id.image);
        }
    }

    public SearchResultAdapter(ArrayList<Activity> activities){
        this.activities = activities;
    }

    public SearchResultAdapter(){
        this.activities = new ArrayList<>();
    }

    //Use custom cells for each row in the recyclerView instead of the default ones
    @Override
    public CustomVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_cell, parent, false);

        return new CustomVH(itemView);
    }

    //Set individual cell values based on the list that came back as a result of the search
    @Override
    public void onBindViewHolder(CustomVH cell, int position) {
        Activity activity = activities.get(position);

        cell.nameLabel.setText(activity.getName());
        cell.locationLabel.setText(activity.getLocation());
        cell.priceLabel.setText(activity.getPrice());

        //Load image from URL using the Picasso Library: http://square.github.io/picasso/
        String url = activity.getImage();
        Picasso.get().load(url).into(cell.image);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }
}
