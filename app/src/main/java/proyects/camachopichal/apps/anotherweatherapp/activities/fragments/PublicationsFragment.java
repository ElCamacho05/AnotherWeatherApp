package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import proyects.camachopichal.apps.anotherweatherapp.R;
import proyects.camachopichal.apps.anotherweatherapp.adapters.PublicationsAdapter;
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentPublicationsBinding;
import proyects.camachopichal.apps.anotherweatherapp.models.Publication;

public class PublicationsFragment extends Fragment implements SensorEventListener {

    private FragmentPublicationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;

    // Sensores para la brújula
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;
    private String currentOrientation = "Desconocida"; // Se actualiza en tiempo real

    // Variables temporales para guardar datos AL MOMENTO DE TOMAR LA FOTO
    private Uri photoURI;
    private String tempUbicacion = "";
    private String tempOrientacion = "";

    private List<Publication> publicationList;
    private PublicationsAdapter adapter;

    public PublicationsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPublicationsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inicializar servicios
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Inicializar Sensores
        if (getActivity() != null) {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
        }

        // Configurar Recycler
        binding.rvPublicaciones.setHasFixedSize(true);
        binding.rvPublicaciones.setLayoutManager(new GridLayoutManager(getContext(), 2));
        publicationList = new ArrayList<>();
        // Pasamos el listener para abrir el detalle al hacer clic
        adapter = new PublicationsAdapter(getContext(), publicationList, this::mostrarDetallesPublicacion);
        binding.rvPublicaciones.setAdapter(adapter);

        cargarPublicaciones();

        binding.fabNuevaPublicacion.setOnClickListener(v -> verificarPermisos());

        return view;
    }

    // --- CICLO DE VIDA DE SENSORES ---
    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values;

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation[0] es el azimut en radianes
                float azimuth = (float) Math.toDegrees(orientation[0]);
                currentOrientation = obtenerPuntoCardinal(azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private String obtenerPuntoCardinal(float azimuth) {
        if (azimuth < 0) azimuth += 360;
        if (azimuth >= 337.5 || azimuth < 22.5) return "Norte";
        if (azimuth >= 22.5 && azimuth < 67.5) return "Noreste";
        if (azimuth >= 67.5 && azimuth < 112.5) return "Este";
        if (azimuth >= 112.5 && azimuth < 157.5) return "Sureste";
        if (azimuth >= 157.5 && azimuth < 202.5) return "Sur";
        if (azimuth >= 202.5 && azimuth < 247.5) return "Suroeste";
        if (azimuth >= 247.5 && azimuth < 292.5) return "Oeste";
        if (azimuth >= 292.5 && azimuth < 337.5) return "Noroeste";
        return "N/A";
    }

    // --- PERMISOS Y PREPARACIÓN ---
    private void verificarPermisos() {
        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};

        boolean camaraOtorgada = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean ubicacionOtorgada = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (camaraOtorgada && ubicacionOtorgada) {
            prepararDatosYAbrirCamara();
        } else {
            requestPermissionsLauncher.launch(permisos);
        }
    }

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cam = result.getOrDefault(Manifest.permission.CAMERA, false);
                // No exigimos ubicación estricta, pero sí cámara
                if (cam != null && cam) {
                    prepararDatosYAbrirCamara();
                } else {
                    Toast.makeText(getContext(), "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show();
                }
            });

    private void prepararDatosYAbrirCamara() {
        // 1. Guardar Orientación Actual
        tempOrientacion = currentOrientation;
        tempUbicacion = ""; // Limpiar anterior

        // 2. Obtener Ubicación GPS (si hay permiso)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (!addresses.isEmpty()) {
                            Address addr = addresses.get(0);
                            String calle = addr.getThoroughfare() != null ? addr.getThoroughfare() : "";
                            String ciudad = addr.getLocality() != null ? addr.getLocality() : "";

                            if (!calle.isEmpty() && !ciudad.isEmpty()) {
                                tempUbicacion = calle + ", " + ciudad;
                            } else if (!ciudad.isEmpty()) {
                                tempUbicacion = ciudad;
                            } else {
                                tempUbicacion = "Ubicación GPS detectada";
                            }
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
                // 3. Abrir Cámara (incluso si falla el GPS, abrimos cámara)
                abrirCamara();
            });
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        File photoFile = null;
        try { photoFile = crearArchivoImagen(); } catch (IOException ex) {
            Toast.makeText(getContext(), "Error creando archivo", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(requireContext(), "proyects.camachopichal.apps.anotherweatherapp.provider", photoFile);
            cameraLauncher.launch(photoURI);
        }
    }

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess) mostrarDialogoConfirmacion();
            });

    private File crearArchivoImagen() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        return File.createTempFile(imageFileName, ".jpg", requireContext().getExternalCacheDir());
    }

    // --- DIÁLOGO Y SUBIDA ---
    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_publication, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView iv = view.findViewById(R.id.ivPreviewFoto);
        EditText etTitulo = view.findViewById(R.id.etTituloPub);
        EditText etDesc = view.findViewById(R.id.etDescPub);
        EditText etUbic = view.findViewById(R.id.etUbicacionPub);
        Button btn = view.findViewById(R.id.btnPublicar);

        iv.setImageURI(photoURI);

        // Pre-llenar datos
        if (!tempUbicacion.isEmpty()) etUbic.setText(tempUbicacion);

        // Agregar la orientación sugerida en la descripción si no es desconocida
        if (!tempOrientacion.equals("Desconocida")) {
            etDesc.setText("Capturado hacia el " + tempOrientacion);
        }

        btn.setOnClickListener(v -> {
            String t = etTitulo.getText().toString();
            String d = etDesc.getText().toString();
            String u = etUbic.getText().toString();
            if (t.isEmpty()) Toast.makeText(getContext(), "Título requerido", Toast.LENGTH_SHORT).show();
            else subirFotoYGuardar(t, d, u, dialog);
        });
        dialog.show();
    }

    private void subirFotoYGuardar(String titulo, String desc, String ubic, AlertDialog dialog) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Publicando...");
        pd.setCancelable(false);
        pd.show();

        // 1. Obtener Nombre de Usuario de Firestore
        // Asumiendo que la colección es "Usuario" como en tus capturas
        db.collection("usuario").document(uid).get().addOnSuccessListener(doc -> {
            String nombreUser = "Anónimo";
            if (doc.exists()) {
                // Adaptar según tu estructura real. Si es un mapa anidado:
                Map<String, Object> map = (Map<String, Object>) doc.get("nombre_completo");
                if (map != null) {
                    Object nombreObj = map.get("nombre");
                    Object apObj = map.get("apellido_paterno");
                    if (nombreObj != null) nombreUser = nombreObj.toString();
                    if (apObj != null) nombreUser += " " + apObj.toString();
                }
            }
            final String finalNombre = nombreUser;

            // 2. Subir Imagen
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = storage.getReference().child("fotos_publicaciones/" + fileName);

            ref.putFile(photoURI).addOnSuccessListener(task -> {
                ref.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    // 3. Guardar en Colección GLOBAL "Publicaciones"
                    guardarEnFirestore(uid, downloadUrl.toString(), titulo, desc, ubic, finalNombre, tempOrientacion);
                    pd.dismiss();
                    dialog.dismiss();
                });
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(getContext(), "Error subida: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Si falla obtener usuario, publicamos como Anónimo pero subimos la foto
            pd.dismiss(); // En realidad deberíamos seguir, pero por seguridad cerramos
            Toast.makeText(getContext(), "Error al leer perfil", Toast.LENGTH_SHORT).show();
        });
    }

    private void guardarEnFirestore(String uid, String url, String titulo, String desc, String ubic, String nombreUser, String orientacion) {
        Map<String, Object> pub = new HashMap<>();
        pub.put("uidUsuario", uid);
        pub.put("nombreUsuario", nombreUser);
        pub.put("Url", url);
        pub.put("titulo", titulo);
        pub.put("Descripcion", desc);
        pub.put("Fecha", new Date());
        pub.put("Ubicacion", ubic);
        pub.put("orientacion", orientacion);

        // Guardamos en la RAÍZ para que todos lo vean
        db.collection("Publicaciones").add(pub)
                .addOnSuccessListener(d -> Toast.makeText(getContext(), "¡Publicado!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar en DB", Toast.LENGTH_SHORT).show());
    }

    private void cargarPublicaciones() {
        // Cargar desde la colección raíz "Publicaciones"
        db.collection("Publicaciones").orderBy("Fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        publicationList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // Mapeo manual seguro
                            String u = doc.getString("uidUsuario");
                            String n = doc.getString("nombreUsuario");
                            String url = doc.getString("Url");
                            String t = doc.getString("titulo");
                            String d = doc.getString("Descripcion");
                            String l = doc.getString("Ubicacion");
                            Date f = doc.getDate("Fecha");
                            String o = doc.getString("orientacion");

                            Publication p = new Publication(url, t, d, f, l, n, o);
                            p.setId(doc.getId());
                            publicationList.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Método para mostrar detalles (al hacer clic en una foto del grid)
    private void mostrarDetallesPublicacion(Publication pub) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Usamos un layout personalizado para el detalle
        View view = getLayoutInflater().inflate(R.layout.dialog_publication_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView iv = view.findViewById(R.id.ivDetalleFoto);
        TextView tt = view.findViewById(R.id.tvDetalleTitulo);
        TextView ta = view.findViewById(R.id.tvDetalleAutor);
        TextView tu = view.findViewById(R.id.tvDetalleUbicacion);
        TextView td = view.findViewById(R.id.tvDetalleDescripcion);
        TextView tf = view.findViewById(R.id.tvDetalleFecha); // Asegúrate de tener este ID en el XML
        Button btn = view.findViewById(R.id.btnCerrarDetalle);

        tt.setText(pub.getTitulo());

        String autorTexto = pub.getNombreUsuario() != null ? pub.getNombreUsuario() : "Anónimo";
        ta.setText(autorTexto);

        tu.setText(pub.getUbicacion());
        td.setText(pub.getDescripcion());

        if (pub.getFecha() != null) {
            tf.setText(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", pub.getFecha()));
        }

        if (pub.getUrl() != null) {
            Glide.with(this).load(pub.getUrl()).into(iv);
        }

        btn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}