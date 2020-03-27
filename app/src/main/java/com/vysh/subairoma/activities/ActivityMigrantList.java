package com.vysh.subairoma.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.vysh.subairoma.dialogs.DialogUsertypeChooser;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.services.LocationChecker;
import com.vysh.subairoma.utils.CustomTextView;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONArray;
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
    private final String tilesAPI = "/getalltiles.php";
    private final String questionAPI = "/getallquestions.php";
    private final String optionsAPI = "/getalloptions.php";
    private final String countiesAPI = "/getcountries.php";
    private final String manpowersAPI = "/getmanpowers.php";
    private final String importantContactsAPI = "/getimportantcontacts.php";
    private final String importantContactsDefaultAPI = "/getimportantcontactsdefault.php";
    private final String feedbackQuestions = "/getfeedbackquestions.php";
    private int savedCount = 0, apiErrorCount = 0;
    private int apiCount = 8;
    private HashMap<String, String> fParams;


    private final String ApiDISABLE = "/deactivatesafemigrant.php";
    final String apiURLMigrant = "/savesafemigrant.php";
    final String saveResponseAPI = "/saveresponse.php";
    final String userTypeAPI = "/updateusertype.php";
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

    @BindView(R.id.llBottom)
    LinearLayout bottomLayoutMessage;
    @BindView(R.id.btnTryAgain)
    Button btnTryAgain;

    CustomTextView tvName, tvPhone, tvNavCounty;
    ImageView ivUserAvatar;

    String userType;
    ArrayList<MigrantModel> migrantModels;
    MigrantListAdapter migrantListAdapter;
    SharedPreferences sp;
    ProgressDialog progressDialog;

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
        progressDialog = new ProgressDialog(ActivityMigrantList.this);
        if (isLocationAccessAllowed())
            getUpdatedMigrantCounties();
        else
            requestLocationAccess();
        btnAddMigrant.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMigrantList.this);
            builder.setView(R.layout.dialog_disclaimer);
            builder.setPositiveButton(R.string.understand, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(ActivityMigrantList.this, ActivityRegisterMigrant.class);
                    startActivity(intent);
                }
            });
            builder.show();
        });
        if (getIntent().hasExtra("message"))
            Toast.makeText(this, getIntent().getStringExtra("message"), Toast.LENGTH_SHORT).show();

        //To Restart if exits with exception
        //Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        navView = findViewById(R.id.nav_view);
        setUpNavigationButtons();
        setUpListeners();
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getAllData();
                //progressBar.setVisibility(View.VISIBLE);
                bottomLayoutMessage.setVisibility(View.GONE);
            }
        });
        sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        savedCount = sp.getInt(SharedPrefKeys.savedCount, 0);
        if (savedCount != apiCount) {
            fParams = new HashMap<>();
            fParams.put("lang", sp.getString(SharedPrefKeys.lang, ""));
            //getAllData();
        }
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
                        Intent intentMig = new Intent(ActivityMigrantList.this, ActivityRegisterMigrant.class);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentMig);
                        break;
                    case R.id.update_usertype:
                        DialogUsertypeChooser chooser = new DialogUsertypeChooser();
                        chooser.show(getFragmentManager(), "userchooser");
                        drawerLayout.closeDrawer(GravityCompat.END);
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
            //saveLocalDataToServer();
            new ReponseSaver(this).execute();
            //Mig Percent
        }
        getSavedMigrants();
        //getMigrants();
    }

    public void updateUserType(String userType) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityMigrantList.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.generic_updating));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final HashMap<String, String> fParams = new HashMap<>();
        fParams.put("user_type", userType);
        fParams.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
        String api = ApplicationClass.getInstance().getAPIROOT() + userTypeAPI;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "Country Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ActivityMigrantList.this, getResources().getString(R.string.failed_user_update), Toast.LENGTH_SHORT).show();
                String err = error.toString();
                if (!err.isEmpty() && err.contains("NoConnection")) {
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    Log.d("mylog", "couldn't save country");
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", userToken);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ActivityMigrantList.this);
        requestQueue.add(stringRequest);

    }

    private class ReponseSaver extends AsyncTask<Void, Void, ArrayList<ArrayList<HashMap>>> {

        private ProgressDialog dialog;

        public ReponseSaver(Activity activity) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(getString(R.string.updatingToServer));
        }

        @Override
        protected ArrayList<ArrayList<HashMap>> doInBackground(Void... voids) {
            /*new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });*/
            ArrayList<ArrayList<HashMap>> allMigResponses = new ArrayList();
            for (MigrantModel migrant : migrantModels) {
                allMigResponses.add(dbHelper.getAllResponse(migrant.getMigrantId()));
            }
            return allMigResponses;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<HashMap>> arrayLists) {
            super.onPostExecute(arrayLists);
            for (ArrayList<HashMap> arrayItem : arrayLists) {
                //dialog.dismiss();
                for (HashMap itemResponse : arrayItem) {
                    saveResponseToServer(itemResponse);
                }
            }
        }
    }

    private void saveResponseToServer(final HashMap<String, String> fParams) {
        String api = ApplicationClass.getInstance().getAPIROOT() + saveResponseAPI;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Response of resp save: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.toString();
                Log.d("mylog", "Error saving feedback : " + err);
                if (!err.isEmpty() && err.contains("NoConnection")) {
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    //Log.d("mylog", "Response couldn't be saved, please check your Intenet connection.");
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", userToken);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
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
                    params.put("user_img", currModel.getMigImg() + "");
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
        String phone = sharedPreferences.getString(SharedPrefKeys.userPhone, "");
        if (phone.isEmpty())
            phone = "Phone: -";
        tvPhone.setText(phone);
        String name = sharedPreferences.getString(SharedPrefKeys.userName, "");
        if (name.isEmpty())
            name = "Name: -";
        tvName.setText(name);
        String sex = sharedPreferences.getString(SharedPrefKeys.userSex, "");
        String age = sharedPreferences.getString(SharedPrefKeys.userAge, "");
        String img = sharedPreferences.getString(SharedPrefKeys.userImg, "");
        if (age.equalsIgnoreCase("null"))
            age = "-";
        tvNavCounty.setText("Age: " + age);

        if (img.length() > 10) {
            ivUserAvatar.setImageBitmap(ImageEncoder.decodeFromBase64(img));
        } else {
            if (sex.equals("female"))
                ivUserAvatar.setImageResource(R.drawable.ic_female);
            else
                ivUserAvatar.setImageResource(R.drawable.ic_male);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
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
        FlurryAgent.logEvent("migrant_listing");
        setUpRecyclerView(migrantModels);

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
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityMigrantList.this);
        for (int i = 0; i < disableMigs.size(); i++) {
            int migId = disableMigs.get(i).getMigrantId();
            if (migId > 0)
                deactivateUser(uid, 0, disableMigs.get(i).getInactiveDate(), requestQueue);
        }
    }

    public void deactivateUser(final int uid, final int migId, final String deactivateTime, RequestQueue requestQueue) {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(ActivityMigrantList.this);
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
        requestQueue.add(getRequest);
    }


    //Below are for initialization

    private void getAllData() {
        //Showing Progress
        if (sp.getInt(SharedPrefKeys.savedTableCount, 0) != apiCount) {
            progressDialog.setMessage(getString(R.string.loading_text));
            progressDialog.setCancelable(false);
            progressDialog.show();
            Log.d("mylog", "Starting save");
            dbHelper = SQLDatabaseHelper.getInstance(ActivityMigrantList.this);
            dbHelper.getWritableDatabase();
            savedCount = 0;
            /*getTiles();
            getQuestions();
            getOptions();
            getCountries();
            getContacts(1);
            getContacts(2);
            getFeedbackQuestions();
            getManpowers();*/

            if (!sp.getBoolean(SharedPrefKeys.savedTiles, false)) {
                Log.d("mylog", "Getting tiles");
                getTiles();
            } else
                incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedQuestions, false)) {
                Log.d("mylog", "Getting questions");
                getQuestions();
            } else
                incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedOptions, false)) {
                Log.d("mylog", "Getting options");
                getOptions();
            } else
                incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedCountries, false)) {
                Log.d("mylog", "Getting countries");
                getCountries();
            } else
                incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedContacts, false)) {
                Log.d("mylog", "Getting contacts");
                getContacts(1);
                getContacts(2);
            } else {
                incrementCount();
                incrementCount();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedFeedbackQuestions, false)) {
                Log.d("mylog", "Getting feedback questions");
                getFeedbackQuestions();
            } else
                incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedManpowers, false)) {
                Log.d("mylog", "Getting manpowers");
                getManpowers();
            } else
                incrementCount();

        } else {
            Log.d("mylog", "Saved already, starting");
            //Start activity directly or show splash
            //sleepTime = 2000;
            //sleepThread.start();
            //startNextActivity();
        }
    }

    private void getTiles() {
        String api = ApplicationClass.getInstance().getAPIROOT() + tilesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got Tiles: " + response);
                parseAndSaveTiles(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedTiles, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Got Tiles error: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getTiles();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    private void showErrorResponse() {
        if (Build.VERSION.SDK_INT >= 19) {
            TransitionManager.beginDelayedTransition((ViewGroup) bottomLayoutMessage.getParent());
        }
        if (bottomLayoutMessage.getVisibility() != View.VISIBLE) {
            bottomLayoutMessage.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }
    }

    private void getQuestions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + questionAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got questions: " + response);
                parseAndSaveQuestions(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedQuestions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting questions: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getQuestions();
                apiErrorCount = 0;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };
        ;
        requestQueue.add(getRequest);
    }

    private void getContacts(int type) {
        String api;
        if (type == 1)
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsAPI;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsDefaultAPI;

        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got contacts: " + response);
                parseAndSaveContacts(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedContacts, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting questions: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else {
                    getContacts(1);
                    getContacts(2);
                }
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    private void getOptions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + optionsAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got options: " + response);
                parseAndSaveOptions(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedOptions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting options: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getOptions();
                apiErrorCount = 0;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    private void getCountries() {
        String api = ApplicationClass.getInstance().getAPIROOT() + countiesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveCountries(response);
                Log.d("mylog", "Got countries: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedCountries, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting countries: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getCountries();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    private void getManpowers() {
        String api = ApplicationClass.getInstance().getAPIROOT() + manpowersAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveManpowers(response);
                Log.d("mylog", "Got Manpowers: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedManpowers, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting manpowers: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getManpowers();
                apiErrorCount++;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    public void getFeedbackQuestions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + feedbackQuestions;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveFeedback(response);
                Log.d("mylog", "Got feedback: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedFeedbackQuestions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting countries: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else getFeedbackQuestions();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        requestQueue.add(getRequest);
    }

    private void parseAndSaveFeedback(String response) {
        try {
            JSONObject jsonQuestions = new JSONObject(response);
            boolean error = jsonQuestions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting contacts: " + response);
            } else {
                JSONArray questionArray = jsonQuestions.getJSONArray("feedback_questions");
                for (int i = 0; i < questionArray.length(); i++) {
                    JSONObject questionObject = questionArray.getJSONObject(i);

                    String qidString = questionObject.getString("question_id");
                    int qid = -1;
                    if (qidString != null && !qidString.equalsIgnoreCase("null")) {
                        qid = Integer.parseInt(qidString);
                    }
                    String qTitle = questionObject.getString("question_title");
                    String qOption = questionObject.getString("question_option");
                    String qType = questionObject.getString("feedback_type");

                    Log.d("mylog", "Feedback qid: " + qid);
                    dbHelper.insertFeedbackQuestions(qid, qTitle, qOption, qType);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing feedback questions: " + e.toString());
        }
    }

    private void parseAndSaveContacts(String response) {
        try {
            JSONObject jsonContacts = new JSONObject(response);
            boolean error = jsonContacts.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting contacts: " + response);
            } else {
                JSONArray contactsArray = jsonContacts.getJSONArray("contacts");
                for (int i = 0; i < contactsArray.length(); i++) {
                    JSONObject contactsObject = contactsArray.getJSONObject(i);
                    String cid;
                    if (contactsObject.has("country_id"))
                        cid = contactsObject.getString("country_id");
                    else
                        cid = "default";

                    int contactId;
                    if (contactsObject.has("contact_id"))
                        contactId = contactsObject.getInt("contact_id");
                    else
                        contactId = contactsObject.getInt("id");

                    String title = contactsObject.getString("title");
                    String description = contactsObject.getString("description");
                    String address = contactsObject.getString("address");
                    String phone = contactsObject.getString("phone");
                    String email = contactsObject.getString("email");
                    String website = contactsObject.getString("website");
                    Log.d("mylog", cid);
                    if (cid.equalsIgnoreCase("default")) {
                        Log.d("myimplog", "Saving default:" + contactId);
                        dbHelper.insertImportantContactsDefault(contactId, title, description, address, phone, email, website);
                        Log.d("mylog", "Inserting Default: " + title);
                    } else {
                        Log.d("myimplog", "Saving specific:" + cid);
                        dbHelper.insertImportantContacts(contactId, cid, title, description, address, phone, email, website);
                    }
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing contacts: " + e.toString());
        }
    }

    private void parseAndSaveTiles(String response) {
        try {
            JSONObject jsonTiles = new JSONObject(response);
            boolean error = jsonTiles.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting tiles: " + response);
            } else {
                JSONArray jsonTileArray = jsonTiles.getJSONArray("tiles");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempTile = jsonTileArray.getJSONObject(i);
                    int id = tempTile.getInt("tile_id");
                    String tileName = tempTile.getString("tile_title");
                    String tileDescription = tempTile.getString("tile_description");
                    String tileType = tempTile.getString("tile_type");
                    int tileOrder = tempTile.getInt("tile_order");
                    Log.d("mylog", tileName);
                    dbHelper.insertTile(id, tileName, tileDescription, tileType, tileOrder);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing tiles: " + e.toString());
        }
    }

    private void parseAndSaveQuestions(String response) {
        try {
            JSONObject jsonQuestions = new JSONObject(response);
            boolean error = jsonQuestions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting questions: " + response);
            } else {
                JSONArray jsonTileArray = jsonQuestions.getJSONArray("questions");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempQuestion = jsonTileArray.getJSONObject(i);
                    int id = tempQuestion.getInt("question_id");
                    int tileId = tempQuestion.getInt("tile_id");
                    String step = tempQuestion.getString("question_step");
                    String title = tempQuestion.getString("question_title");
                    String description = tempQuestion.getString("question_description");
                    String confDesc = tempQuestion.getString("conflict_description");
                    Log.d("mylog", "Conflict Desc form server: " + confDesc);
                    String condition = tempQuestion.getString("condition");
                    String variable = tempQuestion.getString("variable");
                    String order = tempQuestion.getString("order");
                    String responseType = tempQuestion.getString("response_type");
                    String questionCall = tempQuestion.getString("question_call");
                    Log.d("mylog", "Got Number: " + questionCall);
                    String questionVideo = tempQuestion.getString("question_video");
                    Log.d("mylog", "Got Link: " + questionVideo);
                    dbHelper.insertQuestion(id, tileId, order, step, title, description, condition, responseType, variable, confDesc, questionCall, questionVideo);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing questions: " + e.toString());
        }
    }

    private void parseAndSaveOptions(String response) {
        try {
            JSONObject jsonOptions = new JSONObject(response);
            boolean error = jsonOptions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting options: " + response);
            } else {
                JSONArray jsonOptionsArray = jsonOptions.getJSONArray("options");
                for (int i = 0; i < jsonOptionsArray.length(); i++) {
                    JSONObject tempOption = jsonOptionsArray.getJSONObject(i);
                    int qid = tempOption.getInt("question_id");
                    int oid = tempOption.getInt("option_id");
                    String option = tempOption.getString("option_text");
                    Log.d("mylog", "Saving options for qid: " + qid + " option: " + option);
                    dbHelper.insertOption(qid, oid, option);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing options: " + e.toString());
        }
    }

    private void parseAndSaveCountries(String response) {
        try {
            JSONObject jsonCountries = new JSONObject(response);
            boolean error = jsonCountries.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting countries: " + response);
            } else {
                JSONArray jsonTileArray = jsonCountries.getJSONArray("countries");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempCountry = jsonTileArray.getJSONObject(i);
                    String id = tempCountry.getString("country_id");
                    String name = tempCountry.getString("country_name");
                    int status = tempCountry.getInt("country_status");
                    int blacklist = tempCountry.getInt("country_blacklist");
                    String order = null;
                    if (!(tempCountry.get("country_order") == null))
                        order = tempCountry.getString("country_order");
                    Log.d("mylog", "Country id: " + tempCountry.getString("country_id") + " Country name: " + tempCountry.getString("country_name")
                            + " Status: " + tempCountry.getInt("country_status") + " Blacklist: " + tempCountry.getInt("country_blacklist"));
                    dbHelper.insertCountry(id, name, status, blacklist, order);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing countries: " + e.toString());
        }
    }

    private void parseAndSaveManpowers(String response) {
        try {
            JSONObject jsonMan = new JSONObject(response);
            boolean error = jsonMan.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting manpowers: " + response);
            } else {
                JSONArray jsonManpowerArray = jsonMan.getJSONArray("manpowers");
                for (int i = 0; i < jsonManpowerArray.length(); i++) {
                    JSONObject tempManpower = jsonManpowerArray.getJSONObject(i);
                    int id = tempManpower.getInt("id");
                    String name = tempManpower.getString("manpower");
                    dbHelper.insertManpower(id, name);
                }
            }
        } catch (JSONException ex) {
            Log.d("mylog", "Error parsing manpower: " + response);
        }
        incrementCount();
    }

    public synchronized void incrementCount() {
        ++savedCount;
        Log.d("mylog", "Incremented Count: " + savedCount);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SharedPrefKeys.savedCount, savedCount);
        editor.commit();
        if (savedCount == apiCount) {
            //checkSleep();
            //startInfoActivity();
            progressDialog.dismiss();
        }
    }
}
