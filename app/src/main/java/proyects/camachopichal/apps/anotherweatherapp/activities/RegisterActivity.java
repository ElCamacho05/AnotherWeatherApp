package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityRegisterBinding;
import proyects.camachopichal.apps.anotherweatherapp.R;

// --- Importaciones de Firebase ---
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    // Declaración del objeto Binding
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el layout y obtener la instancia de binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());

        // 2. Establecer el root view como el contenido de la actividad
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OBTENER LOS DATOS DEL VIEW
                String nombre=binding.etNombre.getText().toString();
                String apPaterno=binding.etApellidoPaterno.getText().toString();
                String apMaterno=binding.etApellidoMaterno.getText().toString();
                String email=binding.etEmail.getText().toString();
                String password=binding.etPassword.getText().toString();
                String telefono=binding.etTelefono.getText().toString();

                String cp=binding.etCodigoPostal.getText().toString();
                String calle = binding.etCalle.getText().toString();
                String numero = binding.etNumero.getText().toString();

                //VALIDACIONES
                if(email.isEmpty() || password.isEmpty() || nombre.isEmpty() || apPaterno.isEmpty() || apMaterno.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"Ingresa todos los datos",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegistrarUsuario(email, password, nombre, apPaterno, apMaterno, telefono, calle, numero, cp);

            }
        });
    }

    private void RegistrarUsuario(String email, String password, String nombre,String apPaterno, String apMaterno, String telefono, String calle, String numero,String cp){
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String uid = mAuth.getCurrentUser().getUid();
                            // --- PASO 2: GUARDAR PERFIL EN FIRESTORE (DATOS) ---
                            guardarPerfilEnFirestore(uid, email, nombre, apPaterno, apMaterno, telefono, calle, numero, cp);
                        }else {
                            Log.w("AUTH", "Fallo al crear usuario.", task.getException());
                            Toast.makeText(RegisterActivity.this, "Fallo en registro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void guardarPerfilEnFirestore(String uid, String email, String nombre, String apPaterno, String apMaterno, String telefono, String calle, String numero, String cp) {

        // --- 1. Crear el MAPA ANIDADO para el Nombre Completo ---
        Map<String, Object> nombreCompletoMap = new HashMap<>();
        nombreCompletoMap.put("nombre", nombre);
        nombreCompletoMap.put("apellido_paterno", apPaterno);
        nombreCompletoMap.put("apellido_materno", apMaterno);

        // --- 2. Crear el MAPA ANIDADO para la Dirección ---
        Map<String, Object> direccionMap = new HashMap<>();
        direccionMap.put("calle", calle);
        direccionMap.put("numero", numero);
        // --- INCLUSIÓN DEL NUEVO CAMPO ---
        direccionMap.put("codigo_postal", cp);

        // --- 3. Crear el MAPA PRINCIPAL para el Documento ---
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", uid);
        usuario.put("email", email);
        usuario.put("telefono", telefono);
        usuario.put("fecha_registro", new Date());

        // AÑADIMOS LOS MAPAS ANIDADOS
        usuario.put("nombre_completo", nombreCompletoMap);
        usuario.put("direccion", direccionMap);

        // --- Guardar en Firestore ---
        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Registro completo.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error de DB.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    
}