package proyects.camachopichal.apps.anotherweatherapp.adapters;

import android.content.Context;
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
import proyects.camachopichal.apps.anotherweatherapp.models.SavedLocation;

public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {

    private Context context;
    private List<SavedLocation> locationsList;
    private OnLocationActionListener listener;
    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";

    public interface OnLocationActionListener {
        void onEdit(SavedLocation location);
        void onDelete(SavedLocation location);
        void onClick(SavedLocation location);
    }

    public SavedLocationsAdapter(Context context, List<SavedLocation> locationsList, OnLocationActionListener listener) {
        this.context = context;
        this.locationsList = locationsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation location = locationsList.get(position);

        holder.tvTitle.setText(location.getTitulo());
        holder.tvDesc.setText(location.getDescripcion());
        holder.tvTemp.setText(location.getCurrentTemp() + "°C");

        String iconUrl = ICON_BASE_URL + location.getCurrentIcon() + "@2x.png";
        Glide.with(context).load(iconUrl).into(holder.ivIcon);

        // Listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(location));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(location));
        holder.itemView.setOnClickListener(v -> listener.onClick(location));
    }

    @Override
    public int getItemCount() {
        return locationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTemp;
        ImageView ivIcon, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLocationTitle);
            tvDesc = itemView.findViewById(R.id.tvLocationDesc);
            tvTemp = itemView.findViewById(R.id.tvLocationTemp);
            ivIcon = itemView.findViewById(R.id.ivLocationIcon);
            btnEdit = itemView.findViewById(R.id.btnEditLocation);
            btnDelete = itemView.findViewById(R.id.btnDeleteLocation);
        }
    }
}