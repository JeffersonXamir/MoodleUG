package ec.edu.ug.moodleug.ui.courses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.models.CourseModule;

public class CourseModuleAdapter extends RecyclerView.Adapter<CourseModuleAdapter.ModuleViewHolder> {

    private List<CourseModule> moduleList;
    private OnItemClickListener listener;

    // 1. Interfaz para escuchar el clic
    public interface OnItemClickListener {
        void onItemClick(CourseModule module);
    }

    // 2. Actualizamos constructor
    public CourseModuleAdapter(List<CourseModule> moduleList, OnItemClickListener listener) {
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        CourseModule module = moduleList.get(position);
        holder.textModuleName.setText(module.getName());

        String tipoAmigable = "Recurso";
        String icono = "R";

        if ("assign".equals(module.getModname())) {
            tipoAmigable = "Tarea";
            icono = "T";
        } else if ("forum".equals(module.getModname())) {
            tipoAmigable = "Foro";
            icono = "F";
        } else if ("resource".equals(module.getModname()) || "folder".equals(module.getModname())) {
            tipoAmigable = "Archivo/Carpeta";
            icono = "A";
        } else if ("quiz".equals(module.getModname())) {
            tipoAmigable = "Cuestionario";
            icono = "C";
        }

        holder.textModuleType.setText(tipoAmigable);
        holder.textModuleTypeIcon.setText(icono);

        // 3. Pasamos el clic a la actividad
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(module);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleList != null ? moduleList.size() : 0;
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        TextView textModuleName, textModuleType, textModuleTypeIcon;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            textModuleName = itemView.findViewById(R.id.textModuleName);
            textModuleType = itemView.findViewById(R.id.textModuleType);
            textModuleTypeIcon = itemView.findViewById(R.id.textModuleTypeIcon);
        }
    }
}