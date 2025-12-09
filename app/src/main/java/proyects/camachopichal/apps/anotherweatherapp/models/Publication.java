package proyects.camachopichal.apps.anotherweatherapp.models;

import java.util.Date;

public class Publication {
    private String id;
    private String Url;
    private String titulo;
    private String Descripcion;
    private Date Fecha;
    private String Ubicacion;
    private String nombreUsuario; // Campo para el autor
    private String orientacion;   // Campo para la brújula

    // 1. Constructor vacío (OBLIGATORIO para Firebase Firestore)
    // Firestore lo necesita para convertir el documento JSON a este objeto Java
    public Publication() {}

    // 2. Constructor completo (Para crear objetos manualmente en tu código)
    public Publication(String url, String titulo, String descripcion, Date fecha, String ubicacion, String nombreUsuario, String orientacion) {
        this.Url = url;
        this.titulo = titulo;
        this.Descripcion = descripcion;
        this.Fecha = fecha;
        this.Ubicacion = ubicacion;
        this.nombreUsuario = nombreUsuario;
        this.orientacion = orientacion;
    }

    // --- GETTERS Y SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return Url; }
    public void setUrl(String url) { Url = url; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return Descripcion; }
    public void setDescripcion(String descripcion) { Descripcion = descripcion; }

    public Date getFecha() { return Fecha; }
    public void setFecha(Date fecha) { Fecha = fecha; }

    public String getUbicacion() { return Ubicacion; }
    public void setUbicacion(String ubicacion) { Ubicacion = ubicacion; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getOrientacion() { return orientacion; }
    public void setOrientacion(String orientacion) { this.orientacion = orientacion; }
}