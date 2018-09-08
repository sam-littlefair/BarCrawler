package uk.co.squaredsoftware.barcrawler;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SecondActivity extends AppCompatActivity implements LocationListener {
    private int total = 51567;
    private int iterations = 0;
    private List<Point> x = new ArrayList<Point>();
    private final String TAG = "GPS";
    private String googleMapsLink = "https://www.google.com/maps/dir/?api=1&origin=";

    //double maxPrice = Double.parseDouble(max);
    private double totalPrice = 0;
    private double totalDistance = 0;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView tvLatitude, tvLongitude, tvTime;
    private LocationManager locationManager;
    private Location loc;
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private boolean isGPS = false;
    private boolean isNetwork = false;
    private boolean canGetLocation = true;
    private String longitude = "";
    private String latitude = "";
    private double maxValue = 1000000;
    private double maxValueOverall = 1000000;
    private File sdcard = Environment.getExternalStorageDirectory();

    /*@Override
    public void onRestart(){
        super.onRestart();
        Intent previewMessage = new Intent(SecondActivity.this, SecondActivity.class);
        this.finish();
        startActivity(previewMessage);
    }*/
    public void setMaxVal(double max, double overall) {
        maxValue = max;
        maxValueOverall = overall;
    }

    public void assignGoogle(String google) {
        googleMapsLink = google;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().getStringExtra("maxvalue") == null) {
            Intent myIntent = new Intent(SecondActivity.this,
                    MainActivity.class);

            startActivity(myIntent);
        }
        String max = getIntent().getStringExtra("maxvalue");
        double maxVal = Double.parseDouble(max);
        String overallmax = getIntent().getStringExtra("overallmaxvalue");
        double overallmaxVal = Double.parseDouble(overallmax);
        setMaxVal(maxVal, overallmaxVal);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.padded_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //tvLatitude = findViewById(R.id.tvLatitude);
        // tvLongitude = findViewById(R.id.tvLongitude);
        //tvTime = findViewById(R.id.tvTime);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                        ALL_PERMISSIONS_RESULT);
                Log.d(TAG, "Permission requests");
                canGetLocation = false;
            }

            // get location
            getLocation();
            final String postcode = getIntent().getStringExtra("postcode");


            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://api.postcodes.io/postcodes/" + postcode;
            if ("use_the_device_loc".equals(postcode)) {

                List<Point> p1 = new ArrayList<Point>();
                Scanner scanner = null;
                try {
                    scanner = new Scanner(getAssets().open("open_pubs.csv"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                scanner.useDelimiter("\\n");
                // while(scanner.hasNext()){
                scanner.next();

                p1.add(new Point(Double.parseDouble(longitude), Double.parseDouble(latitude), "Person", 0, 0));
                for (int i = 0; i < 51567; i++) {
                    String data = scanner.next();
                    String[] array = data.split(",");
                    //System.out.println(array[7]);
                    try {
                        Double.parseDouble(array[7].substring(1, array[7].length() - 1));
                        p1.add(new Point(Double.parseDouble(array[7].substring(1, array[7].length() - 1)),
                                Double.parseDouble(array[6].substring(1, array[6].length() - 1)),
                                (String) array[1], 0,
                                Double.parseDouble(array[9].substring(1, array[9].length() - 1))));

                    } catch (NumberFormatException e) {
                        total--;
                    }

                }
                scanner.close();
                int numofpubs = getIntent().getIntExtra("pubs", 0);
                List<String> input = new ArrayList<>();
                findRoute(p1, p1.get(0), numofpubs, p1);
                googleMapsLink += x.get(0).getY() + "," + x.get(0).getX() + "&destination=" + x.get(x.size() - 1).getY() + "," + x.get(x.size() - 1).getX() + "&waypoints=";
                for (int ab = 0; ab < x.size(); ab++) {
                    //System.out.println(x.get(ab));
                    //Toast toast2 = Toast.makeText(getApplicationContext(), x.get(ab).toString(), Toast.LENGTH_SHORT);
                    //toast2.show();
                    if (ab != 0 && ab != x.size() - 1) {
                        googleMapsLink += "|" + x.get(ab).getY() + "," + x.get(ab).getX();
                    }

                    String next = x.get(ab).getName();
                    next = next.substring(1, next.length() - 1);
                    next += "splitme" + String.format("%.3f", x.get(ab).getKey()) + "splitme" + x.get(ab).getPrice();
                    input.add(next);

                }
                setContentView(R.layout.activity_second);
                recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                recyclerView.setHasFixedSize(true);

                layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                mAdapter = new MyAdapter(input);
                recyclerView.setAdapter(mAdapter);

            } else {
                setContentView(R.layout.activity_second);
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                //mTextView.setText("Response is: "+ response.substring(0,500));
                                JSONObject obj = null;
                                String isValid = "";

                                try {
                                    obj = new JSONObject(response);
                                    isValid = obj.getString("status");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (isValid.equals("200")) {

                                    try {
                                        longitude = obj.getJSONObject("result").getString("longitude");
                                        latitude = obj.getJSONObject("result").getString("latitude");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //Toast toast = Toast.makeText(getApplicationContext(), longitude + "" + latitude, Toast.LENGTH_SHORT);
                                    //toast.show();


                                    List<Point> p1 = new ArrayList<Point>();
                                    Scanner scanner = null;
                                    try {
                                        scanner = new Scanner(getAssets().open("open_pubs.csv"));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    scanner.useDelimiter("\\n");
                                    // while(scanner.hasNext()){
                                    scanner.next();
                                    p1.add(new Point(Double.parseDouble(longitude), Double.parseDouble(latitude), "Person", 0, 0));
                                    for (int i = 0; i < 51567; i++) {
                                        String data = scanner.next();
                                        String[] array = data.split(",");
                                        //System.out.println(array[7]);
                                        try {
                                            Double.parseDouble(array[7].substring(1, array[7].length() - 1));
                                            p1.add(new Point(Double.parseDouble(array[7].substring(1, array[7].length() - 1)),
                                                    Double.parseDouble(array[6].substring(1, array[6].length() - 1)),
                                                    (String) array[1], 0,
                                                    Double.parseDouble(array[9].substring(1, array[9].length() - 1))));
                                        } catch (NumberFormatException e) {
                                            total--;
                                        }

                                    }
                                    scanner.close();
                                    int numofpubs = getIntent().getIntExtra("pubs", 0);
                                    List<String> input = new ArrayList<>();
                                    findRoute(p1, p1.get(0), numofpubs, p1);
                                    String otherGoogle = "https://www.google.com/maps/dir/?api=1&origin=";
                                    otherGoogle += x.get(0).getY() + "," + x.get(0).getX() + "&destination=" + x.get(x.size() - 1).getY() + "," + x.get(x.size() - 1).getX() + "&waypoints=";

                                    for (int ab = 0; ab < x.size(); ab++) {
                                        if (ab != 0 && ab != x.size() - 1) {
                                            otherGoogle += "|" + x.get(ab).getY() + "," + x.get(ab).getX();
                                        }
                                        String next = x.get(ab).getName();
                                        next = next.substring(1, next.length() - 1);
                                        next += "splitme" + String.format("%.3f", x.get(ab).getKey())
                                                + "splitme" + x.get(ab).getPrice();
                                        input.add(next);

                                    }
                                    assignGoogle(otherGoogle);
                                    recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                                    recyclerView.setHasFixedSize(true);

                                    layoutManager = new LinearLayoutManager(getApplicationContext());
                                    recyclerView.setLayoutManager(layoutManager);
                                    mAdapter = new MyAdapter(input);
                                    recyclerView.setAdapter(mAdapter);


                                    NumberFormat formatter = NumberFormat.getCurrencyInstance();
                                    TextView info = (TextView) findViewById(R.id.info);
                                    info.setText("Total Distance: " + String.format("%.3f", totalDistance) + " miles.    Total cost: " + formatter.format(totalPrice));
                                    //  tvLatitude.setText(latitude);
                                    // tvLongitude.setText(longitude);
                                    //Toast toast = Toast.makeText(getApplicationContext(), "long" + longitude + " lat:" + latitude, Toast.LENGTH_SHORT);
                                    //toast.show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast3 = Toast.makeText(getApplicationContext(),
                                "Post code is not valid!", Toast.LENGTH_SHORT);
                        toast3.show();

                        Intent myIntent = new Intent(SecondActivity.this,
                                MainActivity.class);

                        startActivity(myIntent);
                    }
                });
// Add the request to the RequestQueue.
                queue.add(stringRequest);


            }
        }
        Button button = (Button) findViewById(R.id.googlemaps);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View arg0) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsLink));
                startActivity(browserIntent);

            }
        });
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        TextView info = (TextView) findViewById(R.id.info);
        info.setText("Total Distance: " + String.format("%.3f", totalDistance) + " miles.    Total cost: " + formatter.format(totalPrice));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void getLocation() {
        try {
            if (canGetLocation) {
                Log.d(TAG, "Can get location");
                if (isGPS) {
                    // from GPS
                    Log.d(TAG, "GPS on");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else if (isNetwork) {
                    // from Network Provider
                    Log.d(TAG, "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else {
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    updateUI(loc);
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private double findDistance(double x1, double y1, double x2, double y2) {
        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(x2 - x1);
        double dLng = Math.toRadians(y2 - y1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(y1)) * Math.cos(Math.toRadians(y2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
        //return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }


    private List<Point> findRoute(List<Point> pubs, Point v, int numofpubs, List<Point> p1) {
        if (totalPrice > maxValueOverall) {
            Toast toast3 = Toast.makeText(getApplicationContext(),
                    "Max budget exceeded, crawl shortened.", Toast.LENGTH_SHORT);
            toast3.show();
            x.remove(x.size() - 1);
            return x;
        } else {
            pubs.remove(v);
            double smallest = 10000000;
            int arrayindex = 0;
            for (int index = 0; index < total; index++) {
                if (pubs.get(index).getPrice() <= maxValue) {
                    {
                        if ((findDistance(v.getX(), v.getY(), pubs.get(index).getX(), pubs.get(index).getY())) < smallest) {
                            smallest = (findDistance(v.getX(), v.getY(), pubs.get(index).getX(), pubs.get(index).getY()));
                            arrayindex = index;
                        }
                        p1.get(index).setKey(findDistance(v.getX(), v.getY(), pubs.get(index).getX(), pubs.get(index).getY()));

                    }
                }
                totalDistance += p1.get(arrayindex).getKey();
                totalPrice += p1.get(arrayindex).getPrice();
                x.add(p1.get(arrayindex));
                x.get(iterations).setKey(p1.get(arrayindex).getKey());
                p1.remove(arrayindex);
                total--;
                if (iterations == numofpubs - 1) {
                    return x;
                } else {
                    iterations++;
                    findRoute(p1, x.get(iterations - 1), numofpubs, p1);
                }
                //}
            }
            return null;
        }


        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult ( int requestCode, String[] permissions,int[] grantResults){
            switch (requestCode) {
                case ALL_PERMISSIONS_RESULT:
                    Log.d(TAG, "onRequestPermissionsResult");
                    for (String perms : permissionsToRequest) {
                        if (!hasPermission(perms)) {
                            permissionsRejected.add(perms);
                        }
                    }

                    if (permissionsRejected.size() > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;

                        }
                    } else {
                        Log.d(TAG, "No rejected permissions.");
                        canGetLocation = true;
                        getLocation();
                    }
                    break;
                default:
                    break;
            }
        }

        public void showSettingsAlert () {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("GPS is not Enabled!");
            alertDialog.setMessage("Do you want to turn on GPS?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }

        private void showMessageOKCancel (String message, DialogInterface.OnClickListener okListener){
            new AlertDialog.Builder(SecondActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        }

        private void updateUI (Location loc){
            Log.d(TAG, "updateUI");
            longitude = Double.toString(loc.getLongitude());
            latitude = Double.toString(loc.getLatitude());
            //  tvLatitude.setText(Double.toString(loc.getLatitude()));
            // tvLongitude.setText(Double.toString(loc.getLongitude()));

            // tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
        }
    }