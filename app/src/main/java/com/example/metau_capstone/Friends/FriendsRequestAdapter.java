package com.example.metau_capstone.Friends;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.parse.SaveCallback;

import java.util.List;
import java.util.Objects;

import Fragments.Friends.FriendsListFragment;
import Fragments.Main.FriendsFragment;

/**
 ** Adapter used to manage the Friends Request Fragment recycler view
 */
public class FriendsRequestAdapter extends RecyclerView.Adapter<FriendsRequestAdapter.ViewHolder> {
    private static final String TAG = "FriendsRequestAdapter";

    // List in the recycler view
    public List<ParseUser> requests;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    translationManager manager;


    /**
     * Initialize the adapter
     * @param users A list of ParseUser objects which we want to initialize the
     *                recycler view with.
     * @param context Context from the class using this adapter
     * @param fragmentManager Fragment manager using this adapter to handle back presses
     */
    public FriendsRequestAdapter(List<ParseUser> users, Context context, FragmentManager fragmentManager) {
        this.requests = users;
        this.fragmentManager = fragmentManager;
        this.context = context;
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));
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
        TextView tvNumForts;

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
            tvNumForts = itemView.findViewById(R.id.tvNumForts);
            friending = false;

            // Translate any text
            manager.addText(tvNumForts, R.string.numForts, context);
            manager.addText(btnAcceptRequest, R.string.requestAccept, context);
            manager.addText(btnDeclineRequest, R.string.requestDecline, context);
            manager.addText(btnRequestAccepted, R.string.requestAccepted, context);
            manager.addText(btnRequestDeclined, R.string.requestDeclined, context);
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser Nfriend) {
            // Store the friend
            friend = Nfriend;

            // Set the username
            tvFriendUsername_request.setText(friend.getUsername());

            // Store the user image
            ParseFile pic = friend.getParseFile("profilePic");
            if (pic == null) {
                ivFriend_request.setImageResource(R.drawable.default_pfp);
            }
            else {
                Glide.with(context)
                        .load(pic.getUrl())
                        .error(R.drawable.default_pfp)
                        .circleCrop()
                        .into(ivFriend_request);
            }

            // Load in the fortune count
            ParseRelation<Fortune> fortunes = friend.getRelation("fortunes");
            ParseQuery<Fortune> query = fortunes.getQuery();
            query.findInBackground(new FindCallback<Fortune>() {
                @Override
                public void done(List<Fortune> objects, ParseException e) {
                    manager.addText(tvFriendFortuneCt_request, String.valueOf(objects.size()));
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
                    addFriendToQueue(friend, curUser);

                    // Remove the user from the requests
                    removeRequest(curUser, friend, "accept");

                    // Find the friends fragment
                    FragmentManager FriendsFragManager = null;
                    for (Fragment frag : fragmentManager.getFragments()) {
                        if (frag.getClass() == FriendsFragment.class) {
                            FriendsFragManager = frag.getChildFragmentManager();
                            break;
                        }
                    }

                    if (FriendsFragManager == null) {
                        return;
                    }

                    // Find the list fragment and unload it
                    for (Fragment frag : FriendsFragManager.getFragments()) {
                        if (frag.getClass() == FriendsListFragment.class) {
                            FriendsFragManager.beginTransaction().remove(frag).commit();
                            break;
                        }
                    }


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


        /**
         * Given a user and a friend, add the friend to the user's friends list
         * @param user The user to add a new friend to
         * @param friend The friend to add to the user
         */
        private void addFriend(ParseUser user, ParseUser friend) {
            ParseRelation<ParseUser> friends = user.getRelation("friends");
            friends.add(friend);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to friend user", e);
                        manager.createToast(context, "Unable to friend user");
                    }
                    else {
                        manager.createToast(context, "User friended!");
                    }
                }
            });
        }


        /**
         * Given a user and a friend, add the friend to the user's friend's queue
         * @param user The user to add a new friend to
         * @param friend The friend to add to the user
         */
        private void addFriendToQueue(ParseUser user, ParseUser friend) {
            Friend_queue queue = new Friend_queue();
            queue.setFriend(friend);
            queue.setUser(user);
            queue.setMode("add");
            queue.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to save to queue", e);
                    }
                }
            });
        }



        /**
         * Given a user and a friend, remove the friend from the user's requests list
         * @param user The user to remove the request from
         * @param friend The friend that sent the request
         * @param mode The mode in which the request should be removed. "accept" if
         *             the user accepted the request and "reject" if the user
         *             rejected the request.
         */
        private void removeRequest(ParseUser user, ParseUser friend, String mode) {
            ParseRelation<ParseUser> requests = user.getRelation("friend_requests");
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

                        // Set the new button as visible and send a new request
                        // to the database
                        if (Objects.equals(mode, "accept")) {
                            // Send back a accepted request to the database
                            Friend_queue queue = new Friend_queue();
                            queue.setUser(friend);
                            queue.setFriend(user);
                            queue.setMode("accept");
                            queue.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Unable to save accepted request to queue", e);
                                    }
                                    else {
                                        manager.createToast(context, "User friended");
                                        btnRequestAccepted.setVisibility(View.VISIBLE);
                                    }
                                }
                            });


                        }
                        else {
                            // Send back a rejected request to the database
                            Friend_queue queue = new Friend_queue();
                            queue.setUser(friend);
                            queue.setFriend(user);
                            queue.setMode("rejected");
                            queue.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Unable to save rejected request to queue", e);
                                    }
                                    else {
                                        manager.createToast(context, "Request declined");
                                        btnRequestDeclined.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }

                        friending = false;
                    }
                }
            });
        }
    }
}
