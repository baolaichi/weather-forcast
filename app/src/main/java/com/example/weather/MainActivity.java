package com.example.weather;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;  // Để kiểm tra và yêu cầu quyền
import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

private DrawerLayout drawerLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long UPDATE_INTERVAL = 5 * 60 * 1000; // 5 phút

    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;


    EditText edtSearch;
Button btnSearch, btnNext;
TextView txtName;
    TextView txtCountry;
    TextView txtTemp;
    TextView txtStatus;
    TextView txtHumidity;
    TextView txtCloud;
    TextView txtWind;
    TextView txtDay;
ImageView imgIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.Toolbar_view);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        Anhxa();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String City = edtSearch.getText().toString();
                GetCurrentWeatherData(City);
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Gọi phương thức lấy vị trí tự động
        getCurrentLocation();

        // cập nhật thời tiết 5 phút trên lần
        handler.postDelayed(weatherUpdateRunnable, UPDATE_INTERVAL);

        // Bắt đầu việc cập nhật thời gian theo từng giây
        startUpdatingTime();
    }

    private void startUpdatingTime() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                // Lấy thời gian hiện tại
                long currentTimeMillis = System.currentTimeMillis();
                Date currentDate = new Date(currentTimeMillis);

                // Định dạng thời gian hiện tại
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedTime = simpleDateFormat.format(currentDate);

                // Cập nhật TextView với thời gian hiện tại
                txtDay.setText(formattedTime);

                // Cập nhật lại sau 1 giây
                timeHandler.postDelayed(this, 1000); // 1000 ms = 1 giây
            }
        };

        // Bắt đầu thực thi Runnable để cập nhật thời gian
        timeHandler.post(timeRunnable);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập nếu chưa được cấp
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        // Lấy tọa độ hiện tại
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Gọi API lấy dữ liệu thời tiết dựa trên vị trí hiện tại
                    getCurrentWeatherByLocation(latitude, longitude);
                }
            }


        });
    }

    private void getCurrentWeatherByLocation(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=ca0d9a31cff98d7dd0f5ea320f341e21&units=metric";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("API_RESPONSE", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String day = jsonObject.getString("dt");
                            String City = jsonObject.getString("name");
                            txtCountry.setText(City);

                            // Xử lý ngày
                            long timestamp = Long.parseLong(day);
                            Date date = new Date(timestamp * 1000L);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm:ss");
                            String formattedDate = simpleDateFormat.format(date);
                            txtDay.setText(formattedDate);

                            JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
                            JSONObject jsonObjectWeather = jsonArrayWeather.getJSONObject(0);
                            String status = jsonObjectWeather.getString("main");
                            String icon = jsonObjectWeather.getString("icon");

                            Picasso.get().load("https://openweathermap.org/img/wn/" + icon + ".png").into(imgIcon);
                            txtStatus.setText(status);

                            JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                            String nhietdo = jsonObjectMain.getString("temp");
                            String doam = jsonObjectMain.getString("humidity");

                            Double a = Double.valueOf(nhietdo);
                            String NhietDo = String.valueOf(a.intValue());
                            txtTemp.setText(NhietDo + "C");
                            txtHumidity.setText(doam + "%");

                            JSONObject jsonObjectWind = jsonObject.getJSONObject("wind");
                            String gio = jsonObjectWind.getString("speed");
                            txtWind.setText(gio + "m/s");

                            JSONObject jsonObjectCloud = jsonObject.getJSONObject("clouds");
                            String May = jsonObjectCloud.getString("all");
                            txtCloud.setText(May + "%");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("API_ERROR", error.toString());
                    }
                });

        requestQueue.add(stringRequest);
    }

    private final Runnable weatherUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // Lấy vị trí và cập nhật thời tiết
            getCurrentLocation();

            // Tiếp tục chạy lại runnable sau 5 phút
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        // Dừng việc cập nhật tự động khi activity bị hủy
        handler.removeCallbacks(weatherUpdateRunnable);
        timeHandler.removeCallbacks(timeRunnable);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }


    public void GetCurrentWeatherData(String data){
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://api.openweathermap.org/data/2.5/weather?q="+data+"&appid=ca0d9a31cff98d7dd0f5ea320f341e21&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("API_RESPONSE", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String day = jsonObject.getString("dt");
                            String City = jsonObject.getString("name");
                            txtCountry.setText( City);
                            // xử lý ngày
                            long timestamp = Long.parseLong(day); // Chuyển 'day' từ String sang long
                            Date date = new Date(timestamp * 1000L); // Chuyển timestamp từ giây sang mili giây (Vì timestamp trong API thường ở đơn vị giây)
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm:ss");
                            String formattedDate = simpleDateFormat.format(date); // Định dạng đối tượng 'date' đã chuyển đổi
                            txtDay.setText(formattedDate); // Hiển thị ngày đã định dạng

                            JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
                            JSONObject jsonObjectWeather = jsonArrayWeather.getJSONObject(0);
                            String status = jsonObjectWeather.getString("main");
                            String icon = jsonObjectWeather.getString("icon");

                            Picasso.get().load("https://openweathermap.org/img/wn/" + icon + ".png");
                            txtStatus.setText(status);
                            JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                            String nhietdo = jsonObjectMain.getString("temp");
                            String doam = jsonObjectMain.getString("humidity");

                            Double a = Double.valueOf(nhietdo);
                            String NhietDo = String.valueOf(a.intValue());
                            txtTemp.setText(NhietDo+"C");
                            txtHumidity.setText(doam+"%");

                            JSONObject jsonObjectWind =jsonObject.getJSONObject("wind");
                            String gio = jsonObjectWind.getString("speed");
                            txtWind.setText(gio+"m/s");
                            JSONObject jsonObjectCloud = jsonObject.getJSONObject("clouds");
                            String May = jsonObjectCloud.getString("all");
                            txtCloud.setText(May+"%");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(stringRequest);
    }

    private void Anhxa() {
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        btnSearch = (Button) findViewById(R.id.BtnSearch);
        btnNext = (Button) findViewById(R.id.BtnNext);
        txtName = (TextView) findViewById(R.id.txtNameAcc);
        txtHumidity = (TextView) findViewById(R.id.DoAm);
        txtCloud = (TextView) findViewById(R.id.May);
        txtWind = (TextView) findViewById(R.id.Gio);
        txtTemp = (TextView) findViewById(R.id.txtNhiet);
        txtStatus = (TextView) findViewById(R.id.txtTrangThai);
        txtDay = (TextView) findViewById(R.id.txtDay);
        txtCountry = (TextView) findViewById(R.id.txtCity);
        imgIcon = (ImageView) findViewById(R.id.IMGIcon);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.Danh_sach){
            
        } else if (id == R.id.Setting) {
            
        } else if (id == R.id.Bao) {
            
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}