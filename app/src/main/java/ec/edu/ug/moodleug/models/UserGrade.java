package ec.edu.ug.moodleug.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserGrade {
    @SerializedName("gradeitems")
    private List<GradeItem> gradeitems;

    public List<GradeItem> getGradeitems() { return gradeitems; }
}