/* CSCI 4176: Mobile Computing Group Project
Group #9

This singleton class is used to retrieve weather information from OpenWeatherMap's API.
 */
package group9.adventurus;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

public class RequestQueue {
    private static RequestQueue storedQueue;
    private com.android.volley.RequestQueue rQueue;
    private static Context context;

    //Singleton constructpr
    private RequestQueue(Context context){
        this.context = context.getApplicationContext();
        rQueue = getRequestQueue();
    }

    //Either instantiate or just return the queue
    public static synchronized RequestQueue getInstance(Context context){
        if(storedQueue == null){
            storedQueue = new RequestQueue(context.getApplicationContext());
        }
            return storedQueue;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

    public com.android.volley.RequestQueue getRequestQueue(){
        if(rQueue == null){
            rQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return rQueue;
    }
}
