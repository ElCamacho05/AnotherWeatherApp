/**
 * POR FAVOR, NO CAMBIAR LA DOCUMENTACION/COMENTARIOS DE ESTE ARCHIVO, A MENOS QUE SEA PARA AGREGAR COSAS.
 * */

package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;


import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentHomeBinding;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.OpenWeatherAPIManager;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;
import proyects.camachopichal.apps.anotherweatherapp.activities.HourlyForecastActivity;
import proyects.camachopichal.apps.anotherweatherapp.activities.MapSelectionActivity; // Importar la nueva actividad

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase-Fragmento de pestaña principal "Home"
 * Modificado para abrir MapSelectionActivity al buscar ubicación manual.
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

    // ---- FIREBASE ----
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Prueba con coordenadas preestablecidas (CDMX)
    private double lastKnownLat = 19.3909832;
    private double lastKnownLon = -99.3084198;
    // FIN VARIABLES DE LOCALIZACION ----

    // Launcher para recibir el resultado del mapa
    private final ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("selected_lat", 0.0);
                    double lon = result.getData().getDoubleExtra("selected_lon", 0.0);

                    Toast.makeText(getContext(), "Ubicación seleccionada del mapa", Toast.LENGTH_SHORT).show();

                    // Actualizar todo con las nuevas coordenadas
                    lastKnownLat = lat;
                    lastKnownLon = lon;
                    saveLocation(lat, lon);
                    fetchWeatherForAllData(lat, lon);

                    // Opcional: Buscar el nombre de la ciudad para guardar en historial
                    buscarNombreCiudadYGuardar(lat, lon);
                }
            });


    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga datos de la ubicacion si existen
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

        // Se piden permisos de ubicacion al usuario (GPS automático al inicio)
        requestLocationPermissions();

        // Boton de busqueda por texto (mantener lógica original o eliminar si solo quieres mapa)
        binding.btnSearch.setOnClickListener(v -> {
            String ubicacion = binding.etSearchLocation.getText().toString();
            if (!ubicacion.isEmpty()) {
                buscarCoordenadasYGuardar(ubicacion);
            } else {
                // Si está vacío, abrir el mapa como alternativa
                abrirMapaSeleccion();
            }
        });

        // Boton de "Obtener mi ubicación" (Lo cambiamos para que abra el MAPA interactivo)
        binding.btnGetLocation.setOnClickListener(v -> {
            // Opción A: Abrir Mapa Interactivo
            abrirMapaSeleccion();

            // Opción B (Si quieres conservar el GPS automático en este botón, descomenta esto y comenta la línea de arriba):
            // Toast.makeText(getContext(), "Actualizando ubicación GPS...", Toast.LENGTH_SHORT).show();
            // requestLocationPermissions();
        });

        // Redirige a la actividad de HoutlyForecastActivity
        binding.cardCurrentWeather.setOnClickListener(v -> {
            launchHourlyForecast(lastKnownLat, lastKnownLon, "HOY");
        });

        return view;
    }

    private void abrirMapaSeleccion() {
        Intent intent = new Intent(getContext(), MapSelectionActivity.class);
        intent.putExtra("lat", lastKnownLat);
        intent.putExtra("lon", lastKnownLon);
        mapActivityLauncher.launch(intent);
    }

    //---- METODO PARA BUSCAR POR TEXTO ----
    private void buscarCoordenadasYGuardar(String ciudadNombre){
        Geocoder geocoder = new Geocoder(requireContext(),Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocationName(ciudadNombre,1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lon = address.getLongitude();

                lastKnownLat = lat;
                lastKnownLon = lon;
                saveLocation(lat, lon);
                fetchWeatherForAllData(lat, lon);

                // Guardar historial
                guardarEnHistorial(ciudadNombre, lat, lon, ciudadNombre);
            } else {
                Toast.makeText(getContext(), "Ciudad no encontrada", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error de red al buscar dirección", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para obtener nombre de ciudad desde coordenadas (Reverse Geocoding) para el historial
    private void buscarNombreCiudadYGuardar(double lat, double lon) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String ciudad = addresses.get(0).getLocality();
                if (ciudad == null) ciudad = "Ubicación en Mapa";
                guardarEnHistorial(ciudad, lat, lon, "Selección en Mapa");
            }
        } catch (IOException e) {
            // Ignorar error silenciosamente
        }
    }

    private void guardarEnHistorial(String direccionBonita, double lat, double lon, String terminoBusqueda) {
        if (mAuth == null) mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> busqueda = new HashMap<>();
        busqueda.put("termino_busqueda", terminoBusqueda);
        busqueda.put("direccion_completa", direccionBonita);
        busqueda.put("latitud", lat);
        busqueda.put("longitud", lon);
        busqueda.put("fecha_busqueda", new Date());

        if (db == null) db = FirebaseFirestore.getInstance();

        db.collection("usuario").document(uid).collection("Historial_Busquedas")
                .add(busqueda);
    }

    // ... [RESTO DE MÉTODOS SIN CAMBIOS: saveLocation, loadSavedLocation, requestLocationPermissions, fetchLastLocation, requestNewLocationData, fetchWeatherForAllData, updateCurrentWeatherUI, displayDailyForecast, launchHourlyForecast, loadWeatherIcon, onDestroyView] ...

    // (Incluir aquí el resto de métodos tal cual estaban en tu archivo original para no romper nada)
    // Para simplificar la respuesta, asumo que mantienes los métodos de abajo igual.
    // Solo asegúrate de copiar los métodos de persistencia y localización del archivo anterior.

    private void saveLocation(double lat, double lon) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAT, String.valueOf(lat));
        editor.putString(KEY_LON, String.valueOf(lon));
        editor.apply();
    }

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

    private void fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLat = location.getLatitude();
                lastKnownLon = location.getLongitude();
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

    private void fetchWeatherForAllData(double lat, double lon) {
        binding.tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));
        maxHourlyTimestamp = 0;

        OpenWeatherAPIManager.getHourlyForecastReal(lat, lon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            List<WeatherObject> forecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);
                            if (!forecast.isEmpty()) {
                                updateCurrentWeatherUI(forecast.get(0));
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

    private void updateCurrentWeatherUI(WeatherObject current) {
        loadWeatherIcon(current.getIconCode(), binding.ivWeatherIconActual);
        binding.tvTemp.setText(current.getTempCelsius() + "°C");
        String desc = current.getDescription();
        if(desc != null && !desc.isEmpty()){
            desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
        }
        binding.tvWeatherDescription.setText(desc);
        binding.tvFeelsLike.setText("Sensación: " + current.getFeelsLikeCelsius() + "°C | Lluvia: " + current.getRainVolumeString());
        binding.tvTime.setText(current.getHourMinuteString());
    }

    private void displayDailyForecast(List<WeatherObject> forecastList) {
        LinearLayout forecastContainer = binding.forecastContainer;
        forecastContainer.removeAllViews();

        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        int currentDayOfYear = currentCal.get(Calendar.DAY_OF_YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE", new Locale("es", "ES"));
        sdf.setTimeZone(TimeZone.getDefault());
        String currentDayName = sdf.format(new Date()).toUpperCase(Locale.ROOT).replace(".", "");
        if (currentDayName.length() > 3) currentDayName = currentDayName.substring(0, 3);

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

                if (dataYear < currentYear) continue;
                if (dataYear == currentYear && dataDayOfYear <= currentDayOfYear) continue;
                if (maxHourlyTimestamp > 0) {
                    if (dataYear > limitYear) continue;
                    if (dataYear == limitYear && dataDayOfYear > limitDayOfYear) continue;
                }
                if (day.getDayOfWeek().equals(currentDayName)) {
                    continue;
                }

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