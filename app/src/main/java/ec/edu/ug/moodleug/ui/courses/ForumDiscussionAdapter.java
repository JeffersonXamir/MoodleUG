package ec.edu.ug.moodleug.ui.courses;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.models.ForumDiscussion;

public class ForumDiscussionAdapter extends RecyclerView.Adapter<ForumDiscussionAdapter.DiscussionViewHolder> {

    private List<ForumDiscussion> discussionList;
    private OnItemClickListener listener;

    // 1. Interfaz para escuchar el clic
    public interface OnItemClickListener {
        void onItemClick(ForumDiscussion discussion);
    }

    // 2. Constructor actualizado
    public ForumDiscussionAdapter(List<ForumDiscussion> discussionList, OnItemClickListener listener) {
        this.discussionList = discussionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forum_discussion, parent, false);
        return new DiscussionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position) {
        ForumDiscussion discussion = discussionList.get(position);

        holder.textDiscussionTitle.setText(discussion.getName());
        holder.textDiscussionAuthor.setText("Publicado por: " + discussion.getUserfullname());

        if (discussion.getMessage() != null) {
            holder.textDiscussionMessage.setText(Html.fromHtml(discussion.getMessage(), Html.FROM_HTML_MODE_LEGACY).toString().trim());
        }

        // 3. Pasamos el clic a la Actividad principal
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(discussion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return discussionList != null ? discussionList.size() : 0;
    }

    static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        TextView textDiscussionTitle, textDiscussionAuthor, textDiscussionMessage;

        public DiscussionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDiscussionTitle = itemView.findViewById(R.id.textDiscussionTitle);
            textDiscussionAuthor = itemView.findViewById(R.id.textDiscussionAuthor);
            textDiscussionMessage = itemView.findViewById(R.id.textDiscussionMessage);
        }
    }
}