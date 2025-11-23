/**
 * POR FAVOR, NO CAMBIAR LA DOCUMENTACION/COMENTARIOS DE ESTE ARCHIVO, A MENOS QUE SEA PARA AGREGAR COSAS.
 * */

package proyects.camachopichal.apps.anotherweatherapp.database.Weather;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

/**
 * Gestor de la API de OpenWeatherMap
 * */
public class OpenWeatherAPIManager {

    private static final String API_KEY = "481911831c3ea2c230e1803d9293f41e";
    private static final String BASE_HOURLY_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private static final String BASE_DAILY_URL = "https://pro.openweathermap.org/data/2.5/forecast/daily";

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Interfaz para manejar la respuesta de la red (callback)
    public interface APIResponseCallback {
        void onSuccess(String jsonResponse);
        void onFailure(String errorMessage);
    }

    /**
     * Realiza la llamada a la API para el pronostico por hora (96 entradas = 4 días).
     */
    public static void getHourlyForecastReal(double lat, double lon, APIResponseCallback callback) {
        performApiCall(BASE_HOURLY_URL, lat, lon, callback, 96); // 96 entradas = 4 días
    }

    /**
     * Realiza la llamada a la API para el pronostico por dia (7 entradas = 7 días)
     */
    public static void getDailyForecastReal(double lat, double lon, APIResponseCallback callback) {
        performApiCall(BASE_DAILY_URL, lat, lon, callback, 8); // 8 entradas = 8 días (incluye hoy)
    }

    /**
     * Metodo genérico para ejecutar la llamada a la API en un hilo de fondo (uso de Executor)
     */
    private static void performApiCall(String baseUrl, double lat, double lon, APIResponseCallback callback, int count) {
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // Construir la URL completa: &units=metric (Celsius)
                String urlString = String.format(Locale.getDefault(), "%s?lat=%.4f&lon=%.4f&appid=%s&units=metric&cnt=%d",
                        baseUrl, lat, lon, API_KEY, count);
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
     * Parseador de respuesta horaria a lista de objetos WeatherObject
     */
    public static List<WeatherObject> parseHourlyForecast(String jsonResponse) throws Exception {

        // Lista de objetos WeatherObject para almacenar las preducciones por hora
        List<WeatherObject> hourlyForecast = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);
        if (!root.getString("cod").equals("200")) {
            throw new Exception("Error del API: " + root.getString("message"));
        }

        JSONArray listArray = root.getJSONArray("list");

        for (int i = 0; i < listArray.length(); i++) {
            JSONObject item = listArray.getJSONObject(i);
            WeatherObject weatherObj = new WeatherObject();

            // Campos del nivel superior
            weatherObj.setTimestamp(item.getLong("dt"));
            weatherObj.setDateTimeTxt(item.getString("dt_txt"));
            // Probability of Precipitation
            if (item.has("pop")) { weatherObj.setPop(item.getDouble("pop")); }
            if (item.has("visibility")) { weatherObj.setVisibility(item.getInt("visibility")); }

            // Campos de "main" (Temperatura, Presion, Humedad)
            JSONObject main = item.getJSONObject("main");
            // Valores de temperatura parseados de kelvin a celsius
            weatherObj.setTempKelvin(main.getDouble("temp") + 273.15);
            weatherObj.setFeelsLikeKelvin(main.getDouble("feels_like") + 273.15);
            weatherObj.setTempMinKelvin(main.getDouble("temp_min") + 273.15);
            weatherObj.setTempMaxKelvin(main.getDouble("temp_max") + 273.15);

            weatherObj.setPressure(main.getInt("pressure"));
            weatherObj.setHumidity(main.getInt("humidity"));
            if (main.has("sea_level")) { weatherObj.setSeaLevel(main.getInt("sea_level")); }

            // Campos de "weather" (descripcion de clima)
            JSONArray weatherArray = item.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weather = weatherArray.getJSONObject(0);
                weatherObj.setWeatherId(weather.getInt("id"));
                weatherObj.setWeatherMain(weather.getString("main"));
                weatherObj.setDescription(weather.getString("description"));
                weatherObj.setIconCode(weather.getString("icon"));
            }

            // Campos de "wind" y "rain" (meteorologia)
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
     */
    public static List<WeatherObject> parseDailyForecast(String jsonResponse) throws Exception {

        List<WeatherObject> dailyForecast = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);

        if (!root.getString("cod").equals("200")) {
            throw new Exception("Error del API: " + root.getString("message"));
        }

        JSONArray listArray = root.getJSONArray("list");

        for (int i = 0; i < listArray.length(); i++) {
            JSONObject item = listArray.getJSONObject(i);
            WeatherObject weatherObj = new WeatherObject();

            // Campos del nivel superior
            weatherObj.setTimestamp(item.getLong("dt"));
            // Probability of Precipitation
            if (item.has("pop")) { weatherObj.setPop(item.getDouble("pop")); }

            // Campos de "temp" (Temperaturas Min/Max del dia)
            JSONObject temp = item.getJSONObject("temp");

            // Valores de temperatura parseados de kelvin a celsius
            weatherObj.setTempMinKelvin(temp.getDouble("min") + 273.15);
            weatherObj.setTempMaxKelvin(temp.getDouble("max") + 273.15);
            // Se usa la temperatura del dia como la principal para el icono
            weatherObj.setTempKelvin(temp.getDouble("day") + 273.15);

            // Campos de "weather" (descripcion de clima)
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