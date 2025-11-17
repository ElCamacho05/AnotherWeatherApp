package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    // Se usa un objeto nullable y privado para el binding
    private FragmentProfileBinding binding;

    public ProfileFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflar el layout usando el objeto Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // 2. Obtener la vista raíz (root view)
        View view = binding.getRoot();

        // Aquí pondrías la lógica para cargar los datos del usuario
        // (por ahora ponemos datos de ejemplo)
        // Acceso directo a la vista con "binding.idDeLaVista"
        binding.tvUserEmail.setText("usuario@ejemplo.com");
        binding.tvLocationsCount.setText("2");

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // IMPORTANTE: Limpiar la referencia del binding para evitar fugas de memoria
        binding = null;
    }
}