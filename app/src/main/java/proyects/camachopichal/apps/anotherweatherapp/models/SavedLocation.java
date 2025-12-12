package proyects.camachopichal.apps.anotherweatherapp.models;

import com.google.firebase.firestore.Exclude;

public class SavedLocation {
    private String id;
    private String titulo;
    private String descripcion; // del geocoder + lo que el usuario quiera poner
    private double latitud;
    private double longitud;

    @Exclude private String currentTemp = "--";
    @Exclude private String currentIcon = "01d"; // sol por default

    public SavedLocation() {}

    public SavedLocation(String id, String titulo, String descripcion, double latitud, double longitud) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    // Metodos para datos temporales (del clima)
    @Exclude public String getCurrentTemp() { return currentTemp; }
    @Exclude public void setCurrentTemp(String currentTemp) { this.currentTemp = currentTemp; }

    @Exclude public String getCurrentIcon() { return currentIcon; }
    @Exclude public void setCurrentIcon(String currentIcon) { this.currentIcon = currentIcon; }
}