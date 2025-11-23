package proyects.camachopichal.apps.anotherweatherapp.database.Weather;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase de modelo para representar un punto de datos de clima (hora o día).
 * Todas las temperaturas se almacenan en Kelvin (como vienen del API).
 */
public class WeatherObject {

    // Campo del nivel superior (del objeto JSON)
    private long timestamp; // dt: UNIX timestamp en segundos
    private String dateTimeTxt; // dt_txt: se ignora para cálculos de hora local (para evitar cambios de hora por desface horario)
    private double pop; // pop: Probability of precipitation
    private int visibility; // Visibility in meters

    // Campos anidados (del objeto "main")
    private double tempKelvin;
    private double feelsLikeKelvin;
    private double tempMinKelvin;
    private double tempMaxKelvin;
    private int pressure; // hPa
    private int humidity; // %
    private int seaLevel; // hPa

    // Campos anidados (del primer objeto en array "weather")
    private int weatherId; // id: ej: 804
    private String weatherMain; // main: ej: "Clouds")
    private String description; // description: ej: "overcast clouds"
    private String iconCode; // icon: ej: "04n"

    // Campos anidados (del objeto "wind")
    private double windSpeed; // m/s
    private int windDeg; // degrees
    private double windGust; // m/s

    // Campos opcionales (del objeto "rain")
    private Double rain1h; // Rain volume for the last 1 hour (mm)


    // Getters y Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getDateTimeTxt() { return dateTimeTxt; }
    public void setDateTimeTxt(String dateTimeTxt) { this.dateTimeTxt = dateTimeTxt; }
    public double getPop() { return pop; }
    public void setPop(double pop) { this.pop = pop; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public double getTempKelvin() { return tempKelvin; }
    public void setTempKelvin(double tempKelvin) { this.tempKelvin = tempKelvin; }
    public double getFeelsLikeKelvin() { return feelsLikeKelvin; }
    public void setFeelsLikeKelvin(double feelsLikeKelvin) { this.feelsLikeKelvin = feelsLikeKelvin; }
    public double getTempMinKelvin() { return tempMinKelvin; }
    public void setTempMinKelvin(double tempMinKelvin) { this.tempMinKelvin = tempMinKelvin; }
    public double getTempMaxKelvin() { return tempMaxKelvin; }
    public void setTempMaxKelvin(double tempMaxKelvin) { this.tempMaxKelvin = tempMaxKelvin; }
    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public int getSeaLevel() { return seaLevel; }
    public void setSeaLevel(int seaLevel) { this.seaLevel = seaLevel; }
    public int getWeatherId() { return weatherId; }
    public void setWeatherId(int weatherId) { this.weatherId = weatherId; }
    public String getWeatherMain() { return weatherMain; }
    public void setWeatherMain(String weatherMain) { this.weatherMain = weatherMain; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public int getWindDeg() { return windDeg; }
    public void setWindDeg(int windDeg) { this.windDeg = windDeg; }
    public double getWindGust() { return windGust; }
    public void setWindGust(double windGust) { this.windGust = windGust; }
    public Double getRain1h() { return rain1h; }
    public void setRain1h(Double rain1h) { this.rain1h = rain1h; }



    // Metodos de conveniencia para uso en la interfaz

    /**
     * Convierte la temperatura principal de Kelvin a Celsius y redondea.
     */
    public int getTempCelsius() {
        return (int) Math.round(this.tempKelvin - 273.15);
    }

    public int getFeelsLikeCelsius() {
        return (int) Math.round(this.feelsLikeKelvin - 273.15);
    }

    public int getTempMinCelsius() {
        return (int) Math.round(this.tempMinKelvin - 273.15);
    }

    public int getTempMaxCelsius() {
        return (int) Math.round(this.tempMaxKelvin - 273.15);
    }

    /**
     * Obtiene la hora basada en el Timestamp y la zona horaria del dispositivo
     */
    public String getHourMinuteString() {
        try {
            Date date = new Date(this.timestamp * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "--:--";
        }
    }

    /**
     * Devuelve el día de la semana (LUN, MAR...) usando TimeZone local
     */
    public String getDayOfWeek() {
        try {
            // Convierte el timestamp a milisegundos
            Date date = new Date(this.timestamp * 1000L);
            // Formato de dia de la semana corto
            SimpleDateFormat sdf = new SimpleDateFormat("EEE", new Locale("es", "ES"));
            sdf.setTimeZone(TimeZone.getDefault()); // Uso de la hora local

            String day = sdf.format(date).toUpperCase(Locale.ROOT);

            // Asegura que no haya puntos en la string
            day = day.replace(".", "");
            if (day.length() > 3) {
                return day.substring(0, 3);
            }
            return day;
        } catch (Exception e) {
            return "---";
        }
    }

    public String getPopPercentage() {
        // Probability of Precipitation Percentage
        return (int) (pop * 100) + "%";
    }

    public String getRainVolumeString() {
        if (rain1h != null && rain1h > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(rain1h) + " mm";
        }
        return "0 mm";
    }
}