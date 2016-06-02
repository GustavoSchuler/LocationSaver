package br.feevale.locationsaver;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import br.feevale.locationsaver.db.LocationContract;

import br.feevale.locationsaver.db.LocationDbHelper;

/**
    Para outros exemplos de geradores de mapas estáticos, acesse: http://staticmapmaker.com/

    Para Yandex: http://staticmapmaker.com/yandex/
    Para Google: http://staticmapmaker.com/google/
 */
public class LocationActivity extends Activity {

    private LocationManager locationManager;
    private TextView latitude, longitude, altitude, velocidade;
    private WebView mapa;
    private String urlBase = "http://maps.googleapis.com/maps/api" +
            "/staticmap?size=400x300&sensor=true&markers=color:red|label:Eu|%s,%s";
    private LocationDbHelper mHelper;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localizacao);

        mHelper = new LocationDbHelper(this);

        latitude = (TextView) findViewById( R.id.latitude );
        longitude = (TextView) findViewById( R.id.longitude );
        altitude = (TextView) findViewById( R.id.altitude );
        velocidade = (TextView) findViewById( R.id.velocidade );
        mapa = (WebView) findViewById(R.id.mapa);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Listener listener = new Listener();
        long tempoAtualizacao = 0;
        float distancia = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, tempoAtualizacao, distancia, listener );
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, tempoAtualizacao, distancia, listener);
    }

    private class Listener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            updateUI(location);
        }

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText( this, "Não é possível utilizar esta aplicação sem as permissões nescessárias", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void saveLocation(View v) {
        final EditText locationEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adicionar localização")
                .setMessage("Qual o nome da localização atual?")
                .setView(locationEditText)
                .setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = String.valueOf(locationEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(LocationContract.LocationEntry.COL_LOCATION_NAME, name);
                        values.put(LocationContract.LocationEntry.COL_LOCATION_LATITUDE, latitude.getText().toString());
                        values.put(LocationContract.LocationEntry.COL_LOCATION_LONGITUDE, longitude.getText().toString());
                        db.insertWithOnConflict(LocationContract.LocationEntry.TABLE,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        updateUI(lastLocation);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    private void updateUI(Location location) {

        String latitudeStr = String.valueOf(location.getLatitude());
        String longitudeStr = String.valueOf(location.getLongitude());
        String altitudeStr = String.valueOf(location.getAltitude());
        String velocidadeStr = String.valueOf(location.getSpeed());

        latitude.setText(latitudeStr);
        longitude.setText(longitudeStr);
        altitude.setText(altitudeStr);
        velocidade.setText(velocidadeStr);

        String url = String.format(urlBase, latitudeStr, longitudeStr);

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationContract.LocationEntry.TABLE,
                new String[]{LocationContract.LocationEntry._ID, LocationContract.LocationEntry.COL_LOCATION_NAME, LocationContract.LocationEntry.COL_LOCATION_LATITUDE, LocationContract.LocationEntry.COL_LOCATION_LONGITUDE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int lbl = cursor.getColumnIndex(LocationContract.LocationEntry.COL_LOCATION_NAME);
            int lat = cursor.getColumnIndex(LocationContract.LocationEntry.COL_LOCATION_LATITUDE);
            int lon = cursor.getColumnIndex(LocationContract.LocationEntry.COL_LOCATION_LONGITUDE);
            url = url + "&markers=color:red|label=" + cursor.getString(lbl) + "|" + cursor.getString(lat) + "," + cursor.getString(lon);
        }

        mapa.loadUrl(url);
        //Log.d("url", "onLocationChanged: " + url);

        cursor.close();
        db.close();
    }
}
