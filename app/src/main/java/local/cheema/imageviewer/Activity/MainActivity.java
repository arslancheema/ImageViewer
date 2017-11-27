package local.cheema.imageviewer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import local.cheema.imageviewer.R;
import local.cheema.imageviewer.Adapter.GalleryAdapter;
import local.cheema.imageviewer.Model.Image;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "https://api.androidhive.info/json/glide.json";
    private ArrayList<Image> images;
    private ProgressDialog pDialog;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("ImageViewer");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getApplicationContext(), images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

         recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                FragmentSlideShow newFragment = FragmentSlideShow.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        downloadImages();
    }

    private void downloadImages() {

        pDialog.setMessage("Downloading Images ...");
        pDialog.show();

        // Fetch the Tags from EditText Preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String tags =  prefs.getString(getApplicationContext().getString(R.string.pref_tags_key),
                getApplicationContext().getString(R.string.pref_tags_default));

        Toast.makeText(this, "Tags: " + tags, Toast.LENGTH_SHORT).show();

        String requestURL = "https://pixabay.com/api/?key=7161440-3baa487e12c9cbc34fee421fc&q=" + tags +
                "&image_type=photo&editors_choice=true";

        // OKHttpClient request to fetch the images from the API.
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestURL)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                if (response.isSuccessful()) {
                    String result = response.body().string();
                    Log.v(TAG, "Response Body " + result);

                    try {
                        JSONObject jsonObject = new JSONObject(result);

                        JSONArray hits = jsonObject.getJSONArray("hits");
                        Log.v(TAG, "Length " + hits.length());

                        for (int i = 0; i < hits.length(); i++) {
                            JSONObject object = hits.getJSONObject(i);
                            Image image = new Image();
                            image.setName("Tags: "+ object.getString("tags"));
                            image.setImage(object.getString("userImageURL"));
                            images.add(image);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            Log.v(TAG, "runOnUiThread");
                            mAdapter.notifyDataSetChanged();
                                pDialog.dismiss();
                            }
                        });


                    } catch (JSONException e) {
                        Log.v(TAG, "Error " + e.getMessage());
                        e.printStackTrace();
                    }


                } else {
                    Log.v(TAG, "Error in getting response" );
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings_menu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        images.clear();
        downloadImages();
    }
}