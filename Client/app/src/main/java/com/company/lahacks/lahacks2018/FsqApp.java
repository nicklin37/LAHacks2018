package com.company.lahacks.lahacks2018;

import android.location.Location;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FsqApp {
    private String mParams;
    private String mAccessToken;
    private static final String API_URL = "https://api.foursquare.com/v2";
    private static final String CALLBACK_URL = "myapp://connect";

    public FsqApp(String client_id, String client_secret, String query, String radius) {
        mParams = "&query=" + query + "&radius=" + radius;
        mAccessToken = "&client_id=" + client_id + "&client_secret=" + client_secret;
    }
    private ArrayList<FsqVenue> getNearby(double latitude, double longitude) throws Exception {
        ArrayList<FsqVenue> venueList = new ArrayList<>();
        try {
            String v = msToString(System.currentTimeMillis());
            String ll = String.valueOf(latitude) + "," + String.valueOf(longitude);
            URL url = new URL(API_URL + "/venues/suggestedcompletion?ll=" + ll + mParams + mAccessToken + "&v=" + v);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();

            String response = streamToString(urlConnection.getInputStream());
            JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
            JSONArray groups = jsonObj.getJSONObject("response").getJSONArray("groups");
            int length = groups.length();

            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    JSONObject group = (JSONObject) groups.get(i);
                    JSONArray items = group.getJSONArray("items");

                    int ilength = items.length();

                    for(int j = 0; j < ilength; j++) {
                        JSONObject item = (JSONObject) items.get(j);

                        FsqVenue venue = new FsqVenue();

                        venue.id = item.getString("id");
                        venue.name = item.getString("name");

                        JSONObject location = item.getJSONObject("location");

                        Location loc = new Location(LocationManager.GPS_PROVIDER);

                        loc.setLatitude(Double.valueOf(location.getString("lat")));
                        loc.setLongitude(Double.valueOf(location.getString("lng")));

                        venue.location = loc;
                        venue.address = location.getString("address");
                        venue.distance = location.getInt("distance");
                        venue.herenow = item.getJSONObject("hereNow").getInt("count");
                        venue.type = group.getString("type");
                        venueList.add(venue);

                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return venueList;
    }

    private String msToString(long ms) {
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(ms);
        return date.format(calendar.getTime());
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }
        return str;
    }
}
