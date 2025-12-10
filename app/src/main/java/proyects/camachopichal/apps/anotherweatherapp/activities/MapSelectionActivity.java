package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import proyects.camachopichal.apps.anotherweatherapp.R;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Button btnConfirmar;

    // Coordenadas por defecto (Veracruz, por ejemplo) si no se recibe nada
    private double initialLat = 19.1738;
    private double initialLon = -96.1342;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion);

        // Recibir coordenadas iniciales si existen
        if (getIntent().hasExtra("lat") && getIntent().hasExtra("lon")) {
            initialLat = getIntent().getDoubleExtra("lat", 19.1738);
            initialLon = getIntent().getDoubleExtra("lon", -96.1342);
        }
        selectedLocation = new LatLng(initialLat, initialLon);

        // Inicializar el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selected_lat", selectedLocation.latitude);
                    resultIntent.putExtra("selected_lon", selectedLocation.longitude);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(MapSelectionActivity.this, "Selecciona una ubicación", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Mover la cámara a la ubicación inicial y poner marcador
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 12));
        mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Ubicación Seleccionada"));

        // Listener para clics en el mapa
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // Actualizar la ubicación seleccionada
                selectedLocation = latLng;

                // Limpiar marcadores anteriores y poner el nuevo
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Nueva Selección"));

                // Opcional: Animar cámara al nuevo punto
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }
}