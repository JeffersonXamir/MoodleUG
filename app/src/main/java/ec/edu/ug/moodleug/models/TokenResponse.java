package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("error")
    private String error;

    public String getToken() { return token; }
    public String getError() { return error; }
}