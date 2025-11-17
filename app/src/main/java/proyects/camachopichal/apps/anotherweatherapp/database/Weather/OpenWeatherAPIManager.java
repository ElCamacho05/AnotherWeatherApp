package proyects.camachopichal.apps.anotherweatherapp.database.Weather;


import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherAPIManager {

    private static final String API_KEY = "481911831c3ea2c230e1803d9293f41e"; // Tu clave API
    private static final String BASE_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";

    // ... (Interfaz WeatherForecastCallback omitida por brevedad) ...

    /**
     * Método central para parsear la respuesta JSON completa del API.
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
            weatherObj.setPop(item.getDouble("pop"));

            // La visibilidad puede faltar, pero si está, es un int
            if (item.has("visibility")) {
                weatherObj.setVisibility(item.getInt("visibility"));
            }

            // --- 2. Campos de 'main' (Temperaturas, Presión, Humedad) ---
            JSONObject main = item.getJSONObject("main");
            weatherObj.setTempKelvin(main.getDouble("temp"));
            weatherObj.setFeelsLikeKelvin(main.getDouble("feels_like"));
            weatherObj.setTempMinKelvin(main.getDouble("temp_min"));
            weatherObj.setTempMaxKelvin(main.getDouble("temp_max"));
            weatherObj.setPressure(main.getInt("pressure"));
            weatherObj.setHumidity(main.getInt("humidity"));

            // El API de pronóstico a veces incluye sea_level
            if (main.has("sea_level")) {
                weatherObj.setSeaLevel(main.getInt("sea_level"));
            }

            // --- 3. Campos de 'weather' (Descripción e Icono) ---
            // 'weather' es un array, tomamos el primer elemento (índice 0)
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
                // El campo para la última hora es "1h"
                if (rain.has("1h")) {
                    weatherObj.setRain1h(rain.getDouble("1h"));
                }
            }
            // Si 'rain' no existe, el campo rain1h permanecerá nulo, manejado por el método getRainVolumeString().

            hourlyForecast.add(weatherObj);
        }

        return hourlyForecast;
    }

    // Nota: Aquí iría la implementación real de la solicitud de red (OkHttp/Retrofit)
    // que llamaría a parseHourlyForecast(String) dentro del hilo de respuesta.
}