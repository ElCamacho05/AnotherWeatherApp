package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityRegisterBinding;

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
                String Nombre=binding.etNombre.getText().toString();
                String ApPaterno=binding.etApellidoPaterno.getText().toString();
                String ApMaterno=binding.etApellidoMaterno.getText().toString();
                String Email=binding.etEmail.getText().toString();
                String password=binding.etPassword.getText().toString();
                String Telefono=binding.etTelefono.getText().toString();

                String CP=binding.etCodigoPostal.getText().toString();
                String Calle = binding.etCalle.getText().toString();
                String Numero = binding.etNumero.getText().toString();

                //VALIDACIONES
                if(Email.isEmpty() || password.isEmpty() || Nombre.isEmpty() || ApPaterno.isEmpty() || ApMaterno.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"Ingresa todos los datos",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegistrarUsuario(Email, password, Nombre, ApPaterno, ApMaterno, Telefono, Calle, Numero, CP);

            }
        });
    }

    private void RegistrarUsuario(String Email, String password, String Nombre,String ApPaterno, String ApMaterno, String Telefono, String Calle, String Numero,String CP){
        mAuth.createUserWithEmailAndPassword(Email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String uid = mAuth.getCurrentUser().getUid();
                            // --- PASO 2: GUARDAR PERFIL EN FIRESTORE (DATOS) ---
                            guardarPerfilEnFirestore(uid, Email, Nombre, ApPaterno, ApMaterno, Telefono, Calle, Numero, CP);
                        }else {
                            Log.w("AUTH", "Fallo al crear usuario.", task.getException());
                            Toast.makeText(RegisterActivity.this, "Fallo en registro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void guardarPerfilEnFirestore(String uid, String Email, String Nombre, String ApPaterno, String ApMaterno, String Telefono, String Calle, String Numero, String CP) {

        // --- 1. Crear el MAPA ANIDADO para el Nombre Completo ---
        Map<String, Object> Nombre_Completo = new HashMap<>();
        Nombre_Completo.put("nombre", Nombre);
        Nombre_Completo.put("apellido_paterno", ApPaterno);
        Nombre_Completo.put("apellido_materno", ApMaterno);

        // --- 2. Crear el MAPA ANIDADO para la Dirección ---
        Map<String, Object> Direccion = new HashMap<>();
        Direccion.put("calle", Calle);
        Direccion.put("numero", Numero);
        Direccion.put("codigo_postal", CP);

        // --- 3. Crear el MAPA PRINCIPAL para el Documento ---
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", uid);
        usuario.put("email", Email);
        usuario.put("telefono", Telefono);
        usuario.put("fecha_registro", new Date());

        // AÑADIMOS LOS MAPAS ANIDADOS
        usuario.put("nombre_completo", Nombre_Completo);
        usuario.put("direccion", Direccion);

        // --- Guardar en Firestore ---
        db.collection("Usuario").document(uid)
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