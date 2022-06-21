package com.example.metau_capstone;

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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

import Fragments.ProfileDetailFragment;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    // List in the recycler view
    List<ParseUser> friends;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    public FriendsAdapter(List<ParseUser> friends, Context context, FragmentManager fragmentManager) {
        this.friends = friends;
        this.fragmentManager = fragmentManager;
        this.context = context;
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
//                // Setup the fragment switch
//                FragmentTransaction ft = fragmentManager.beginTransaction();
//
//                // Create the fragment with paramters
//                ProfileDetailFragment fragmentProfileDetail = ProfileDetailFragment.newInstance(fortune);
//
//                // Change the fragment
//                ft.replace(R.id.flContainer, fragmentProfileDetail);
//                ft.commit();
                return;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend = itemView.findViewById(R.id.ivFriend);
            tvFriendUsername = itemView.findViewById(R.id.tvFriendUsername);
            tvFriendFortuneCt = itemView.findViewById(R.id.tvFriendFortuneCt);
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser friend) {
            // Set the username
            tvFriendUsername.setText(friend.getUsername());

            // Store the user image
            ParseQuery<ParseUser> q = new ParseQuery<>(ParseUser.class);
            q.whereEqualTo("objectId", friend.getObjectId());
            q.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> user, ParseException e) {
                    ParseFile pic = user.get(0).getParseFile("profilePic");
                    if (pic == null) {
                        Glide.with(context)
                                .load(R.drawable.default_pfp)
                                .circleCrop()
                                .into(ivFriend);
                    }
                    else {
                        Glide.with(context)
                                .load(pic.getUrl())
                                .error(R.drawable.default_pfp)
                                .circleCrop()
                                .into(ivFriend);
                    }
                }
            });

            // Load in the fortune count
            ParseRelation<Fortune> fortunes = friend.getRelation("fortunes");
            ParseQuery<Fortune> query = fortunes.getQuery();
            query.findInBackground(new FindCallback<Fortune>() {
                @Override
                public void done(List<Fortune> objects, ParseException e) {
                    tvFriendFortuneCt.setText(String.valueOf(objects.size()));
                }
            });
        }
    }
}
