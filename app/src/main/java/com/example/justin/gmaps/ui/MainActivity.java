package com.example.justin.gmaps.ui;


import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.justin.gmaps.R;
import com.example.justin.gmaps.datamodel.data.GoogleMapData;
import com.example.justin.gmaps.datamodel.data.LocationData;
import com.example.justin.gmaps.googleMap.GoogleMapUtils;
import com.example.justin.gmaps.location.LocationHandler;
import com.example.justin.gmaps.service.GPSService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Intent mServiceIntent;
    boolean mFirstSetZoom = true;
    boolean mIsLocationData = false;

    ContentResolver mContentResolver;

    Context mContext = this;
    GoogleMapData mGoogleMapData = new GoogleMapData();
    LocationManager mLocationManager;

    LocationHandler mLocationHandler = new LocationHandler();
    ArrayList<LocationData> mLocationDataList = new ArrayList<>();

    GPSService mGPSService;
    ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSService.BinderService binderService = (GPSService.BinderService) service;
            mGPSService = binderService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    // Butterknife
    @BindView(R.id.start_btn)
    Button mStartBtn;
    @BindView(R.id.update_btn)
    Button mUpdateBtn;
    @OnClick(R.id.update_btn)
    void clickStartBtn(){
        selectQuery();
        sendBroadCastReceiver();
    }
    @OnClick(R.id.start_btn)
    void clickUpdateBtn(){
        mServiceIntent = new Intent(this, GPSService.class);
        startService(mServiceIntent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        //컨텐트 프로바이더 리졸버 생성
        mContentResolver = getContentResolver();

        //구글맵 사용하기 위해 초기화
        mGoogleMapData.arrayPoints = new ArrayList<>();
        mGoogleMapData.callback = this; // Callback 메소드를 사용하기 위함.
        FragmentManager fragmentManager = getFragmentManager();
        mGoogleMapData.mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //권한 획득
        GetPermission();
        /*TedPermission.with(this)
                .setPermissionListener(mPermissionListener)
                .setRationaleMessage("GPS 권한이 필요합니다.")
                .setDeniedMessage("거부 하셨습니다.\nSetting에서 설정해주세요.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();*/
    }


    // 퍼미션은 액티비티에서 얻어야 하므로 이관 불가.
    public void GetPermission() {
        //퍼미션을 얻는데 사용자의 확인을 받아야지만 권한이 획득 된다. 앱 실행중에 권한을 획득한다는 것.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //권한이 부여가 되었을 경우
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // 1은 그냥 넣은 것. 해당 권한 획득이 제대로 된다면 콜백 메소드에 1이 전달
            }
        } else {

        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMapData.googleMap = map;
        LatLng latLng = null;
        //이건 처음에 1번만 하고 하나씩 추가하는 식으로 하면 좋을거같다.
        if (mFirstSetZoom) {
            mGoogleMapData.googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    LinearLayout linearLayout = new LinearLayout(mContext);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(mContext);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(mContext);
                    snippet.setTextColor(Color.BLACK);
                    snippet.setText(marker.getSnippet());

                    linearLayout.addView(title);
                    linearLayout.addView(snippet);

                    return linearLayout;
                }
            });
        }
        // 마커와 폴리라인을 지우고
        mGoogleMapData.googleMap.clear();
        //새로 다 다시 그려라!!!
        for (int i = 0; i < mLocationDataList.size(); i++) {
            latLng = new LatLng(mLocationDataList.get(i).latitude, mLocationDataList.get(i).longitude);

            //포인트에 지점을 더해놓음.
            mGoogleMapData.arrayPoints.add(latLng);

            //구글맵에 마커 찍는다.
            mGoogleMapData.markerOptions = new MarkerOptions();
            mGoogleMapData.markerOptions = GoogleMapUtils.setMarketOptions(mGoogleMapData.markerOptions, latLng, (i + 1) + "번째 거점", "주소 : " +
                    mLocationDataList.get(i).address, "진입 시간 : " +
                    mLocationDataList.get(i).date, "체류 시간 : " +
                    mLocationDataList.get(i).stayTime);

            mGoogleMapData.polylineOptions = new PolylineOptions(); // 마커와 선을 연결해준다.
            mGoogleMapData.polylineOptions = GoogleMapUtils.setPolylineOptions(mGoogleMapData.polylineOptions, mGoogleMapData.arrayPoints);
            if(i == 0){
                // 시작 부분
                mGoogleMapData.markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                mGoogleMapData.markerOptions.zIndex(1.0f);

            }
            if(i == mLocationDataList.size() -1){
                // 끝 부분
                mGoogleMapData.markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mGoogleMapData.markerOptions.zIndex(1.0f);
            }
            mGoogleMapData.googleMap = GoogleMapUtils.setGoogleMap(mGoogleMapData.googleMap, mGoogleMapData.markerOptions, mGoogleMapData.polylineOptions);
            if(i == mLocationDataList.size() -1){
                GoogleMapUtils.moveCamera(mGoogleMapData.googleMap, latLng);
                mIsLocationData = true;
            }
        }
        // 처음 한번만 줌을 땡겨준다.
        if (mFirstSetZoom && mIsLocationData) {
            mGoogleMapData.googleMap = GoogleMapUtils.setFirstZoom(mGoogleMapData.googleMap);
            mFirstSetZoom = false;
        }
        if(!mIsLocationData){
            Toast.makeText(this,"No GPS Data!", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectQuery() {
        mLocationDataList = mLocationHandler.updateMaps(mContext);
        mGoogleMapData.arrayPoints.clear();
        mGoogleMapData.mapFragment.getMapAsync(mGoogleMapData.callback);
    }

    // 구글맵 관련 멤버 클래스

    /*public void clickBtn(View v) {
        switch (v.getId()) {
            case R.id.update_btn:
                selectQuery();
                sendBroadCastReceiver();
                break;
            case R.id.start_btn:
                mServiceIntent = new Intent(this, GPSService.class);
                startService(mServiceIntent);
                break;
        }
    }*/
    public void sendBroadCastReceiver(){
        Intent intent = new Intent();
        intent.setAction("com.example.justin.gmaps.UPDATE");
        sendBroadcast(intent);
    }

    PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(mContext,"권한 거부!",Toast.LENGTH_SHORT).show();
        }
    };
}
