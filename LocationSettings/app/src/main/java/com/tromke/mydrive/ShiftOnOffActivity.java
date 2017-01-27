package com.tromke.mydrive;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.tromke.mydrive.Constants.ConstantsSharedPreferences;
import com.tromke.mydrive.Models.DriverData;
import com.tromke.mydrive.util.ConnectionManager;

import java.util.Map;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.transmitter.model.HTShift;
import io.hypertrack.lib.transmitter.model.HTShiftParams;
import io.hypertrack.lib.transmitter.model.HTShiftParamsBuilder;
import io.hypertrack.lib.transmitter.model.callback.HTShiftStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;

public class ShiftOnOffActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Switch.OnCheckedChangeListener,
        TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {
    private static Boolean isShiftStarted = false;
    private String hyperTrackId;
    private ProgressDialog loadingProgress;
    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    DriverData data;
    Switch actionStatus;
    private ViewPager viewPager;
    public static String checkst = "2";
    HTTransmitterService transmitterService;
    HTShiftParamsBuilder htShiftParamsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_on_off);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        HyperTrack.setPublishableApiKey("pk_ef36da6cd4f2c4570f1600f587cc0d7edb95bfb5", getApplicationContext());
        HTTransmitterService.initHTTransmitter(getApplicationContext());
        try {
            checkst = getIntent().getStringExtra("checkstate");
        } catch (Exception ex) {
            Log.e("Error", ex.getLocalizedMessage().toString());
        }

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Trips"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        Pager pagerAdapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(this);
        tabLayout.setOnTabSelectedListener(this);

        isShiftStarted = ParseApplication.getSharedPreferences().getBoolean(ConstantsSharedPreferences.SHIFT_STARTED, false);

        loadingProgress = new ProgressDialog(ShiftOnOffActivity.this,
                ProgressDialog.THEME_HOLO_LIGHT);
        loadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingProgress.setTitle(getResources().getString(R.string.app_name));
        loadingProgress.setCancelable(false);
        transmitterService = HTTransmitterService.getInstance(this);
        htShiftParamsBuilder = new HTShiftParamsBuilder();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shift, menu);
        MenuItem toggleService = menu.findItem(R.id.action_status);
        actionStatus = (Switch) toggleService.getActionView();
        actionStatus.setChecked(false);
        actionStatus.setText("Offline");
        if (ParseApplication.getSharedPreferences().getBoolean(ConstantsSharedPreferences.SHIFT_STARTED, false)&&!checkst.equals("2")) {
            actionStatus.setText("Online");
            actionStatus.setChecked(true);
//            actionStatus.setText("Offline");
//            actionStatus.setChecked(false);
        }
        actionStatus.setOnCheckedChangeListener(this);

        return super.onCreateOptionsMenu(menu);
    }


    public boolean checkGPS() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return enabled;
    }

    public void showGpsAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ShiftOnOffActivity.this);
        // set title
        alertDialogBuilder.setTitle("Pola Driver");
        // set dialog message
        alertDialogBuilder
                .setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 201);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void startAndStopShift() {
        if (isShiftStarted == false) {
            loadingProgress.setMessage("Starting shift...");
            loadingProgress.show();
            hyperTrackId = ParseApplication.getSharedPreferences().getString(ConstantsSharedPreferences.HYPERTRACK_ID, ConstantsSharedPreferences.HYPERTRACK_ID);
            HTShiftParams htShiftParams = htShiftParamsBuilder.setDriverID(hyperTrackId).createHTShiftParams();
            transmitterService.startShift(htShiftParams, new HTShiftStatusCallback() {
                @Override
                public void onSuccess(HTShift htShift) {
                    if (loadingProgress != null)
                        loadingProgress.cancel();
                    ParseApplication.getSharedPreferences().edit().putBoolean(ConstantsSharedPreferences.SHIFT_STARTED, true).commit();
                }

                @Override
                public void onError(Exception error) {
                    if (loadingProgress != null)
                        loadingProgress.cancel();
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            loadingProgress.setMessage("Stoping shift...");
            loadingProgress.show();
            transmitterService.endShift(new HTShiftStatusCallback() {
                @Override
                public void onSuccess(HTShift htShift) {
                    if (loadingProgress != null)
                        loadingProgress.cancel();
                    ParseApplication.getSharedPreferences().edit().putBoolean(ConstantsSharedPreferences.SHIFT_STARTED, false).commit();
                }

                @Override
                public void onError(Exception error) {
                    if (loadingProgress != null)
                        loadingProgress.cancel();
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            });

        }
        isShiftStarted = !isShiftStarted;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionLogout:
                if (ConnectionManager.getInstance(getApplicationContext()).isDeviceConnectedToInternet()) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ShiftOnOffActivity.this, ActRegistration.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_internet),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 201:
                startAndStopShift();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!checkst.equals("2")) {
            if (isChecked) {
                buttonView.setText("Online");

            } else {
                buttonView.setText("Offline");
            }
            if (ConnectionManager.getInstance(getApplicationContext()).isDeviceConnectedToInternet()) {
                if (checkGPS()) {
                    startAndStopShift();
                } else {
                    showGpsAlert();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Your approval is not complete. Please Contact Customer Support", Toast.LENGTH_LONG).show();
            actionStatus.setChecked(true);
            actionStatus.setChecked(false);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.myprofile) {
            // Handle the camera action
            if (data != null) {
                Intent intent = new Intent(ShiftOnOffActivity.this, Driver_Profile.class);
                intent.putExtra("driver_profile", data);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Check Network Please", Toast.LENGTH_SHORT).show();
                onStart();
            }
        } else if (id == R.id.myDocuments) {
            if (data != null) {
                Intent intent = new Intent(ShiftOnOffActivity.this, Driver_Documents.class);
                intent.putExtra("driver_documents", data);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Check your Network Please", Toast.LENGTH_SHORT).show();
                onStart();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onStart() {
        checkst = "2";
        invalidateOptionsMenu();
        super.onStart();
        invalidateOptionsMenu();
        Driver_data();
    }

    public void Driver_data() {

        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Driver");
        com.google.firebase.database.Query query = reference.orderByChild("UUID").equalTo(Uid);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                data = dataSnapshot.getValue(DriverData.class);
                checkst = data.getHypertrack_id();
                ParseApplication.getSharedPreferences().edit().putString(ConstantsSharedPreferences.HYPERTRACK_ID, data.getHypertrack_id()).commit();
                invalidateOptionsMenu();
                try {
                    Picasso.with(getApplicationContext()).load(data.getProfileImage()).resize(150, 150).placeholder(R.drawable.profile)
                            .into((ImageView) findViewById(R.id.profile_image));
                } catch (Exception ex) {
                    Log.e("Error", ex.toString());
                }

                // if user change his device he will still able to receive notifications
                Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                String Token = String.valueOf(value.get("notification_token"));
                if (!FirebaseInstanceId.getInstance().getToken().equals(Token)) {
                    String key = dataSnapshot.getKey();
                    reference.child(key).child("notification_token").setValue(FirebaseInstanceId.getInstance().getToken());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                data = dataSnapshot.getValue(DriverData.class);
                checkst = data.getHypertrack_id();
                ParseApplication.getSharedPreferences().edit().putString(ConstantsSharedPreferences.HYPERTRACK_ID, data.getHypertrack_id()).commit();
                invalidateOptionsMenu();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
