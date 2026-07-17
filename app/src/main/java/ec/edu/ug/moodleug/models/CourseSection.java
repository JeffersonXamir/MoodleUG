package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourseSection {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("modules")
    private List<CourseModule> modules; // La lista de actividades dentro de este tema

    public int getId() { return id; }
    public String getName() { return name; }
    public List<CourseModule> getModules() { return modules; }
}