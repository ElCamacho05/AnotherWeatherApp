package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentHomeBinding;

// Se eliminan los imports de Button y EditText, y las declaraciones de las vistas
// ya que se accede a ellas a través del objeto binding.

public class HomeFragment extends Fragment {

    // Se usa un objeto nullable y privado para el binding
    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflar el layout usando el objeto Binding.
        // El tercer parámetro (false) es necesario para fragments.
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // 2. Obtener la vista raíz (root view) del layout inflado
        View view = binding.getRoot();

        // --- Ahora puedes acceder a tus vistas (botones, etc.) directamente desde "binding" ---
        // Ejemplo de cómo añadir un listener a tu botón
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí pones lo que quieres que haga el botón "Buscar"
                String ubicacion = binding.etSearchLocation.getText().toString();
                // (Aquí iría la lógica para buscar el clima...)
            }
        });

        // No olvides retornar la "view"
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // IMPORTANTE: Limpiar la referencia del binding para evitar fugas de memoria
        // cuando la vista del fragmento se destruye.
        binding = null;
    }
}