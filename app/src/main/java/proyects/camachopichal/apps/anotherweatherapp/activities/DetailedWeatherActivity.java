package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityDetailedWeatherBinding;

import java.util.Locale;

public class DetailedWeatherActivity extends AppCompatActivity {

    private ActivityDetailedWeatherBinding binding;
    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";

    // Constantes para las claves del Intent (deben coincidir con el emisor)
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_TEMP = "extra_temp";
    public static final String EXTRA_FEELS_LIKE = "extra_feels_like";
    public static final String EXTRA_DESC = "extra_desc";
    public static final String EXTRA_ICON = "extra_icon";
    public static final String EXTRA_WIND_SPEED = "extra_wind_speed";
    public static final String EXTRA_WIND_DEG = "extra_wind_deg";
    public static final String EXTRA_HUMIDITY = "extra_humidity";
    public static final String EXTRA_PRESSURE = "extra_pressure";
    public static final String EXTRA_POP = "extra_pop";
    public static final String EXTRA_RAIN_VOL = "extra_rain_vol";
    public static final String EXTRA_VISIBILITY = "extra_visibility";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailedWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Botón de regreso
        binding.btnBack.setOnClickListener(v -> finish());

        // Obtener datos
        if (getIntent() != null) {
            String time = getIntent().getStringExtra(EXTRA_TIME);
            int temp = getIntent().getIntExtra(EXTRA_TEMP, 0);
            int feelsLike = getIntent().getIntExtra(EXTRA_FEELS_LIKE, 0);
            String desc = getIntent().getStringExtra(EXTRA_DESC);
            String icon = getIntent().getStringExtra(EXTRA_ICON);
            double windSpeed = getIntent().getDoubleExtra(EXTRA_WIND_SPEED, 0);
            int windDeg = getIntent().getIntExtra(EXTRA_WIND_DEG, 0);
            int humidity = getIntent().getIntExtra(EXTRA_HUMIDITY, 0);
            int pressure = getIntent().getIntExtra(EXTRA_PRESSURE, 0);
            String pop = getIntent().getStringExtra(EXTRA_POP);
            String rainVol = getIntent().getStringExtra(EXTRA_RAIN_VOL);
            int visibility = getIntent().getIntExtra(EXTRA_VISIBILITY, 0);

            // Asignar datos a la UI
            binding.tvDetailTime.setText(time);
            binding.tvDetailTemp.setText(temp + "°C");
            binding.tvDetailFeelsLike.setText(feelsLike + "°C");

            // Capitalizar descripción
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            binding.tvDetailDesc.setText(desc);

            binding.tvDetailWind.setText(windSpeed + " m/s");
            binding.tvDetailWindDir.setText("Dirección: " + windDeg + "°");

            binding.tvDetailHumidity.setText(humidity + "%");
            binding.tvDetailPressure.setText(pressure + " hPa");

            binding.tvDetailRain.setText(pop); // "30%"
            binding.tvDetailRainVol.setText(rainVol); // "2.5 mm"

            // Convertir metros a kilometros para visibilidad
            double visKm = visibility / 1000.0;
            binding.tvDetailVisibility.setText(String.format(Locale.getDefault(), "%.1f km", visKm));

            // Cargar Icono Gigante
            if (icon != null) {
                String iconUrl = ICON_BASE_URL + icon + "@4x.png"; // @4x para alta resolución
                Glide.with(this).load(iconUrl).into(binding.ivDetailIcon);
            }
        } else {
            Toast.makeText(this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
        }
    }
}