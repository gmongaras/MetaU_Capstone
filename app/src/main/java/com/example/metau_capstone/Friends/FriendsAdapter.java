package com.example.metau_capstone.Friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.example.metau_capstone.translationManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

import Fragments.Main.ProfileFragment;

/**
 ** Adapter used to manage the Friends List Fragment recycler view
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    // List in the recycler view
    public List<ParseUser> friends;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    translationManager manager;

    /**
     * Initialize the adapter
     * @param friends A list of ParseUser objects which we want to initialize the
     *                recycler view with.
     * @param context Context from the class using this adapter
     * @param fragmentManager Fragment manager using this adapter to handle back presses
     */
    public FriendsAdapter(List<ParseUser> friends, Context context, FragmentManager fragmentManager) {
        this.friends = friends;
        this.fragmentManager = fragmentManager;
        this.context = context;
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);

        // Return the view
        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.ViewHolder holder, int position) {
        // Get the item at the given position
        ParseUser friend = friends.get(position);

        // Bind the post to the view holder
        holder.bind(friend);

        // Put an onClick listener on the view to go into the detailed view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup the fragment switch
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ProfileFragment fragmentProfile = ProfileFragment.newInstance(friend, 1);

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentProfile);
                ft.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Elements in the item view
        ImageView ivFriend;
        TextView tvFriendUsername;
        TextView tvFriendFortuneCt;
        TextView tvNumFortsList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend = itemView.findViewById(R.id.ivFriend);
            tvFriendUsername = itemView.findViewById(R.id.tvFriendUsername);
            tvFriendFortuneCt = itemView.findViewById(R.id.tvFriendFortuneCt);
            tvNumFortsList = itemView.findViewById(R.id.tvNumForts);

            // Translate the text in the elements
            manager.addText(tvNumFortsList, R.string.numForts, context);
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser friend) {
            // Set the username
            tvFriendUsername.setText(friend.getUsername());

            // Store the user image
            ParseFile pic = friend.getParseFile("profilePic");
            if (pic == null) {
                ivFriend.setImageResource(R.drawable.default_pfp);
            }
            else {
                Glide.with(context)
                        .load(pic.getUrl())
                        .error(R.drawable.default_pfp)
                        .circleCrop()
                        .into(ivFriend);
            }

            // Load in the fortune count
            ParseRelation<Fortune> fortunes = friend.getRelation("fortunes");
            ParseQuery<Fortune> query = fortunes.getQuery();
            query.findInBackground(new FindCallback<Fortune>() {
                @Override
                public void done(List<Fortune> objects, ParseException e) {
                    manager.addText(tvFriendFortuneCt, String.valueOf(objects.size()));
                }
            });
        }
    }
}
