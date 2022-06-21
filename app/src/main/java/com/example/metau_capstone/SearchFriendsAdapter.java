package com.example.metau_capstone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Objects;

public class SearchFriendsAdapter extends RecyclerView.Adapter<SearchFriendsAdapter.ViewHolder> {
    private static final String TAG = "SearchFriendsAdapter";

    // List in the recycler view
    List<ParseUser> users;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    public SearchFriendsAdapter(List<ParseUser> users, Context context, FragmentManager fragmentManager) {
        this.users = users;
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_friends, parent, false);

        // Return the view
        return new SearchFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchFriendsAdapter.ViewHolder holder, int position) {
        // Get the item at the given position
        ParseUser friend = users.get(position);

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
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Elements in the item view
        ImageView ivFriend_search;
        TextView tvFriendUsername_search;
        TextView tvFriendFortuneCt_search;
        Button btnAddFriend;
        Button btnRemoveFiend;

        // Is the user friending someone?
        boolean friending;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend_search = itemView.findViewById(R.id.ivFriend_search);
            tvFriendUsername_search = itemView.findViewById(R.id.tvFriendUsername_search);
            tvFriendFortuneCt_search = itemView.findViewById(R.id.tvFriendFortuneCt_search);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
            btnRemoveFiend = itemView.findViewById(R.id.btnRemoveFiend);
            friending = false;
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser friend) {
            // Set the username
            tvFriendUsername_search.setText(friend.getUsername());

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
                                .into(ivFriend_search);
                    }
                    else {
                        Glide.with(context)
                                .load(pic.getUrl())
                                .error(R.drawable.default_pfp)
                                .circleCrop()
                                .into(ivFriend_search);
                    }
                }
            });

            // Load in the fortune count
            ParseRelation<Fortune> fortunes = friend.getRelation("fortunes");
            ParseQuery<Fortune> query = fortunes.getQuery();
            query.findInBackground(new FindCallback<Fortune>() {
                @Override
                public void done(List<Fortune> objects, ParseException e) {
                    tvFriendFortuneCt_search.setText(String.valueOf(objects.size()));
                }
            });

            // Get all the user's friends and check if the given friend is
            // in their list to show the correct display
            ParseRelation<ParseUser> users = ParseUser.getCurrentUser().getRelation("friends");
            ParseQuery<ParseUser> friends_query = users.getQuery();
            friends_query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e) {
                    // Iterate over all users. If the current given friend is in that
                    // list, show the proper button.
                    String id = friend.getObjectId();
                    for (ParseUser item_friend : friends) {
                        if (Objects.equals(item_friend.getObjectId(), id)) {
                            btnAddFriend.setVisibility(View.INVISIBLE);
                            btnRemoveFiend.setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                    btnRemoveFiend.setVisibility(View.INVISIBLE);
                    btnAddFriend.setVisibility(View.VISIBLE);
                }
            });

            // Add an onClick listener to the add friend button so the user can add
            // this user as a friend.
            btnAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    // When clicked, add this user as a friend in the current
                    // user's friend list
                    friending = true;
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseRelation<ParseUser> friends = user.getRelation("friends");
                    friends.add(friend);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Unable to friend user", e);
                                Toast.makeText(context, "Unable to friend user", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "User friended!", Toast.LENGTH_SHORT).show();

                                // Swap the buttons
                                btnAddFriend.setVisibility(View.INVISIBLE);
                                btnRemoveFiend.setVisibility(View.VISIBLE);
                                friending = false;
                            }
                        }
                    });
                }
            });


            // Add an onClick listener to the remove friend button so the user can remove
            // this user as a friend.
            btnRemoveFiend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    // When clicked, add this user as a friend in the current
                    // user's friend list
                    friending = true;
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseRelation<ParseUser> friends = user.getRelation("friends");
                    friends.remove(friend);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Unable to remove friend", e);
                                Toast.makeText(context, "Unable to remove friend", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "User unfriended", Toast.LENGTH_SHORT).show();

                                // Swap the buttons
                                btnRemoveFiend.setVisibility(View.INVISIBLE);
                                btnAddFriend.setVisibility(View.VISIBLE);
                                friending = false;
                            }
                        }
                    });
                }
            });


        }
    }
}
