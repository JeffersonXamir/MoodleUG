package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

public class ForumDiscussion {
    @SerializedName("id")
    private int id; // ID de la discusión

    @SerializedName("name")
    private String name; // Título de la discusión

    @SerializedName("message")
    private String message; // Contenido del mensaje

    @SerializedName("userfullname")
    private String userfullname; // Quién lo publicó

    public int getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getUserfullname() { return userfullname; }
}