package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import androidx.annotation.NonNull;
import android.widget.Toast;
import android.util.Log;

// Importa la clase de View Binding generada
import com.google.firebase.auth.FirebaseAuth;

import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class LoginActivity extends AppCompatActivity {

    // Declaración del objeto Binding
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el layout y obtener la instancia de binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        // 2. Establecer el root view como el contenido de la actividad
        setContentView(binding.getRoot());

        //Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Acceso directo a la vista con "binding.idDeLaVista"
        binding.btnIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un "Intent" para iniciar la nueva actividad
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Esto te mandará a la pantalla principal de la app
        binding.btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica de validación de email/contraseña
                String Email = binding.etEmailLogin.getText().toString();
                String password = binding.etPasswordLogin.getText().toString();

                if (Email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Ingrese email y contraseña.", Toast.LENGTH_SHORT).show();
                    return;
                }

                iniciarSesion(Email, password);
            }
        });

        // Para la lógica para los botones de Google y Apple
    }

    private void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("AUTH", "Inicio de sesión exitoso");

                            // Si el login es correcto, navegamos a MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // Limpiamos la pila de actividades para que el usuario no pueda volver al Login
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            // Si el login falla (contraseña incorrecta, usuario no existe)
                            Log.w("AUTH", "Fallo en inicio de sesión", task.getException());
                            Toast.makeText(LoginActivity.this, "Error de credenciales: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}