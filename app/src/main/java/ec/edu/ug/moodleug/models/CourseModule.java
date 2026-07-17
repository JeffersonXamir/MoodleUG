package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CourseModule {
    @SerializedName("id")
    private int id;

    @SerializedName("instance")
    private int instance;
    @SerializedName("name")
    private String name;

    @SerializedName("modname")
    private String modname; // Nos dirá si es "assign" (tarea), "forum", "resource", etc.

    @SerializedName("contents")
    private List<ModuleContent> contents;

    public int getId() { return id; }
    public int getInstance() { return instance; }

    public String getName() { return name; }
    public String getModname() { return modname; }
    public List<ModuleContent> getContents() { return contents; }

    public static class ModuleContent {
        @SerializedName("fileurl")
        private String fileurl;

        public String getFileurl() { return fileurl; }
    }
}