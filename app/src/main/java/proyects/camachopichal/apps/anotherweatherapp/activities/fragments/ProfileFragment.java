package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentProfileBinding;
import proyects.camachopichal.apps.anotherweatherapp.activities.LoginActivity;

// Importaciones de Firebase y Google
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;

    public ProfileFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout usando View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Cargar datos del usuario (Email)
        cargarEmailUsuario();

        // 2. Configurar el botón de Cerrar Sesión
        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesionUsuario());
    }

    private void cargarEmailUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null) {
                binding.tvUserEmail.setText(email);
            } else {
                binding.tvUserEmail.setText("Email no disponible");
            }
        } else {
            binding.tvUserEmail.setText("Desconectado");
        }
    }

    /**
     * Lógica del Punto 2: Cerrar sesión y redirigir
     */
    private void cerrarSesionUsuario() {
        // A. Cerrar sesión en Firebase (Cierra Email/Password y Google en el backend)
        mAuth.signOut();

        // B. Cerrar sesión en el cliente de Google
        // (Esto es vital para que si usas Google Sign-In, te deje elegir cuenta la próxima vez)
        GoogleSignIn.getClient(requireActivity(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        // C. Redirigir al LoginActivity y limpiar la pila
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        // Estas banderas (flags) borran el historial de pantallas para que al dar "Atrás" la app se cierre en lugar de volver al perfil
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(getContext(), "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}