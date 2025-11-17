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
import android.widget.LinearLayout; // Necesario para el contenedor de pronóstico
import android.widget.TextView;
import android.view.Gravity;
import android.util.TypedValue; // Para convertir DP a Pixeles

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        fetchLastLocation();
                    } else {
                        Toast.makeText(getContext(), "Permiso de ubicación denegado. No se puede obtener el clima actual.", Toast.LENGTH_LONG).show();
                        fetchWeatherForAllData(44.34, 10.99); // Coordenadas predeterminadas
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

        // Listeners de búsqueda y ubicación (mantenidos)
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ubicacion = binding.etSearchLocation.getText().toString();
                Toast.makeText(getContext(), "Buscando: " + ubicacion, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnGetLocation.setOnClickListener(v -> {
            requestLocationPermissions();
        });


        return view;
    }

    // --- MÉTODOS DE LOCALIZACIÓN ---

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

                fetchWeatherForAllData(lat, lon); // Llama al nuevo método principal

            } else {
                Toast.makeText(getContext(), "No se encontró la última ubicación. Intentando una nueva...", Toast.LENGTH_SHORT).show();
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void requestNewLocationData() {
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

                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    fetchWeatherForAllData(lat, lon); // Llama al nuevo método principal
                }
            }
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    // --- FIN MÉTODOS DE LOCALIZACIÓN ---


    /**
     * Lógica principal: Obtiene el clima actual (Hourly) y el pronóstico diario (Daily).
     */
    private void fetchWeatherForAllData(double lat, double lon) {

        binding.tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));

        // 2. Obtener Clima Actual (Usando la API por Hora)
        OpenWeatherAPIManager.getHourlyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> forecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);
                            if (!forecast.isEmpty()) {
                                updateCurrentWeatherUI(forecast.get(0)); // Actualiza la tarjeta principal
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error parseo HOURLY: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error HOURLY API: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

        // 3. Obtener Pronóstico Diario (Usando la API Diaria)
        OpenWeatherAPIManager.getDailyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> dailyForecast = OpenWeatherAPIManager.parseDailyForecast(jsonResponse);
                            displayDailyForecast(dailyForecast); // Dibuja los 7 días
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error parseo DAILY: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error DAILY API: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Actualiza la tarjeta de clima actual.
     */
    private void updateCurrentWeatherUI(WeatherObject current) {
        // CORREGIDO: Usando binding.ivWeatherIconActual
        loadWeatherIcon(current.getIconCode(), binding.ivWeatherIconActual);
        binding.tvTemp.setText(current.getTempCelsius() + "°C");
        binding.tvWeatherDescription.setText(current.getDescription());
        binding.tvFeelsLike.setText("Sensacion termica de: " + current.getFeelsLikeCelsius() + "°C");
        binding.tvTime.setText(current.getHourMinuteString());
    }

    /**
     * Dibuja dinámicamente los elementos de pronóstico en el HorizontalScrollView.
     */
    private void displayDailyForecast(List<WeatherObject> forecastList) {
        // El forecastContainer fue añadido en el XML y su ID es forecastContainer
        LinearLayout forecastContainer = binding.forecastContainer;

        // Limpiamos los placeholders estáticos y cualquier vista anterior
        forecastContainer.removeAllViews();

        if (forecastList.size() > 0) {
            // El primer elemento es HOY. Iteramos desde el índice 1 hasta un máximo de 7 días.
            for (int i = 1; i < Math.min(forecastList.size(), 8); i++) {
                WeatherObject day = forecastList.get(i);

                // 1. Configuración del contenedor vertical para un día
                LinearLayout dayLayout = new LinearLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                // Añadir un margen al final del elemento para separarlo del siguiente
                layoutParams.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                dayLayout.setLayoutParams(layoutParams);

                dayLayout.setOrientation(LinearLayout.VERTICAL);
                dayLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                dayLayout.setPadding(16, 8, 16, 8);

                // 2. TextView para el Día
                TextView tvDay = new TextView(getContext());
                tvDay.setText(day.getDayOfWeek());
                dayLayout.addView(tvDay);

                // 3. ImageView para el Icono
                ImageView ivIcon = new ImageView(getContext());
                // Tamaño fijo para los iconos pequeños
                ivIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())));
                dayLayout.addView(ivIcon);
                // El ImageView del pronóstico de 7 días usa el ID ivWeatherIconForecast.
                // Aquí, usamos el objeto local 'ivIcon' que acabamos de crear.
                loadWeatherIcon(day.getIconCode(), ivIcon);

                // 4. TextView para Temperaturas Min/Max
                TextView tvTemp = new TextView(getContext());
                String tempText = String.format(Locale.getDefault(), "%d°/%d°", day.getTempMaxCelsius(), day.getTempMinCelsius());
                tvTemp.setText(tempText);
                dayLayout.addView(tvTemp);

                // Agrega el layout del día al contenedor principal
                forecastContainer.addView(dayLayout);
            }
        }
    }


    /**
     * Construye la URL del icono y usa Glide para cargarlo de forma asíncrona.
     */
    private void loadWeatherIcon(String iconCode, ImageView imageView) {
        String iconUrl = ICON_BASE_URL + iconCode + "@2x.png";

        Glide.with(this)
                .load(iconUrl)
                .into(imageView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        binding = null;
    }
}