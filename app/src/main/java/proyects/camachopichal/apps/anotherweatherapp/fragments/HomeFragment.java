package proyects.camachopichal.apps.anotherweatherapp.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Importa Button si vas a usarlo
import android.widget.EditText; // Importa EditText si vas a usarlo

// Importa el R de tu paquete principal
import proyects.camachopichal.apps.anotherweatherapp.R;

public class HomeFragment extends Fragment {

    // Declara los elementos de tu UI aquí si necesitas usarlos
    EditText etSearchLocation;
    Button btnSearch;

    public HomeFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "Infla" (conecta) el layout XML con este archivo Java
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Ahora puedes encontrar tus vistas (botones, etc.) ---
        // Debes usar "view.findViewById" porque estás dentro de un Fragmento
        etSearchLocation = view.findViewById(R.id.etSearchLocation);
        btnSearch = view.findViewById(R.id.btnSearch);

        // Ejemplo de cómo añadir un listener a tu botón
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí pones lo que quieres que haga el botón "Buscar"
                String ubicacion = etSearchLocation.getText().toString();
                // (Aquí iría la lógica para buscar el clima...)
            }
        });

        // No olvides retornar la "view"
        return view;
    }
}
