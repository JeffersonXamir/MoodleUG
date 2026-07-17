package ec.edu.ug.moodleug.models;
import com.google.gson.annotations.SerializedName;

public class GradeItem {
    @SerializedName("itemname")
    private String itemname;

    @SerializedName("gradeformatted")
    private String gradeformatted; // Ej: "80.00" o "100.00"

    public String getItemname() { return itemname; }
    public String getGradeformatted() { return gradeformatted; }
}