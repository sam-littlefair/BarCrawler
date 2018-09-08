package uk.co.squaredsoftware.barcrawler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private CheckBox check;

    @Override
    public void onRestart() {
        super.onRestart();
        Intent previewMessage = new Intent(MainActivity.this, MainActivity.class);
        this.finish();
        startActivity(previewMessage);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from activity_main.xml
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.padded_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        // Locate the button in activity_main.xml
        button = (Button) findViewById(R.id.getloc);
        check = (CheckBox) findViewById(R.id.checkBox);
        check.setOnClickListener(new OnClickListener() {
            public void onClick(View arg1) {
                if (check.isChecked()) {
                    findViewById(R.id.postcode).setVisibility(View.INVISIBLE);
                    findViewById(R.id.post_text).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.postcode).setVisibility(View.VISIBLE);
                    findViewById(R.id.post_text).setVisibility(View.VISIBLE);
                }
            }
        });
        // Capture button clicks
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                button.setEnabled(false);
                button.setText("Thinking...");
                boolean isPubNull = false;
                EditText postcode = (EditText) findViewById(R.id.postcode);
                EditText pubs = (EditText) findViewById(R.id.totalpubs);
                EditText maxprice = (EditText) findViewById(R.id.editText5);
                double max = 100000;
                EditText maxoverallprice = (EditText) findViewById(R.id.editText4);
                double overallmax = 100000;
                if (!maxprice.getText().toString().isEmpty()) {
                    max = Double.parseDouble(maxprice.getText().toString());
                }
                if (!maxoverallprice.getText().toString().isEmpty()) {
                    overallmax = Double.parseDouble(maxoverallprice.getText().toString());
                }
                /*Toast toast2 = Toast.makeText(getApplicationContext(),
                        Double.toString(max), Toast.LENGTH_SHORT);
                toast2.show();*/
                int noOfPubs = 0;
                if (pubs.getText().toString().isEmpty()) {
                    isPubNull = true;
                } else {
                    noOfPubs = Integer.parseInt(pubs.getText().toString());
                    if (noOfPubs < 1) {
                        isPubNull = true;
                    }
                }
                final String poststring = postcode.getText().toString();
                CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
                if (poststring.length() < 1 && !checkBox.isChecked()) {
                    Toast toast4 = Toast.makeText(getApplicationContext(),
                            "You need to enter a postcode.", Toast.LENGTH_SHORT);
                    toast4.show();
                    Intent previewMessage = new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(previewMessage);
                } else if (isPubNull) {
                    Toast toast3 = Toast.makeText(getApplicationContext(),
                            "You need to enter an amount of pubs 1 or above.", Toast.LENGTH_SHORT);
                    toast3.show();
                    Intent previewMessage = new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(previewMessage);
                } else if (max < 1.99) {
                    Toast toast4 = Toast.makeText(getApplicationContext(),
                            "Minimum pint price is £1.99.", Toast.LENGTH_SHORT);
                    toast4.show();
                    Intent previewMessage = new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(previewMessage);
                } else {
                    // Start NewActivity.class
                    Intent myIntent = new Intent(MainActivity.this,
                            SecondActivity.class);

                    if (checkBox.isChecked()) {
                        myIntent.putExtra("postcode", "use_the_device_loc");
                    } else {
                        myIntent.putExtra("postcode", poststring);
                    }
                    myIntent.putExtra("pubs", noOfPubs);
                    myIntent.putExtra("maxvalue", Double.toString(max));
                    myIntent.putExtra("overallmaxvalue", Double.toString(overallmax));
                    startActivity(myIntent);
                }
            }
        });
    }

}
