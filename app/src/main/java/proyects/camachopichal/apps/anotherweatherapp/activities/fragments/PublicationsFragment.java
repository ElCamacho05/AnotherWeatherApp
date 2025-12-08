package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentPublicationsBinding;

public class PublicationsFragment extends Fragment {

    private FragmentPublicationsBinding binding;

    public PublicationsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPublicationsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Configurar el botón flotante (FAB)
        binding.fabNuevaPublicacion.setOnClickListener(v -> {
            // Aquí iría la lógica para abrir la cámara o subir foto
            Toast.makeText(getContext(), "Agregar nueva foto", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}