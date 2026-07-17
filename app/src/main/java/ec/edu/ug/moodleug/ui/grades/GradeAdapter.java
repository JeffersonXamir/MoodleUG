package ec.edu.ug.moodleug.ui.grades;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.models.GradeItem;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<GradeItem> gradeList;

    public GradeAdapter(List<GradeItem> gradeList) {
        this.gradeList = gradeList;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        GradeItem grade = gradeList.get(position);

        // Si el nombre viene vacío, suele ser el "Total del curso"
        if (grade.getItemname() == null || grade.getItemname().isEmpty()) {
            holder.textGradeItemName.setText("Total del curso");
        } else {
            holder.textGradeItemName.setText(grade.getItemname());
        }

        // Si la nota viene nula o vacía, mostramos un guion
        if (grade.getGradeformatted() == null || grade.getGradeformatted().isEmpty()) {
            holder.textGradeValue.setText("-");
        } else {
            holder.textGradeValue.setText(grade.getGradeformatted());
        }
    }

    @Override
    public int getItemCount() {
        return gradeList != null ? gradeList.size() : 0;
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView textGradeItemName, textGradeValue;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            textGradeItemName = itemView.findViewById(R.id.textGradeItemName);
            textGradeValue = itemView.findViewById(R.id.textGradeValue);
        }
    }
}