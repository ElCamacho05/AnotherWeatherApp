package proyects.camachopichal.apps.anotherweatherapp.database.Weather;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Clase de modelo para representar un punto de datos horario del pronóstico.
 * Todas las temperaturas se almacenan en Kelvin (como vienen del API).
 */
public class WeatherObject {

    // --- Campo del nivel superior ---
    private long timestamp; // dt
    private String dateTimeTxt; // dt_txt (ej: "2025-11-17 06:00:00")
    private double pop; // Probability of precipitation
    private int visibility; // Visibility in meters

    // --- Campos anidados (del objeto "main") ---
    private double tempKelvin;
    private double feelsLikeKelvin;
    private double tempMinKelvin;
    private double tempMaxKelvin;
    private int pressure; // hPa
    private int humidity; // %
    private int seaLevel; // hPa

    // --- Campos anidados (del primer objeto en array "weather") ---
    private int weatherId; // id (ej: 804)
    private String weatherMain; // main (ej: "Clouds")
    private String description; // description (ej: "overcast clouds")
    private String iconCode; // icon (ej: "04n")

    // --- Campos anidados (del objeto "wind") ---
    private double windSpeed; // m/s
    private int windDeg; // degrees
    private double windGust; // m/s

    // --- Campos opcionales (del objeto "rain") ---
    // Usamos Double para poder representar la ausencia (null) si no hay lluvia.
    private Double rain1h; // Rain volume for the last 1 hour (mm)


    // Constructor público vacío y métodos Getters/Setters (Omitidos para mantener la brevedad)
    // ...

    // --- MÉTODOS DE CONVENIENCIA PARA LA UI ---

    /**
     * Convierte la temperatura principal de Kelvin a Celsius y redondea.
     * T(°C) = T(K) - 273.15
     */
    public int getTempCelsius() {
        return (int) Math.round(this.tempKelvin - 273.15);
    }

    /**
     * Convierte la sensación térmica de Kelvin a Celsius y redondea.
     */
    public int getFeelsLikeCelsius() {
        return (int) Math.round(this.feelsLikeKelvin - 273.15);
    }

    /**
     * Convierte la temperatura mínima de Kelvin a Celsius y redondea.
     */
    public int getTempMinCelsius() {
        return (int) Math.round(this.tempMinKelvin - 273.15);
    }

    /**
     * Convierte la temperatura máxima de Kelvin a Celsius y redondea.
     */
    public int getTempMaxCelsius() {
        return (int) Math.round(this.tempMaxKelvin - 273.15);
    }

    /**
     * Extrae solo la hora y minutos del string dt_txt.
     */
    public String getHourMinuteString() {
        if (dateTimeTxt != null && dateTimeTxt.length() >= 16) {
            return dateTimeTxt.substring(11, 16); // "HH:MM"
        }
        return "--:--";
    }

    /**
     * Devuelve la probabilidad de lluvia en formato de porcentaje legible.
     */
    public String getPopPercentage() {
        // pop viene como un valor de 0 a 1
        return (int) (pop * 100) + "%";
    }

    /**
     * Devuelve el volumen de lluvia para la última hora o un string predeterminado si no hay datos.
     */
    public String getRainVolumeString() {
        if (rain1h != null && rain1h > 0) {
            DecimalFormat df = new DecimalFormat("#.##"); // Formato para dos decimales
            return df.format(rain1h) + " mm";
        }
        return "0 mm";
    }

    // --- Getters y Setters para todos los campos (Necesarios para el parseo y acceso) ---

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDateTimeTxt() {
        return dateTimeTxt;
    }

    public void setDateTimeTxt(String dateTimeTxt) {
        this.dateTimeTxt = dateTimeTxt;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public double getTempKelvin() {
        return tempKelvin;
    }

    public void setTempKelvin(double tempKelvin) {
        this.tempKelvin = tempKelvin;
    }

    public double getFeelsLikeKelvin() {
        return feelsLikeKelvin;
    }

    public void setFeelsLikeKelvin(double feelsLikeKelvin) {
        this.feelsLikeKelvin = feelsLikeKelvin;
    }

    public double getTempMinKelvin() {
        return tempMinKelvin;
    }

    public void setTempMinKelvin(double tempMinKelvin) {
        this.tempMinKelvin = tempMinKelvin;
    }

    public double getTempMaxKelvin() {
        return tempMaxKelvin;
    }

    public void setTempMaxKelvin(double tempMaxKelvin) {
        this.tempMaxKelvin = tempMaxKelvin;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDeg() {
        return windDeg;
    }

    public void setWindDeg(int windDeg) {
        this.windDeg = windDeg;
    }

    public double getWindGust() {
        return windGust;
    }

    public void setWindGust(double windGust) {
        this.windGust = windGust;
    }

    public Double getRain1h() {
        return rain1h;
    }

    public void setRain1h(Double rain1h) {
        this.rain1h = rain1h;
    }
}