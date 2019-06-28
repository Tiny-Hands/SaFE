package com.vysh.subairoma.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.google.android.material.navigation.NavigationView;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.adapters.MigrantListAdapter;
import com.vysh.subairoma.adapters.RecyclerItemTouchHelper;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.services.LocationChecker;
import com.vysh.subairoma.utils.CustomTextView;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/16/2017.
 */

public class ActivityMigrantList extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private final String APIGetMig = "/getmigrants.php";
    private final String ApiDISABLE = "/deactivatemigrant.php";
    final String apiURLMigrant = "/savemigrant.php";
    private final int REQUEST_LOCATION = 1;
    private String userToken;

    @BindView(R.id.rvMigrants)
    RecyclerView recyclerView;
    @BindView(R.id.btnAddMigrant)
    Button btnAddMigrant;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.ivHam)
    ImageView ivHam;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    NavigationView navView;

    CustomTextView tvName, tvPhone, tvNavCounty;
    ImageView ivUserAvatar;

    String userType;
    ArrayList<MigrantModel> migrantModels;
    MigrantListAdapter migrantListAdapter;

    RequestQueue requestQueue;
    SQLDatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_migrant);
        requestQueue = Volley.newRequestQueue(ActivityMigrantList.this);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        dbHelper = SQLDatabaseHelper.getInstance(this);
        userType = ApplicationClass.getInstance().getUserType();
        userToken = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, "");
        migrantModels = new ArrayList();
        if (isLocationAccessAllowed())
            getUpdatedMigrantCounties();
        else
            requestLocationAccess();
        btnAddMigrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMigrantList.this, ActivityRegister.class);
                intent.putExtra("migrantmode", true);
                startActivity(intent);
            }
        });

        navView = findViewById(R.id.nav_view);
        setUpNavigationButtons();
        setUpListeners();
    }

    private void setUpListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ivHam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navView.getMenu().findItem(R.id.nav_tutorial).setVisible(false);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(ActivityMigrantList.this, ActivityProfileEdit.class);
                        intent.putExtra("userType", SharedPrefKeys.helperUser);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intent);
                        break;
                    case R.id.nav_addmigrants:
                        Intent intentMig = new Intent(ActivityMigrantList.this, ActivityRegister.class);
                        intentMig.putExtra("migrantmode", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentMig);
                        break;
                    case R.id.nav_about:
                        Intent intentAbout = new Intent(ActivityMigrantList.this, ActivityAboutUs.class);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentAbout);
                        break;
                    case R.id.nav_contact:
                        Intent intentContact = new Intent(ActivityMigrantList.this, ActivityAboutUs.class);
                        intentContact.putExtra("contact", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentContact);
                        break;
                    case R.id.nav_faq:
                        Intent intentFaq = new Intent(ActivityMigrantList.this, ActivityAboutUs.class);
                        intentFaq.putExtra("faq", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentFaq);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (InternetConnectionChecker.isNetworkConnected(ActivityMigrantList.this)) {
            //Migrant that's not registered is being registered
            //This will save to server then display migrant list
            saveLocalDataToServer();
            //Mig Percent
        } else {
            getSavedMigrants();
        }
        //getMigrants();
    }

    private void saveLocalDataToServer() {
        migrantModels = getMigrantsToDisplay(dbHelper.getMigrants());
        ArrayList<MigrantModel> migsLocal = new ArrayList<>();
        for (MigrantModel migModel : migrantModels) {
            if (migModel.getMigrantId() < 0) {
                Log.d("mylog", "Temp Mig: " + migModel.getMigrantName() + ", Mid ID: " + migModel.getMigrantId());
                migsLocal.add(migModel);
            }
        }
        if (migsLocal.size() > 0)
            //This will save to server then display migrant list
            saveMigToServer(migsLocal);
        else
            //Displaying migrant list
            getSavedMigrants();
    }

    private void saveMigToServer(final ArrayList<MigrantModel> migModel) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityMigrantList.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
        for (int i = 0; i < migModel.size(); i++) {
            final int currCount = i;
            final MigrantModel currModel = migModel.get(i);
            StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("mylog", "response : " + response);
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        boolean error = jsonObj.getBoolean("error");
                        if (!error)
                            makeChangesInLocalDB(currModel.getMigrantId(), jsonObj.getInt("migrant_id"));
                        if (currCount == migModel.size() - 1) {
                            progressDialog.dismiss();
                            getSavedMigrants();
                        }
                    } catch (JSONException e) {
                        Log.d("mylog", "Exceptions: " + e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (currCount == migModel.size() - 1) {
                        progressDialog.dismiss();
                        getSavedMigrants();
                    }
                    String err = error.toString();
                    Log.d("mylog", "error : " + err);
                    showSnackbar(getString(R.string.server_noconnect));
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("full_name", currModel.getMigrantName());
                    params.put("phone_number", currModel.getMigrantPhone());
                    params.put("age", currModel.getMigrantAge() + "");
                    params.put("gender", currModel.getMigrantSex());
                    params.put("user_id", currModel.getUserId() + "");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    Log.d("mylog", "putting authheader update percent" + userToken);
                    headers.put("Authorization", userToken);
                    return headers;
                }
            };
            saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(saveRequest);
        }

    }

    private void makeChangesInLocalDB(int migrantIdOld, int migrantIdNew) {
        dbHelper.makeMigIdChanges(migrantIdOld, migrantIdNew);
    }

    private void setUpNavigationButtons() {
        //Hiding Delete Migrant Button
        Menu menu = navView.getMenu();
        MenuItem target = menu.findItem(R.id.delete_migrant);
        target.setVisible(false);

        View view = navView.getHeaderView(0);
        if (view == null)
            Log.d("mylog", "Header view Null");
        if (navView == null)
            Log.d("mylog", "Nav View Null");
        tvName = view.findViewById(R.id.tvMName);
        tvNavCounty = view.findViewById(R.id.tvMCountry);
        tvPhone = view.findViewById(R.id.tvMPhone);
        ivUserAvatar = view.findViewById(R.id.ivUserAva);


        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        tvPhone.setText(sharedPreferences.getString(SharedPrefKeys.userPhone, ""));
        tvName.setText(sharedPreferences.getString(SharedPrefKeys.userName, ""));
        String sex = sharedPreferences.getString(SharedPrefKeys.userSex, "");
        String age = sharedPreferences.getString(SharedPrefKeys.userAge, "");
        String img = sharedPreferences.getString(SharedPrefKeys.userImg, "");
        tvNavCounty.setText("Age: " + age);

        if (img.length() > 10) {
            ivUserAvatar.setImageBitmap(ImageEncoder.decodeFromBase64(img));
        } else {
            if (sex.equals("male"))
                ivUserAvatar.setImageResource(R.drawable.ic_male);
            else
                ivUserAvatar.setImageResource(R.drawable.ic_female);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getUpdatedMigrantCounties();
                break;
        }
    }

    private void requestLocationAccess() {
        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private boolean isLocationAccessAllowed() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void getUpdatedMigrantCounties() {
        Intent intent = new Intent(ActivityMigrantList.this, LocationChecker.class);
        startService(intent);
    }

    private void getSavedMigrants() {
        migrantModels = getMigrantsToDisplay(dbHelper.getMigrants());
        migrantListAdapter = new MigrantListAdapter();
        /*if (userType == 1) {
            Log.d("mylog", "Usertype: " + userType);
            setUpRecyclerView(migrantModels);
        } else {*/
        Log.d("mylog", "User ID: " + ApplicationClass.getInstance().getSafeUserId() + " Migrant ID: " + ApplicationClass.getInstance().getMigrantId()
                + " UserType: " + userType);
        if (userType == null) {
            Toast.makeText(ActivityMigrantList.this, getString(R.string.restart_app), Toast.LENGTH_SHORT).show();
        }
        if (migrantModels.size() == 1 && userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
            int migId = migrantModels.get(0).getMigrantId();
            ApplicationClass.getInstance().setMigrantId(migId);
            String cid = dbHelper.getResponse(migId, "mg_destination");
            Log.d("mylog", "Country ID: " + cid);
            if (cid != null && !cid.isEmpty()) {
                Intent intent = new Intent(ActivityMigrantList.this, ActivityTileChooser.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("countryId", cid);
                intent.putExtra("migrantName", migrantModels.get(0).getMigrantName());
                intent.putExtra("migrantPhone", migrantModels.get(0).getMigrantPhone());
                intent.putExtra("migrantGender", migrantModels.get(0).getMigrantSex());
                CountryModel savedCountry = dbHelper.getCountry(cid);
                Log.d("mylog", "Country name: " + savedCountry.getCountryName());
                intent.putExtra("countryName", savedCountry.getCountryName().toUpperCase());
                intent.putExtra("countryStatus", savedCountry.getCountrySatus());
                intent.putExtra("countryBlacklist", savedCountry.getCountryBlacklist());
                startActivity(intent);
            } else {
                Intent intent = new Intent(ActivityMigrantList.this, ActivityTileChooser.class);
                intent.putExtra("countryId", "");
                intent.putExtra("migrantName", migrantModels.get(0).getMigrantName());
                intent.putExtra("migrantPhone", migrantModels.get(0).getMigrantPhone());
                intent.putExtra("migrantGender", migrantModels.get(0).getMigrantSex());
                intent.putExtra("countryName", "");
                intent.putExtra("countryStatus", -1);
                intent.putExtra("countryBlacklist", -1);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } else if (migrantModels.size() < 1 && ApplicationClass.getInstance().getMigrantId() == -1) {
            Intent intent = new Intent(ActivityMigrantList.this, ActivityRegister.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("migrantmode", true);
            startActivity(intent);
        } else {
            FlurryAgent.logEvent("migrant_listing");
            setUpRecyclerView(migrantModels);
        }

    }

    private ArrayList<MigrantModel> getMigrantsToDisplay(ArrayList<MigrantModel> migs) {
        ArrayList<MigrantModel> tempMig = new ArrayList<>();
        ArrayList<MigrantModel> disableMigs = new ArrayList<>();
        for (int i = 0; i < migs.size(); i++) {
            if (migs.get(i).getInactiveDate() == null || migs.get(i).getInactiveDate().isEmpty() || (migs.get(i).getInactiveDate().length() < 5)) {
                tempMig.add(migs.get(i));
            } else {
                disableMigs.add(migs.get(i));
            }
        }
        sendDeactivationStatus(disableMigs);
        return tempMig;
    }

    private void showSnackbar(String msg) {
        /*Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();*/
        Toast.makeText(ActivityMigrantList.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setUpRecyclerView(ArrayList<MigrantModel> migrantModels) {
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityMigrantList.this));
        Log.d("mylog", "Number of migrants: " + migrantModels.size());
        migrantListAdapter.setMigrants(migrantModels);
        recyclerView.setAdapter(migrantListAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MigrantListAdapter.MigrantHolder) {
            // get the removed item name to display it in snack bar
            String name = migrantModels.get(viewHolder.getAdapterPosition()).getMigrantName();

            // backup of removed item for undo purpose
            final MigrantModel deletedMig = migrantModels.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view

            final int uid = ApplicationClass.getInstance().getSafeUserId();
            final int mid = migrantModels.get(viewHolder.getAdapterPosition()).getMigrantId();
            long time = System.currentTimeMillis();
            dbHelper.insertMigrantDeletion(mid, uid, time + "");
            ;
            migrantListAdapter.removeItem(viewHolder.getAdapterPosition());
            deactivateUser(uid, mid, time + "", null);

            // showing snack bar with Undo option
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    migrantListAdapter.restoreItem(deletedMig, deletedIndex);
                    dbHelper.insertMigrantDeletion(mid, uid, "");
                }
            });
            builder.setMessage(R.string.mig_delete_confirmation);
            builder.show();
            builder.setCancelable(false);
            /*
            Snackbar snackbar = Snackbar
                    .make(rootLayout, "Migrant Deleted!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    migrantListAdapter.restoreItem(deletedMig, deletedIndex);
                    dbHelper.insertMigrantDeletion(mid, uid, "");
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            */
        }
    }

    public void sendDeactivationStatus(ArrayList<MigrantModel> disableMigs) {
        int uid = ApplicationClass.getInstance().getSafeUserId();
        RequestQueue queue = Volley.newRequestQueue(ActivityMigrantList.this);
        for (int i = 0; i < disableMigs.size(); i++) {
            int migId = disableMigs.get(i).getMigrantId();
            if (migId > 0)
                deactivateUser(uid, 0, disableMigs.get(i).getInactiveDate(), queue);
        }
    }

    public void deactivateUser(final int uid, final int migId, final String deactivateTime, RequestQueue queue) {
        if (queue == null)
            queue = Volley.newRequestQueue(ActivityMigrantList.this);
        String api = ApplicationClass.getInstance().getAPIROOT() + ApiDISABLE;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "deactivation response : " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error exception: " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                int user_id = ApplicationClass.getInstance().getSafeUserId();
                params.put("user_id", uid + "");
                params.put("migrant_id", migId + "");
                params.put("inactive_date", deactivateTime);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", userToken);
                return headers;
            }
        };
        queue.add(getRequest);
    }
}
