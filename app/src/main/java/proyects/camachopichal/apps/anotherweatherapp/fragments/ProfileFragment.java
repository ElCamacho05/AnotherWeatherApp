package proyects.camachopichal.apps.anotherweatherapp.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Importa TextView

// Importa el R de tu paquete principal
import proyects.camachopichal.apps.anotherweatherapp.R;

public class ProfileFragment extends Fragment {

    // Declaramos los TextViews para mostrar la info
    TextView tvUserEmail;
    TextView tvLocationsCount;

    public ProfileFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "Infla" (conecta) el layout XML con este archivo Java
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Conectamos las vistas
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvLocationsCount = view.findViewById(R.id.tvLocationsCount);

        // Aquí pondrías la lógica para cargar los datos del usuario
        // (por ahora ponemos datos de ejemplo)
        tvUserEmail.setText("usuario@ejemplo.com");
        tvLocationsCount.setText("2");

        return view;
    }
}