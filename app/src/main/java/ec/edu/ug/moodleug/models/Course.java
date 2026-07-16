package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("id")
    private int id;

    @SerializedName("shortname")
    private String shortname;

    @SerializedName("fullname")
    private String fullname;

    // Constructores, Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShortname() { return shortname; }
    public void setShortname(String shortname) { this.shortname = shortname; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
}