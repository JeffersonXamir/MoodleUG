package ec.edu.ug.moodleug.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForumDiscussionsResponse {
    @SerializedName("discussions")
    private List<ForumDiscussion> discussions;

    public List<ForumDiscussion> getDiscussions() {
        return discussions;
    }
}