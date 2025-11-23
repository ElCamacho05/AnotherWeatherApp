/**
 * POR FAVOR, NO CAMBIAR LA DOCUMENTACION/COMENTARIOS DE ESTE ARCHIVO, A MENOS QUE SEA PARA AGREGAR COSAS.
 * */

package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.util.TypedValue;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentHomeBinding;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.OpenWeatherAPIManager;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;
import proyects.camachopichal.apps.anotherweatherapp.activities.HourlyForecastActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase-Fragmento de pestaña principal "Home"
 * Corrección aplicada: Filtro estricto de nombres de días duplicados.
 * */
public class HomeFragment extends Fragment {

    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";
    private FragmentHomeBinding binding;

    // --- CONSTANTES PARA GUARDAR SESION ---
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_LAT = "last_latitude";
    private static final String KEY_LON = "last_longitude";

    // --- VARIABLES DE ESTADO ---
    private long maxHourlyTimestamp = 0;
    private List<WeatherObject> tempDailyList = null;

    // --- VARIABLES DE LOCALIZACION ---
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    // Prueba con coordenadas preestablecidas (CDMX)
        private double lastKnownLat = 19.3909832;
        private double lastKnownLon = -99.3084198;
    // FIN VARIABLES DE LOCALIZACION ----


    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga datos de la ubicacion si existen
        // si se tiene una ubicacion guardada en preferencias, las carga de inmediato
        loadSavedLocation();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if ((fineLocationGranted != null && fineLocationGranted) ||
                            (coarseLocationGranted != null && coarseLocationGranted)) {
                        fetchLastLocation();
                    } else {
                        Toast.makeText(getContext(), "Permiso denegado. Usando última ubicación conocida.", Toast.LENGTH_LONG).show();
                        fetchWeatherForAllData(lastKnownLat, lastKnownLon);
                    }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Carga los datos en la interfaz con los datos guardados de otras sesiones
        fetchWeatherForAllData(lastKnownLat, lastKnownLon);

        // Se piden permisos de ubicacion al usuario
        requestLocationPermissions();

        binding.btnSearch.setOnClickListener(v -> {
            String ubicacion = binding.etSearchLocation.getText().toString();
            Toast.makeText(getContext(), "Buscando: " + ubicacion, Toast.LENGTH_SHORT).show();
            // demas logica de busqueda (TODO)
        });

        // Boton de Obtener Ubicacion
        binding.btnGetLocation.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Actualizando ubicación GPS...", Toast.LENGTH_SHORT).show();
            requestLocationPermissions();
        });

        // Redirige a la actividad de HoutlyForecastActivity
        // para el dia de hoy (panel de datos actuales)
        binding.cardCurrentWeather.setOnClickListener(v -> {
            launchHourlyForecast(lastKnownLat, lastKnownLon, "HOY");
        });

        return view;
    }

    /**
     * Guarda la ubicacion en SharedPreferences para poder usarla
     * en otras sesiones de manera inmediata al cargar los datos en pantalla
     * durante la obtencion de datos actuales y la posterior llamada a la API
     **/
    private void saveLocation(double lat, double lon) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Se guarda como String para mantener precision decimal exacta del double
        // (si se guarda como double se puede llegar a cambiar al momento de parseos, etc)
        editor.putString(KEY_LAT, String.valueOf(lat));
        editor.putString(KEY_LON, String.valueOf(lon));
        editor.apply();
    }

    /**
     * Carga la ubicacion guardada en sesiones anteriores en SharedPreferences
     * */
    private void loadSavedLocation() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(KEY_LAT) && prefs.contains(KEY_LON)) {
            try {
                String latStr = prefs.getString(KEY_LAT, "19.1738");
                String lonStr = prefs.getString(KEY_LON, "-96.1342");
                lastKnownLat = Double.parseDouble(latStr);
                lastKnownLon = Double.parseDouble(lonStr);
            } catch (Exception e) { }
        }
    }

    /**
     * Solicita permisos de ubicacion al usuario
     **/
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLastLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Obtiene la ultima ubicacion obtenida de SharedPreferences
     * y actualiza este mismo con la nueva
     * */
    private void fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLat = location.getLatitude();
                lastKnownLon = location.getLongitude();

                // Guardar la nueva ubicacion obtenida
                saveLocation(lastKnownLat, lastKnownLon);

                fetchWeatherForAllData(lastKnownLat, lastKnownLon);
                Toast.makeText(getContext(), "Ubicación actualizada", Toast.LENGTH_SHORT).show();
            } else {
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al obtener ubicación.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Solicita una nueva ubicacion al usuario con el GPS y la guarda en SharedPreferences
     * */
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
                    lastKnownLat = location.getLatitude();
                    lastKnownLon = location.getLongitude();
                    saveLocation(lastKnownLat, lastKnownLon);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    fetchWeatherForAllData(lastKnownLat, lastKnownLon);
                    Toast.makeText(getContext(), "Ubicación actualizada por GPS", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    /**
     * Carga los datos comunes entre los de clima actuales
     * y pronostico diario en la seccion de clima diario
     * */
    private void fetchWeatherForAllData(double lat, double lon) {
        binding.tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));

        maxHourlyTimestamp = 0;

        // Para el clima actual:
        OpenWeatherAPIManager.getHourlyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> forecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);
                            if (!forecast.isEmpty()) {
                                updateCurrentWeatherUI(forecast.get(0));

                                // Guardar timestamp limite
                                WeatherObject lastItem = forecast.get(forecast.size() - 1);
                                maxHourlyTimestamp = lastItem.getTimestamp();

                                if (tempDailyList != null) {
                                    displayDailyForecast(tempDailyList);
                                }
                            }
                        } catch (Exception e) { }
                    });
                }
            }
            @Override
            public void onFailure(String errorMessage) { }
        });

        // Para el Pronostico diario
        OpenWeatherAPIManager.getDailyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> dailyForecast = OpenWeatherAPIManager.parseDailyForecast(jsonResponse);
                            tempDailyList = dailyForecast;
                            displayDailyForecast(dailyForecast);
                        } catch (Exception e) { }
                    });
                }
            }
            @Override
            public void onFailure(String errorMessage) { }
        });
    }

    /**
     * Carga los elementos particulares de clima actual (HOY, no diario) en la UI
     * */
    private void updateCurrentWeatherUI(WeatherObject current) {
        loadWeatherIcon(current.getIconCode(), binding.ivWeatherIconActual);
        binding.tvTemp.setText(current.getTempCelsius() + "°C");

        String desc = current.getDescription();
        if(desc != null && !desc.isEmpty()){
            desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
        }
        binding.tvWeatherDescription.setText(desc);

        binding.tvFeelsLike.setText("Sensación térmica: " + current.getFeelsLikeCelsius() + "°C"
                    + " | Probabilidad: " + current.getPopPercentage()
                    + " | Lluvia: " + current.getRainVolumeString()
                    + " | Viento: " + current.getWindSpeed() + " m/s"
                    + " | Humedad: " + current.getHumidity() + "%");
        binding.tvTime.setText(current.getHourMinuteString());
    }

    /**
     * Dibuja los elementos del pronostico diario en la UI
     * */
    private void displayDailyForecast(List<WeatherObject> forecastList) {
        LinearLayout forecastContainer = binding.forecastContainer;
        forecastContainer.removeAllViews();

        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        int currentDayOfYear = currentCal.get(Calendar.DAY_OF_YEAR);

        // Obtener el nombre del dia: ej: "DOM"
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", new Locale("es", "ES"));
        sdf.setTimeZone(TimeZone.getDefault());
        String currentDayName = sdf.format(new Date()).toUpperCase(Locale.ROOT).replace(".", "");
        if (currentDayName.length() > 3) currentDayName = currentDayName.substring(0, 3);
        // -------------------------------------------------------

        // Limites de fecha (si ya cargo la API Horaria)
        int limitYear = -1;
        int limitDayOfYear = -1;
        if (maxHourlyTimestamp > 0) {
            Calendar limitCal = Calendar.getInstance();
            limitCal.setTimeInMillis(maxHourlyTimestamp * 1000L);
            limitYear = limitCal.get(Calendar.YEAR);
            limitDayOfYear = limitCal.get(Calendar.DAY_OF_YEAR);
        }

        if (forecastList.size() > 0) {
            for (WeatherObject day : forecastList) {

                Calendar dayCal = Calendar.getInstance();
                dayCal.setTimeInMillis(day.getTimestamp() * 1000L);
                int dataYear = dayCal.get(Calendar.YEAR);
                int dataDayOfYear = dayCal.get(Calendar.DAY_OF_YEAR);

                // Fitros de fechas que no queremos que se muestren

                // Filtro 1: Fechas pasadas
                if (dataYear < currentYear) continue;
                if (dataYear == currentYear && dataDayOfYear <= currentDayOfYear) continue;


                // Filtro 2: por nombre
                // Si el nombre de este día es IGUAL al de hoy, lo saltamos
                // Distinto de la zona de arriba por que esa se trata por separado
                // Esto solo afecta al pronostico diario
                if (day.getDayOfWeek().equals(currentDayName)) {
                    continue;
                }
                // ------------------------------------

                LinearLayout dayLayout = new LinearLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));
                dayLayout.setLayoutParams(layoutParams);

                dayLayout.setOrientation(LinearLayout.VERTICAL);
                dayLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                dayLayout.setPadding(16, 8, 16, 8);

                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                dayLayout.setBackgroundResource(outValue.resourceId);

                TextView tvDay = new TextView(getContext());
                tvDay.setText(day.getDayOfWeek());
                tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
                dayLayout.addView(tvDay);

                ImageView ivIcon = new ImageView(getContext());
                ivIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())));
                dayLayout.addView(ivIcon);
                loadWeatherIcon(day.getIconCode(), ivIcon);

                TextView tvTemp = new TextView(getContext());
                String tempText = String.format(Locale.getDefault(), "%d°/%d°", day.getTempMaxCelsius(), day.getTempMinCelsius());
                tvTemp.setText(tempText);
                dayLayout.addView(tvTemp);

                String dayTitle = String.format(Locale.getDefault(), "%s, %d°/%d°",
                        day.getDayOfWeek(), day.getTempMaxCelsius(), day.getTempMinCelsius());

                dayLayout.setOnClickListener(v -> {
                    launchHourlyForecast(lastKnownLat, lastKnownLon, dayTitle);
                });

                forecastContainer.addView(dayLayout);
            }
        }
    }

    private void launchHourlyForecast(double lat, double lon, String dayTitle) {
        Intent intent = new Intent(getContext(), HourlyForecastActivity.class);
        intent.putExtra(HourlyForecastActivity.EXTRA_LAT, lat);
        intent.putExtra(HourlyForecastActivity.EXTRA_LON, lon);
        intent.putExtra(HourlyForecastActivity.EXTRA_DAY_TITLE, dayTitle);
        startActivity(intent);
    }

    private void loadWeatherIcon(String iconCode, ImageView imageView) {
        String iconUrl = ICON_BASE_URL + iconCode + "@2x.png";
        Glide.with(this).load(iconUrl).into(imageView);
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