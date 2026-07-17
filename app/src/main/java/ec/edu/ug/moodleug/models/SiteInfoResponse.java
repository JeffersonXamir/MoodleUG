package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

public class SiteInfoResponse {
    @SerializedName("userid")
    private int userid;

    @SerializedName("fullname")
    private String fullname;

    public int getUserid() { return userid; }
    public String getFullname() { return fullname; }
}