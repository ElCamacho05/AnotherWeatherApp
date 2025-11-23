package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importa la clase de View Binding generada
import com.google.firebase.auth.FirebaseAuth;

import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    // Se usa un objeto nullable y privado para el binding
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;

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

        // lógica para cargar los datos del usuario
        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void  onViewCreated(@NonNull View view, @Nullable Bundle saveIntanceState){
        super.onViewCreated(view, saveIntanceState);

        cargarEmailUsuario();
        binding.tvLocationsCount.setText("0");
    }

    public void cargarEmailUsuario(){
        // 1. Verificar si hay un usuario logueado
        if (mAuth.getCurrentUser() != null) {

            // 2. Obtener el email directamente del objeto de Firebase User
            String email = mAuth.getCurrentUser().getEmail();

            // 3. Mostrar el email en el TextView
            if (email != null) {
                // Asumo que el TextView para el email tiene el ID: tvUserEmail
                binding.tvUserEmail.setText(email);
            } else {
                binding.tvUserEmail.setText("Email no disponible");
            }

        } else {
            // Si no hay usuario logueado (lo cual no debería pasar si vienes de LoginActivity)
            binding.tvUserEmail.setText("Desconectado");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // IMPORTANTE: Limpiar la referencia del binding para evitar fugas de memoria
        binding = null;
    }
}