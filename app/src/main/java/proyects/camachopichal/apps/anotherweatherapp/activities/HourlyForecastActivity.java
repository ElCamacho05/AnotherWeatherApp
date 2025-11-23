package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.OpenWeatherAPIManager;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityHourlyForecastBinding;
import proyects.camachopichal.apps.anotherweatherapp.databinding.ItemHourlyForecastBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Actividad para mostrar la interfaz de pronostico de clima por hora
 * */
public class HourlyForecastActivity extends AppCompatActivity {

    private ActivityHourlyForecastBinding binding;
    private HourlyForecastAdapter adapter;
    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";

    // Claves para Intent
    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";
    public static final String EXTRA_DAY_TITLE = "extra_day_title";

    private double currentLat;
    private double currentLon;
    private String dayTitle; // El dia en el que se va a trabajar la actividad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHourlyForecastBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Importar a esta actividad los datos del Intent de la actividad padre
        currentLat = getIntent().getDoubleExtra(EXTRA_LAT, 0.0);
        currentLon = getIntent().getDoubleExtra(EXTRA_LON, 0.0);
        dayTitle = getIntent().getStringExtra(EXTRA_DAY_TITLE);

        binding.tvDayTitle.setText(dayTitle);
        getSupportActionBar().setTitle("Clima por Hora");

        // Configurar RecyclerView
        adapter = new HourlyForecastAdapter(this, new ArrayList<>());
        binding.rvHourlyForecast.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHourlyForecast.setAdapter(adapter);

        // Cargar datos
        fetchHourlyData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Muestra en la interfaz los datos de pronostico de clima por hora
     * */
    private void fetchHourlyData() {
        OpenWeatherAPIManager.getHourlyForecastReal(currentLat, currentLon, new OpenWeatherAPIManager.APIResponseCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    List<WeatherObject> allForecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);

                    // Logica de filtrado ajustada dependiendo del dia en el que se haya seleccionado la actividad
                    List<WeatherObject> filteredList = filterForecastByDay(allForecast, dayTitle);

                    if (!filteredList.isEmpty()) {
                        adapter.updateList(filteredList);
                    } else {
                        Toast.makeText(HourlyForecastActivity.this, "No hay datos para mostrar..", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(HourlyForecastActivity.this, "Error de parseo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(HourlyForecastActivity.this, "Error de red: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Filtra la lista de pronosticos para mostrar SOLO las horas correspondientes
     * al día seleccionado (sea hoy o un dia futuro seleccionado)
     */
    private List<WeatherObject> filterForecastByDay(List<WeatherObject> allForecast, String targetDayTitle) {
        List<WeatherObject> filtered = new ArrayList<>();

        // Determinar el dia que se esta buscando
        String targetDayAbbr;

        if (targetDayTitle.equals("HOY")) { // Estamos hablando de hoy
            // Si es HOY, calculamos qué día es "hoy" en el dispositivo (ej: "SÁB")
            SimpleDateFormat sdf = new SimpleDateFormat("EEE", new Locale("es", "ES"));
            sdf.setTimeZone(TimeZone.getDefault()); // Importante: Zona horaria del usuario
            targetDayAbbr = sdf.format(new Date()).toUpperCase(Locale.ROOT).replace(".", "");
            if (targetDayAbbr.length() > 3) targetDayAbbr = targetDayAbbr.substring(0, 3);
        } else { // No es hoy, es un dia futuro
            // Si viene del click de cualquier otro dia excepto HOY (como "DOM, 24°/21°"),
            // se extrae la etiqueta "DOM"
            targetDayAbbr = targetDayTitle.split(",")[0].trim().toUpperCase(Locale.ROOT);
        }

        // Recorremos todos los 96 timestamps obtenidos por la API
        for (WeatherObject item : allForecast) {
            // Obtenemos el dia de la semana de este item específico
            String itemDay = item.getDayOfWeek();

            // Si el elemennto coincide con lo que se busca para un determinado dia, se agrega en el array
            if (itemDay.equals(targetDayAbbr)) {
                filtered.add(item);
            }
        }

        return filtered;
    }


    private class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
        private final Context context;
        private List<WeatherObject> forecastList;

        public HourlyForecastAdapter(Context context, List<WeatherObject> forecastList) {
            this.context = context;
            this.forecastList = forecastList;
        }

        public void updateList(List<WeatherObject> newList) {
            this.forecastList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHourlyForecastBinding itemBinding = ItemHourlyForecastBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherObject item = forecastList.get(position);

            holder.binding.tvHour.setText(item.getHourMinuteString());
            holder.binding.tvTemp.setText(item.getTempCelsius() + "°C");

            String description = item.getDescription();
            if (description != null && !description.isEmpty()) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
            }
            holder.binding.tvDescription.setText(description);

            String details = String.format(Locale.getDefault(), "Viento: %.1f m/s | Prob: %s | Lluvia: %s",
                    item.getWindSpeed(), item.getPopPercentage(), item.getRainVolumeString());
            holder.binding.tvDetails.setText(details);

            loadIcon(item.getIconCode(), holder.binding.ivHourIcon);
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        private void loadIcon(String iconCode, ImageView imageView) {
            String iconUrl = ICON_BASE_URL + iconCode + "@2x.png";
            Glide.with(context).load(iconUrl).into(imageView);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final ItemHourlyForecastBinding binding;

            public ViewHolder(ItemHourlyForecastBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}