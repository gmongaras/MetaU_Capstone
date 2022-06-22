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

public class FriendsRequestAdapter extends RecyclerView.Adapter<FriendsRequestAdapter.ViewHolder> {
    private static final String TAG = "FriendsRequestAdapter";

    // List in the recycler view
    List<ParseUser> requests;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;


    public FriendsRequestAdapter(List<ParseUser> users, Context context, FragmentManager fragmentManager) {
        this.requests = users;
        this.fragmentManager = fragmentManager;
        this.context = context;
    }



    @NonNull
    @Override
    public FriendsRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_request, parent, false);

        // Return the view
        return new FriendsRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsRequestAdapter.ViewHolder holder, int position) {
        // Get the item at the given position
        ParseUser request = requests.get(position);

        // Bind the post to the view holder
        holder.bind(request);

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
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Elements in the item view
        ImageView ivFriend_request;
        TextView tvFriendUsername_request;
        TextView tvFriendFortuneCt_request;
        Button btnAcceptRequest;
        Button btnDeclineRequest;
        Button btnRequestAccepted;
        Button btnRequestDeclined;

        // Store the friend as a part of this item view

        ParseUser friend;

        // Is the user friending someone?
        boolean friending;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend_request = itemView.findViewById(R.id.ivFriend_request);
            tvFriendUsername_request = itemView.findViewById(R.id.tvFriendUsername_request);
            tvFriendFortuneCt_request = itemView.findViewById(R.id.tvFriendFortuneCt_request);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnDeclineRequest = itemView.findViewById(R.id.btnDeclineRequest);
            btnRequestAccepted = itemView.findViewById(R.id.btnRequestAccepted);
            btnRequestDeclined = itemView.findViewById(R.id.btnRequestDeclined);
            friending = false;
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser Nfriend) {
            // Set the username
            tvFriendUsername_request.setText(friend.getUsername());

            // Store the friend
            friend = Nfriend;

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
                                .into(ivFriend_request);
                    }
                    else {
                        Glide.with(context)
                                .load(pic.getUrl())
                                .error(R.drawable.default_pfp)
                                .circleCrop()
                                .into(ivFriend_request);
                    }
                }
            });

            // Load in the fortune count
            ParseRelation<Fortune> fortunes = friend.getRelation("fortunes");
            ParseQuery<Fortune> query = fortunes.getQuery();
            query.findInBackground(new FindCallback<Fortune>() {
                @Override
                public void done(List<Fortune> objects, ParseException e) {
                    tvFriendFortuneCt_request.setText(String.valueOf(objects.size()));
                }
            });

            // Add an onClick listener to the accept request button so the
            // user can accept a friend request
            btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    // When clicked, add this user as a friend in the current
                    // user's friend list
                    friending = true;
                    ParseUser curUser = ParseUser.getCurrentUser();

                    // Add the user as a friend to the current user
                    addFriend(curUser, friend);

                    // Add the current user as a friend of the user that sent the request
                    addFriend(friend, curUser);

                    // Remove the user from the requests
                    removeRequest(curUser, friend, "accept");
                }
            });


            // Add an onClick listener to the decline button so the user can
            // decline the friend request
            btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    // When clicked, add this user as a friend in the current
                    // user's friend list
                    friending = true;
                    ParseUser curUser = ParseUser.getCurrentUser();

                    // Remove the user from the requests
                    removeRequest(curUser, friend, "declined");
                }
            });
        }


        // Given a user and a friend, add the friend to the user's friends list
        private void addFriend(ParseUser user, ParseUser friend) {
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
                    }
                }
            });
        }



        // Given a user and a friend, remove the friend from the user's requests list
        private void removeRequest(ParseUser user, ParseUser friend, String mode) {
            ParseRelation<ParseUser> requests = user.getRelation("requests");
            requests.remove(friend);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to remove request", e);
                    }
                    else {
                        Log.i(TAG, "Request removed!");

                        // Hide the currently shown buttons
                        btnAcceptRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setVisibility(View.INVISIBLE);

                        // Set the new button as visible
                        if (mode == "accept") {
                            Toast.makeText(context, "User friended!", Toast.LENGTH_SHORT).show();
                            btnRequestAccepted.setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show();
                            btnRequestDeclined.setVisibility(View.VISIBLE);
                        }

                        friending = false;
                    }
                }
            });
        }
    }
}