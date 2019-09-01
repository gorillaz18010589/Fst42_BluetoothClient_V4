package tw.brad.apps.brad421;
//1.開4個權限
//2.api包導入
//使用第三方api
//bluetooth imuker github: https://github.com/dingjikerbo/Android-BluetoothKit
//設備可以讀,寫,對方資訊可以發出來
//下載BLE Peirpheral
//先挖service再看serice有哪些功能
//Libgdx遊戲引勤

//godaddy(買網域) :https://tw.godaddy.com/offers/domains/godaddy-b?isc=gofktw06&countryview=1&currencyType=TWD&gclid=EAIaIQobChMImbLB5ZGv5AIVWaaWCh3nugRREAAYASAAEgJXL_D_BwE&gclsrc=aw.ds


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private BluetoothClient mClient;
    private String mac = "73:5D:5F:16:06:69";

    //藍芽狀態監聽
    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            if (openOrClosed){
                Log.v("brad", "ble open");
            }else{
                Log.v("brad", "ble close");
            }
        }

    };
    //藍芽狀態監聽者
    private final BleConnectStatusListener mBleConnectStatusListener =
            new BleConnectStatusListener() {
                @Override
                public void onConnectStatusChanged(String mac, int status) {
                    if (status == STATUS_CONNECTED) {
                        Log.v("brad", "connect OK");
                    } else if (status == STATUS_DISCONNECTED) {
                        Log.v("brad", "disconnect");
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,},
                    123);
        }else{
            init();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init(){
        mClient = new BluetoothClient(this);
        mClient.registerBluetoothStateListener(mBluetoothStateListener);
    }

    //開啟藍芽
    public void test1(View view) {
        if (!mClient.isBluetoothOpened()){ //如果藍芽沒有打開(isBluetoothOpened:藍芽是否打開)
            mClient.openBluetooth(); //打開藍芽
        }
    }
    //關閉藍芽
    public void test2(View view) {
        if (mClient.isBluetoothOpened()){//如果藍芽開啟中
            mClient.closeBluetooth();//藍芽關閉
        }
    }
    //搜尋藍芽
    public void test3(View view) {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.v("brad", "Start scan...");
            }

            //當藍芽被找到
            @Override
            public void onDeviceFounded(SearchResult device) {
                String mac = device.getAddress(); //取得藍芽位置
                String name = device.getName();//取得藍芽名稱
                int rssi = device.rssi;
                Log.v("brad", name + ":" + mac + ":" + rssi);

                if(name.equals("Brad Redmi")){ //如果取得的藍芽名稱包含Brad Redmi
                    MainActivity.this.mac = mac;//這個取得的mac => 存在外部mac
                }
            }

            //當搜尋停止
            @Override
            public void onSearchStopped() {
                Log.v("brad", "Stop scan...");
            }
            //當搜尋取消
            @Override
            public void onSearchCanceled() {
                Log.v("brad", "cancel scan...");

            }
        });
    }
    //停止藍芽搜尋
    public void test4(View view){
        mClient.stopSearch();
    }


    @Override
    public void finish() {
        //藍芽註冊監聽狀態
        mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        super.finish();
    }

    //連接特定藍芽裝置
    public void test5(View view) {
        //73:5D:5F:16:06:69
        mClient.registerConnectStatusListener( //藍芽的連線監聽
                mac, mBleConnectStatusListener); //1.藍芽裝置位置 2.

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();

        mClient.connect(mac, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                Log.v("brad", "connect response");
            }
        });
    }
    //斷開
    public void test6(View view) {
        mClient.disconnect(mac);
    } //斷開聯結

    public void test7(View view) {
        // serviceUUID: 0000180f-0000-1000-8000-00805f9b34fb
        // characterUUID: 00002a19-0000-1000-8000-00805f9b34fb

        String sUUID = "0000180f-0000-1000-8000-00805f9b34fb";
        String cUUID = "00002a19-0000-1000-8000-00805f9b34fb";

        UUID serviceUUID = UUID.fromString(sUUID);
        UUID characterUUID = UUID.fromString(cUUID);

        mClient.notify(mac, //"73:5D:5F:16:06:69"; 對方藍芽位置
                serviceUUID,
                characterUUID,
                new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                for (int v : value){
                    Log.v("brad", "==> " + v);
                }
            }

            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) { //如果連線狀態 == 成功
                    Log.v("brad", "response success");
                }
            }
        });
    }
}