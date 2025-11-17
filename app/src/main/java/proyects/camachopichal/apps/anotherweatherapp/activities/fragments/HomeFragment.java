package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;
import android.location.Location;
import android.os.Looper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

// Importa las clases de View Binding y API
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentHomeBinding;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.OpenWeatherAPIManager;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // URL BASE para los iconos de OpenWeatherMap (tamaño 2x)
    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";
    private FragmentHomeBinding binding;

    // --- VARIABLES DE LOCALIZACIÓN ---
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    // --- FIN VARIABLES DE LOCALIZACIÓN ---


    public HomeFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Inicializar el launcher de permisos (preparado para manejar el resultado)
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        // Permisos concedidos, intentar obtener la ubicación
                        fetchLastLocation();
                    } else {
                        Toast.makeText(getContext(), "Permiso de ubicación denegado. No se puede obtener el clima actual.", Toast.LENGTH_LONG).show();
                        // Opcional: Cargar un clima predeterminado si el permiso es denegado
                    }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Intentamos obtener la ubicación al iniciar la pantalla
        requestLocationPermissions();

        // Listener de búsqueda (Mantiene la funcionalidad previa)
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ubicacion = binding.etSearchLocation.getText().toString();
                Toast.makeText(getContext(), "Buscando: " + ubicacion, Toast.LENGTH_SHORT).show();
            }
        });

        // NUEVO LISTENER para obtener ubicación forzada
        binding.btnGetLocation.setOnClickListener(v -> {
            requestLocationPermissions();
        });


        return view;
    }

    /**
     * Verifica y solicita permisos de ubicación (ACCESS_FINE_LOCATION).
     */
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLastLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Obtiene la última ubicación conocida del dispositivo.
     */
    private void fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(getContext(), "Obteniendo ubicación actual...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                fetchAndDisplayWeather(lat, lon);

            } else {
                Toast.makeText(getContext(), "No se encontró la última ubicación. Intentando una nueva...", Toast.LENGTH_SHORT).show();
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Solicita una actualización de ubicación si getLastLocation falló.
     */
    private void requestNewLocationData() {
        // Configuramos la petición de ubicación (Se usa una clase obsoleta, se recomienda LocationRequest.Builder)
        // Por la versión de Android 36, LocationRequest.create() es mejor reemplazado por Builder
        locationRequest = new LocationRequest.Builder(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    // Detenemos las actualizaciones y llamamos a la API
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    fetchAndDisplayWeather(lat, lon);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }


    /**
     * Llama al API real y actualiza la UI usando el callback en el hilo principal.
     */
    private void fetchAndDisplayWeather(double lat, double lon) {

        // 1. Actualiza el texto de ubicación para confirmar visualmente las coordenadas
        binding.tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));

        // 2. Llama al manager para obtener los datos de la red
        OpenWeatherAPIManager.getHourlyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                // ** DEBE EJECUTARSE EN EL HILO PRINCIPAL PARA ACTUALIZAR LA UI **
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> forecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);

                            if (!forecast.isEmpty()) {
                                WeatherObject current = forecast.get(0);

                                // 3. Actualizar la UI
                                loadWeatherIcon(current.getIconCode());
                                binding.tvTemp.setText(current.getTempCelsius() + "°C");
                                binding.tvWeatherDescription.setText(current.getDescription());
                                binding.tvFeelsLike.setText("Sensacion termica de: " + current.getFeelsLikeCelsius() + "°C");
                                binding.tvTime.setText(current.getHourMinuteString());

                            } else {
                                Toast.makeText(getContext(), "API: No se encontraron datos de clima.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error de parseo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de API: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Construye la URL del icono y usa Glide para cargarlo de forma asíncrona.
     */
    private void loadWeatherIcon(String iconCode) {
        String iconUrl = ICON_BASE_URL + iconCode + "@2x.png";

        Glide.with(this)
                .load(iconUrl)
                .into(binding.ivWeatherIconActual);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener actualizaciones de ubicación si están activas
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        binding = null;
    }
}