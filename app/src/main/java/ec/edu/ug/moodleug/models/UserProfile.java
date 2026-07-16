package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("fullname")
    private String fullname;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getFullname() { return fullname; }
}