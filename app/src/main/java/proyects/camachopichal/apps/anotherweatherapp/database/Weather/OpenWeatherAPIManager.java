package proyects.camachopichal.apps.anotherweatherapp.database.Weather;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class OpenWeatherAPIManager {

    private static final String API_KEY = "481911831c3ea2c230e1803d9293f41e"; // Tu clave API
    private static final String BASE_HOURLY_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private static final String BASE_DAILY_URL = "https://pro.openweathermap.org/data/2.5/forecast/daily"; // Endpoint de pronóstico diario (antiguo)

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler para UI

    // Interfaz para manejar la respuesta de la red (callback)
    public interface APIResponseCallback {
        void onSuccess(String jsonResponse);
        void onFailure(String errorMessage);
    }

    /**
     * Realiza la llamada a la API para el pronóstico por hora (usado para el clima actual).
     */
    public static void getHourlyForecastReal(double lat, double lon, APIResponseCallback callback) {
        performApiCall(BASE_HOURLY_URL, lat, lon, callback);
    }

    /**
     * Realiza la llamada a la API para el pronóstico diario (7 días).
     * Nota: Este endpoint es obsoleto/no estándar, pero se usa para ilustrar el concepto.
     */
    public static void getDailyForecastReal(double lat, double lon, APIResponseCallback callback) {
        performApiCall(BASE_DAILY_URL, lat, lon, callback);
    }

    /**
     * Método genérico para ejecutar la llamada a la API en un hilo de fondo.
     */
    private static void performApiCall(String baseUrl, double lat, double lon, APIResponseCallback callback) {
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // Construir la URL completa
                // &units=metric convierte Kelvin a Celsius, lo cual facilita el parseo.
                String urlString = baseUrl + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric&cnt=7"; // cnt=7 para 7 días
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Devolver éxito (al hilo principal)
                    mainHandler.post(() -> callback.onSuccess(response.toString()));
                } else {
                    mainHandler.post(() -> callback.onFailure("Error HTTP: " + responseCode));
                }

            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure("Error de conexión: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    /**
     * PARSEADOR PARA RESPUESTA HORARIA (Clima Actual).
     * @param jsonResponse La cadena JSON de la API forecast/hourly.
     */
    public static List<WeatherObject> parseHourlyForecast(String jsonResponse) throws Exception {

        List<WeatherObject> hourlyForecast = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);
        if (!root.getString("cod").equals("200")) {
            throw new Exception("Error del API: " + root.getString("message"));
        }

        JSONArray listArray = root.getJSONArray("list");

        for (int i = 0; i < listArray.length(); i++) {
            JSONObject item = listArray.getJSONObject(i);
            WeatherObject weatherObj = new WeatherObject();

            // *** Asumiendo que el API devuelve Celsius por &units=metric, convertimos a Kelvin para almacenamiento ***
            JSONObject main = item.getJSONObject("main");
            weatherObj.setTempKelvin(main.getDouble("temp") + 273.15);
            weatherObj.setFeelsLikeKelvin(main.getDouble("feels_like") + 273.15);
            weatherObj.setTempMinKelvin(main.getDouble("temp_min") + 273.15);
            weatherObj.setTempMaxKelvin(main.getDouble("temp_max") + 273.15);

            // ... (Resto del parseo de campos 'main', 'weather', 'wind', 'rain' igual que antes) ...
            weatherObj.setTimestamp(item.getLong("dt"));
            weatherObj.setDateTimeTxt(item.getString("dt_txt"));
            if (item.has("pop")) { weatherObj.setPop(item.getDouble("pop")); }
            if (item.has("visibility")) { weatherObj.setVisibility(item.getInt("visibility")); }
            weatherObj.setPressure(main.getInt("pressure"));
            weatherObj.setHumidity(main.getInt("humidity"));
            if (main.has("sea_level")) { weatherObj.setSeaLevel(main.getInt("sea_level")); }

            JSONArray weatherArray = item.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weather = weatherArray.getJSONObject(0);
                weatherObj.setWeatherId(weather.getInt("id"));
                weatherObj.setWeatherMain(weather.getString("main"));
                weatherObj.setDescription(weather.getString("description"));
                weatherObj.setIconCode(weather.getString("icon"));
            }

            JSONObject wind = item.getJSONObject("wind");
            weatherObj.setWindSpeed(wind.getDouble("speed"));
            weatherObj.setWindDeg(wind.getInt("deg"));
            if (wind.has("gust")) { weatherObj.setWindGust(wind.getDouble("gust")); }

            if (item.has("rain")) {
                JSONObject rain = item.getJSONObject("rain");
                if (rain.has("1h")) { weatherObj.setRain1h(rain.getDouble("1h")); }
            }

            hourlyForecast.add(weatherObj);
        }

        return hourlyForecast;
    }


    /**
     * PARSEADOR PARA RESPUESTA DIARIA (Pronóstico de 7 Días).
     * @param jsonResponse La cadena JSON de la API forecast/daily.
     */
    public static List<WeatherObject> parseDailyForecast(String jsonResponse) throws Exception {

        List<WeatherObject> dailyForecast = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);

        if (!root.getString("cod").equals("200")) {
            throw new Exception("Error del API: " + root.getString("message"));
        }

        // El array principal en la respuesta diaria (daily) se llama "list"
        JSONArray listArray = root.getJSONArray("list");

        for (int i = 0; i < listArray.length(); i++) {
            JSONObject item = listArray.getJSONObject(i);
            WeatherObject weatherObj = new WeatherObject();

            // --- 1. Campos del nivel superior ---
            weatherObj.setTimestamp(item.getLong("dt"));
            if (item.has("pop")) { weatherObj.setPop(item.getDouble("pop")); }

            // --- 2. Campos de 'temp' (Temperaturas Min/Max) ---
            JSONObject temp = item.getJSONObject("temp");

            // Asumiendo Celsius (+ 273.15 para almacenamiento en Kelvin)
            weatherObj.setTempMinKelvin(temp.getDouble("min") + 273.15);
            weatherObj.setTempMaxKelvin(temp.getDouble("max") + 273.15);
            // Usamos la temperatura del día como la principal para el icono
            weatherObj.setTempKelvin(temp.getDouble("day") + 273.15);

            // --- 3. Campos de 'weather' (Descripción e Icono) ---
            JSONArray weatherArray = item.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weather = weatherArray.getJSONObject(0);
                weatherObj.setDescription(weather.getString("description"));
                weatherObj.setIconCode(weather.getString("icon"));
            }

            dailyForecast.add(weatherObj);
        }

        return dailyForecast;
    }
}