package proyects.camachopichal.apps.anotherweatherapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import proyects.camachopichal.apps.anotherweatherapp.R;
import proyects.camachopichal.apps.anotherweatherapp.models.Publication;

public class PublicationsAdapter extends RecyclerView.Adapter<PublicationsAdapter.ViewHolder> {

    private Context context;
    private List<Publication> publicationsList;
    private OnItemClickListener listener; // Listener para manejar clics

    // Interfaz para el clic en el item
    public interface OnItemClickListener {
        void onItemClick(Publication publication);
    }

    // Constructor actualizado con el listener
    public PublicationsAdapter(Context context, List<Publication> publicationsList, OnItemClickListener listener) {
        this.context = context;
        this.publicationsList = publicationsList;
        this.listener = listener;
    }

    // Constructor simple (por si lo necesitas en otro lado sin clic)
    public PublicationsAdapter(Context context, List<Publication> publicationsList) {
        this.context = context;
        this.publicationsList = publicationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publication publication = publicationsList.get(position);

        // Asignar textos
        holder.tvTitulo.setText(publication.getTitulo());

        if (publication.getUbicacion() != null) {
            holder.tvUbicacion.setText(publication.getUbicacion());
        }

        // Mostrar autor si existe
        if (publication.getNombreUsuario() != null) {
            holder.tvAutor.setText("Por: " + publication.getNombreUsuario());
        } else {
            holder.tvAutor.setText("Por: Anónimo");
        }

        // Mostrar orientación si existe
        if (publication.getOrientacion() != null) {
            holder.tvOrientacion.setText(publication.getOrientacion());
        }

        // Formatear fecha
        if (publication.getFecha() != null) {
            CharSequence fechaStr = DateFormat.format("dd/MM/yyyy", publication.getFecha());
            holder.tvFecha.setText(fechaStr);
        }

        // Cargar imagen con Glide
        if (publication.getUrl() != null && !publication.getUrl().isEmpty()) {
            Glide.with(context)
                    .load(publication.getUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivFoto);
        }

        // Configurar el clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(publication);
            }
        });
    }

    @Override
    public int getItemCount() {
        return publicationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvUbicacion, tvAutor, tvOrientacion;
        ImageView ivFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvPublicacionTitulo);
            tvFecha = itemView.findViewById(R.id.tvPublicacionFecha);
            tvUbicacion = itemView.findViewById(R.id.tvPublicacionUbicacion);
            tvAutor = itemView.findViewById(R.id.tvPublicacionAutor); // Asegúrate de tener este ID en el XML
            tvOrientacion = itemView.findViewById(R.id.tvPublicacionOrientacion); // Asegúrate de tener este ID en el XML
            ivFoto = itemView.findViewById(R.id.ivPublicacionFoto);
        }
    }
}