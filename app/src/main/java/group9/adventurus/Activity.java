/* CSCI 4176: Mobile Computing Group Project
Group #9

This class defines an Activity object, with the standard getters and setters
 */

package group9.adventurus;

import java.io.Serializable;
import java.util.ArrayList;

public class Activity implements Serializable{

    public static ArrayList<Activity> activities = new ArrayList<>();

    private String name;
    private String price;
    private String location;
    private ArrayList<String> activityType;
    private String image;
    private ArrayList<String> busyTimes;
    private String description;

    public Activity(String name, String price, String location, ArrayList<String> activityType, String image, ArrayList<String> busyTimes, String description){
        this.name = name;
        this.price = price;
        this.location = location;
        this.activityType = activityType;
        this.image = image;
        this.busyTimes = busyTimes;
        this.description = description;
    }

    public Activity(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getActivityType() {
        return activityType;
    }

}
