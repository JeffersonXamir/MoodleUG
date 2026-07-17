package ec.edu.ug.moodleug.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GradeReportResponse {
    @SerializedName("usergrades")
    private List<UserGrade> usergrades;

    public List<UserGrade> getUsergrades() { return usergrades; }
}