package com.messageurgence;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Pierre Boucher on 09/01/2018.
 */
public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    Context mContext;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private InputStream inputStream;
    byte buffer[];
    boolean stopThread;
    boolean deviceConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BdAdapter clientBdd = new BdAdapter(this);
        clientBdd.open();
        Cursor c = clientBdd.getNumero();
        if (Objects.equals(String.valueOf(c.getCount()), "0")) {
            clientBdd.insertNumero("0123456789");
        }
        c = clientBdd.getNumero();
        c.moveToFirst();
        clientBdd.close();
        TextView mTextView = (TextView) findViewById(R.id.textView2);

        mTextView.setText(c.getString(c.getColumnIndex("numero")));
        mContext = this;

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListenerGPS);
        isLocationEnabled();

        Button actBluetooth = (Button) findViewById(R.id.button9);
        Button changeNum = (Button) findViewById(R.id.button);
        Button quitter = (Button) findViewById(R.id.button2);

        actBluetooth.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StartBluetooth();
                    }});
        changeNum.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        Intent intent = new Intent(MainActivity.this, changeNumero.class);
                        startActivity(intent);
                    }});
        quitter.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                    }});

    }

    /**1
     * Initialisation du bluetooth
     *
     * @return Retourne une boolean égale a true si la l'appairage a été fait sans erreurs, false sinon
     */
    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Le téléphone ne supporte pas le bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Il faut d'abord appairer la télécommande au téléphone", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getName().equals("MESSAGEURGENCE")) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Connection entre le téléphone et la télécommande
     *
     * @return  Retourne une boolean égale a true si la connection a réussi, false sinon
     */
    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "impossible de se connecter", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                Toast.makeText(getApplicationContext(), "connecté", Toast.LENGTH_SHORT).show();
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return connected;
    }

    /**
     * Fonction tournant en boucle permettant de recuperer les données envoyé par la télécommande
     */
    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    envoieSms();
                                }
                            });

                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    /**
     * Fonction permettant de lancer les fonctions liées au bluetooth
     */
    public void StartBluetooth() {
        if (BTinit()) {
            if (BTconnect()) {
                deviceConnected = true;
                beginListenForData();
            }

        }
    }

    /**
     * Fonction permettant l'envoie d'un sms
     */
    public void envoieSms() {
        final TextView numero = (TextView) findViewById(R.id.textView2);
        final TextView message = (TextView) findViewById(R.id.textView8);

        String num = numero.getText().toString();
        String msg = "J'ai besoin d'aide, je suis approximativement à cette position GPS : " + message.getText().toString();

        SmsManager.getDefault().sendTextMessage(num, null, msg, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocationEnabled();
    }

    /**
     * Fonction permettant de savoir si le fournisseur actuel est activé ou désactivé
     *
     * @return  Retourne true si le fournisseur actuel est actif, false sinon
     */
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String msg = "Latitude: " + latitude + "\nLongitude: " + longitude;
            TextView affichagePos = (TextView) findViewById(R.id.textView8);
            affichagePos.setText(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
