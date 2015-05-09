package io.github.dnivra26.unsamayalarayil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class NewDeviceActivity extends Activity {
    private static final String TAG = "location";

    Button submitButton;
    EditText name;
    EditText alertPercentage;
    EditText phoneNumber;
    String deviceIdFromIntent;
    TextView deviceId;
    Spinner reminderActionsSpinner;
    LinearLayout locationLayout;
    TextView latLng;
    Button pickLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);

        deviceIdFromIntent = getIntent().getStringExtra(GcmIntentService.device_name);
        deviceId = (TextView) findViewById(R.id.deviceId);
        deviceId.append(" " + deviceIdFromIntent);
        name = (EditText) findViewById(R.id.deviceName);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        alertPercentage = (EditText) findViewById(R.id.alertPercentage);
        submitButton = (Button) findViewById(R.id.deviceSubmitButton);
        reminderActionsSpinner = (Spinner) findViewById(R.id.actions_spinner);
        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        latLng = (TextView) findViewById(R.id.latLng);
        pickLocation = (Button) findViewById(R.id.pickLocation);

        pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(NewDeviceActivity.this,MapActivity.class),111);
            }
        });

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.reminder_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderActionsSpinner.setAdapter(adapter);
        reminderActionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).toString().equals("SMS")) {
                    locationLayout.setVisibility(View.GONE);
                    phoneNumber.setVisibility(View.VISIBLE);
                    phoneNumber.requestFocus();
                } else if (adapterView.getItemAtPosition(i).toString().equals("location")) {
                    phoneNumber.setVisibility(View.GONE);
                    locationLayout.setVisibility(View.VISIBLE);
                } else {
                    phoneNumber.setVisibility(View.GONE);
                    locationLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] latlng = latLng.getText().toString().split(",");
                saveAction(name.getText().toString(), reminderActionsSpinner.getSelectedItem().toString(),
                        phoneNumber.getText().toString(), latlng[0], latlng[1]);

                NewDevice newDevice = new NewDevice(getUserId(), deviceIdFromIntent, name.getText().toString(), Integer.parseInt(alertPercentage.getText().toString()));
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(RegisterDeviceFragment.BASE_URL)
                        .build();
                RetrofitInterface apiService =
                        restAdapter.create(RetrofitInterface.class);
                apiService.addItem(newDevice, new Callback<RegistrationResponse>() {
                    @Override
                    public void success(RegistrationResponse registrationResponse, Response response) {
                        Toast.makeText(NewDeviceActivity.this, "Item successfully added", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(NewDeviceActivity.this, AllItemsActivity.class));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(NewDeviceActivity.this, "Failed to add item", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void sendMessage(String item, String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, "Please send some amount of " + item, null, null);

    }

    private void saveAction(String itemName, String action, String phoneNumber, String lattitude, String longitude) {
        try {
            DB snappydb = DBFactory.open(this);
            snappydb.put(itemName, new ItemAction(itemName, action, phoneNumber, lattitude, longitude));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }


    private String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterDeviceFragment.class.getSimpleName(),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(RegisterDeviceFragment.USER_ID, "");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_new_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111){
            latLng.setText(data.getStringExtra("latlng"));
        }
    }
}
