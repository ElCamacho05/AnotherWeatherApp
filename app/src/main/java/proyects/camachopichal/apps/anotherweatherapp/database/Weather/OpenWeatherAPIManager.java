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

public class OpenWeatherAPIManager {

    private static final String API_KEY = "481911831c3ea2c230e1803d9293f41e"; // Tu clave API
    private static final String BASE_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    // Interfaz para manejar la respuesta de la red (callback)
    public interface APIResponseCallback {
        void onSuccess(String jsonResponse);
        void onFailure(String errorMessage);
    }

    // Metodo para realizar la llamada API real
    public static void getHourlyForecastReal(double lat, double lon, APIResponseCallback callback) {
        // Ejecutar la solicitud de red en un hilo en segundo plano
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // 1. Construir la URL completa
                String urlString = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);

                // 2. Abrir conexión y configurarla
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 3. Leer la respuesta
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // 4. Devolver éxito (al hilo principal)
                    callback.onSuccess(response.toString());
                } else {
                    // Manejar errores HTTP
                    callback.onFailure("Error HTTP: " + responseCode);
                }

            } catch (Exception e) {
                // Manejar errores de conexión o IO
                callback.onFailure("Error de conexión: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }


    /**
     * Método central para parsear la respuesta JSON completa del API. (Se mantiene la funcionalidad previa)
     * @param jsonResponse La cadena JSON completa del API.
     * @return Una lista de objetos WeatherObject.
     * @throws Exception si el formato JSON es inesperado o hay errores de parseo.
     */
    public static List<WeatherObject> parseHourlyForecast(String jsonResponse) throws Exception {

        List<WeatherObject> hourlyForecast = new ArrayList<>();

        JSONObject root = new JSONObject(jsonResponse);

        // Verifica el estado de la respuesta
        if (!root.getString("cod").equals("200")) {
            throw new Exception("Error del API: " + root.getString("message"));
        }

        JSONArray listArray = root.getJSONArray("list");

        for (int i = 0; i < listArray.length(); i++) {
            JSONObject item = listArray.getJSONObject(i);
            WeatherObject weatherObj = new WeatherObject();

            // --- 1. Campos del nivel superior ---
            weatherObj.setTimestamp(item.getLong("dt"));
            weatherObj.setDateTimeTxt(item.getString("dt_txt"));

            // pop puede no existir
            if (item.has("pop")) {
                weatherObj.setPop(item.getDouble("pop"));
            }

            // La visibilidad puede faltar, pero si está, es un int
            if (item.has("visibility")) {
                weatherObj.setVisibility(item.getInt("visibility"));
            }

            // --- 2. Campos de 'main' (Temperaturas, Presión, Humedad) ---
            JSONObject main = item.getJSONObject("main");

            // Los valores se leerán en Celsius si se usa &units=metric
            weatherObj.setTempKelvin(main.getDouble("temp") + 273.15); // Almacenar en Kelvin es consistente
            weatherObj.setFeelsLikeKelvin(main.getDouble("feels_like") + 273.15);
            weatherObj.setTempMinKelvin(main.getDouble("temp_min") + 273.15);
            weatherObj.setTempMaxKelvin(main.getDouble("temp_max") + 273.15);

            weatherObj.setPressure(main.getInt("pressure"));
            weatherObj.setHumidity(main.getInt("humidity"));

            // El API de pronóstico a veces incluye sea_level
            if (main.has("sea_level")) {
                weatherObj.setSeaLevel(main.getInt("sea_level"));
            }

            // --- 3. Campos de 'weather' (Descripción e Icono) ---
            JSONArray weatherArray = item.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weather = weatherArray.getJSONObject(0);
                weatherObj.setWeatherId(weather.getInt("id"));
                weatherObj.setWeatherMain(weather.getString("main"));
                weatherObj.setDescription(weather.getString("description"));
                weatherObj.setIconCode(weather.getString("icon"));
            }

            // --- 4. Campos de 'wind' ---
            JSONObject wind = item.getJSONObject("wind");
            weatherObj.setWindSpeed(wind.getDouble("speed"));
            weatherObj.setWindDeg(wind.getInt("deg"));

            // La ráfaga (gust) es opcional
            if (wind.has("gust")) {
                weatherObj.setWindGust(wind.getDouble("gust"));
            }

            // --- 5. Campos opcionales 'rain' ---
            if (item.has("rain")) {
                JSONObject rain = item.getJSONObject("rain");
                if (rain.has("1h")) {
                    weatherObj.setRain1h(rain.getDouble("1h"));
                }
            }

            hourlyForecast.add(weatherObj);
        }

        return hourlyForecast;
    }
}