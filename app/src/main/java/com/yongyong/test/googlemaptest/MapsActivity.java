package com.yongyong.test.googlemaptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnPermissionCallback {

    private GoogleMap mMap;
    private GpsInfo gps;
    private double latitude = 0.0;
    private double longitude = 0.0;

    // 권한 체크용
    private PermissionHelper permissionHelper;
    private AlertDialog builder;

    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 권한요청
        permissionHelper = PermissionHelper.getInstance(this);
        permissionHelper
                .setForceAccepting(false)
                .request(MULTI_PERMISSIONS);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("지금 요기잉네"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void initMap(){
        getLocation();
    }

    /**
     * GPS상 내위치정보를 구함
     * @return
     */
    public void getLocation() {
        // 권한이 있는지 확인(라이브러리 쓰는게 아닌 직접 체크시 이렇게)
        // 훨씬 복잡한걸 알 수 있음ㅋㅋ
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this, "위치정보 사용 권한을 동의해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        gps = new GpsInfo(MapsActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.d("좌표" , "위도 => " + latitude);
            Log.d("좌표" , "경도 => " + longitude);

            Toast.makeText(
                    getApplicationContext(),
                    "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                    Toast.LENGTH_LONG).show();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }


    /**
     * Used to determine if the user accepted {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW} or no.
     * <p/>
     * if you never passed the permission this method won't be called.
     */
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionHelper.onActivityForResult(requestCode);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override public void onPermissionGranted(@NonNull String[] permissionName) {
        // TODO 권한승인 버튼 누름, 내위치정보 요청
        initMap();
    }

    @Override public void onPermissionDeclined(@NonNull String[] permissionName) {
        // TODO 권한승인 거부버튼 누름
        getAlertDialog(permissionName[0]).show();

    }

    @Override public void onPermissionPreGranted(@NonNull String permissionsName) {
        // TODO 이미 승인되어 있음, 내위치정보 요청
        initMap();


    }

    @Override public void onPermissionNeedExplanation(@NonNull String permissionName) {
        // TODO 권한동의 거부시 안내화면을 이곳에 구현
        getAlertDialog(permissionName).show();

    }

    @Override public void onPermissionReallyDeclined(@NonNull String permissionName) {
        // TODO 사용자가 설정화면으로 직접 이동해야만 하는 권한에 대한 콜백
    }

    @Override public void onNoPermissionNeeded() {
        // TODO 구지 동의하지 않아도 되는 권한
        initMap();
    }


    /**
     * 싱글 권한 요청에 대해 거부시 설명 안내 팝업
     * @param permission
     * @return
     */
    public AlertDialog getAlertDialog(final String permission) {
        if (builder == null) {
            builder = new AlertDialog.Builder(this)
                    .setTitle("권한동의 해야함요").create();
        }
        builder.setButton(DialogInterface.BUTTON_POSITIVE, "다시 확인하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionHelper.requestAfterExplanation(permission);
            }
        });
        builder.setMessage("권한을 동의해주셈 (" + permission + ")");
        return builder;
    }


}
