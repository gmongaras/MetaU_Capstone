package com.example.metau_capstone.Friends;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Objects;

import Fragments.Main.FriendsFragment;
import Fragments.Main.ProfileFragment;

public class FriendsSearchAdapter extends RecyclerView.Adapter<FriendsSearchAdapter.ViewHolder> {
    private static final String TAG = "FriendsSearchAdapter";

    // List in the recycler view
    List<ParseUser> users;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    public FriendsSearchAdapter(List<ParseUser> users, Context context, FragmentManager fragmentManager) {
        this.users = users;
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendsSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_search, parent, false);

        // Return the view
        return new FriendsSearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsSearchAdapter.ViewHolder holder, int position) {
        // Get the item at the given position
        ParseUser friend = users.get(position);

        // Bind the post to the view holder
        holder.bind(friend);

        // Default mode is 2
        holder.mode = 2;

        // Get the mode of the current user. Is it a friend (1) or another user (2)?
        ParseRelation<ParseUser> friends = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<ParseUser> q = friends.getQuery();
        q.whereEqualTo("objectId", friend.getObjectId());
        q.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {

                // If the objects are empty, the user is not a friend, so the mode is 2.
                if (objects.size() == 0) {
                    ParseRelation<ParseUser> rel = ParseUser.getCurrentUser().getRelation("Blocked");
                    ParseQuery<ParseUser> query = rel.getQuery();
                    query.whereEqualTo("objectId", friend.getObjectId());
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            // If the number of objects is 0, the other user is
                            // not blocked by this user
                            if (objects.size() == 0) {
                                // Check if the other user blocked the logged in user
                                ParseRelation<ParseUser> rel = friend.getRelation("Blocked");
                                ParseQuery<ParseUser> query = rel.getQuery();
                                query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                                query.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> objects, ParseException e) {
                                        // If the number of objects is 0, then the logged in
                                        // user is not blocked by the other user and is
                                        // not a friend of the other users
                                        if (objects.size() == 0) {
                                            holder.mode = 2;
                                        }
                                        else {
                                            holder.mode = 4;
                                        }
                                    }
                                });
                            }
                            else {
                                holder.mode = 3;
                            }
                        }
                    });


                }
                else {
                    holder.mode = 1;
                }
            }
        });

        // Put an onClick listener on the view to go into the detailed view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup the fragment switch
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ProfileFragment fragmentProfile = ProfileFragment.newInstance(friend, holder.mode);

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentProfile);
                ft.commit();
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
        Button btnSendRequest;
        Button btnRemoveRequest;
        Button btnAlreadyFriends;
        Button btnHaveARequest;
        Button btnNotAcceptingFriends;
        Button btnBlocked;
        Button btnBlockedBy;

        // The current user
        ParseUser curUser;

        // Is the user friending someone?
        boolean friending;

        // User mode
        public int mode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend_search = itemView.findViewById(R.id.ivFriend_search);
            tvFriendUsername_search = itemView.findViewById(R.id.tvFriendUsername_search);
            tvFriendFortuneCt_search = itemView.findViewById(R.id.tvFriendFortuneCt_search);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            btnRemoveRequest = itemView.findViewById(R.id.btnRemoveRequest);
            btnAlreadyFriends = itemView.findViewById(R.id.btnAlreadyFriends);
            btnHaveARequest = itemView.findViewById(R.id.btnHaveARequest);
            btnNotAcceptingFriends = itemView.findViewById(R.id.btnNotAcceptingFriends);
            btnBlocked = itemView.findViewById(R.id.btnBlocked);
            btnBlockedBy = itemView.findViewById(R.id.btnBlockedBy);
            friending = false;

            // Get the current user
            curUser = ParseUser.getCurrentUser();
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser friend) {
            // Make all buttons invisible to start
            btnRemoveRequest.setVisibility(View.INVISIBLE);
            btnAlreadyFriends.setVisibility(View.INVISIBLE);
            btnHaveARequest.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.INVISIBLE);
            btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
            btnBlockedBy.setVisibility(View.INVISIBLE);
            btnBlocked.setVisibility(View.INVISIBLE);

            // Set the username
            tvFriendUsername_search.setText(friend.getUsername());

            // Store the user image
            ParseFile pic = friend.getParseFile("profilePic");
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
            ParseRelation<ParseUser> users = curUser.getRelation("friends");
            ParseQuery<ParseUser> friends_query = users.getQuery();
            friends_query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e) {
                    // Iterate over all users. If the current given friend is in that
                    // list, show the proper button.
                    String id = friend.getObjectId();
                    for (ParseUser item_friend : friends) {
                        if (Objects.equals(item_friend.getObjectId(), id)) {
                            btnRemoveRequest.setVisibility(View.INVISIBLE);
                            btnSendRequest.setVisibility(View.INVISIBLE);
                            btnHaveARequest.setVisibility(View.INVISIBLE);
                            btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                            btnBlockedBy.setVisibility(View.INVISIBLE);
                            btnBlocked.setVisibility(View.INVISIBLE);
                            btnAlreadyFriends.setVisibility(View.VISIBLE);
                            return;
                        }
                    }

                    ParseRelation<ParseUser> rel = ParseUser.getCurrentUser().getRelation("Blocked");
                    ParseQuery<ParseUser> query = rel.getQuery();
                    query.whereEqualTo("objectId", friend.getObjectId());
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            // If the number of objects is 0, the other user is
                            // not blocked by this user
                            if (objects.size() == 0) {
                                // Check if the other user blocked the logged in user
                                ParseRelation<ParseUser> rel = friend.getRelation("Blocked");
                                ParseQuery<ParseUser> query = rel.getQuery();
                                query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                                query.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> objects, ParseException e) {
                                        // If the number of objects is 0, then the logged in
                                        // user is not blocked by the other user and is
                                        // not a friend of the other users
                                        if (objects.size() == 0) {
                                            // If the other user is not in the friends list and not blocked,
                                            // check if the user is in the requests list
                                            ParseRelation<ParseUser> requests = curUser.getRelation("friend_requests");
                                            ParseQuery<ParseUser> requests_query = requests.getQuery();
                                            requests_query.whereEqualTo("objectId", friend.getObjectId());
                                            requests_query.findInBackground(new FindCallback<ParseUser>() {
                                                @Override
                                                public void done(List<ParseUser> objects, ParseException e) {
                                                    // If the list is not empty, then there is a request
                                                    if (objects.size() > 0) {
                                                        btnRemoveRequest.setVisibility(View.INVISIBLE);
                                                        btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                                        btnSendRequest.setVisibility(View.INVISIBLE);
                                                        btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                                        btnBlockedBy.setVisibility(View.INVISIBLE);
                                                        btnBlocked.setVisibility(View.INVISIBLE);
                                                        btnHaveARequest.setVisibility(View.VISIBLE);
                                                        return;
                                                    }

                                                    // If the other user is not in the friend_requests list
                                                    // or the friends list, check if they are in the
                                                    // sent_requests list meaning a request was sent to
                                                    // the other user
                                                    ParseRelation<ParseUser> sent = curUser.getRelation("sent_requests");
                                                    ParseQuery<ParseUser> sent_query = sent.getQuery();
                                                    sent_query.whereEqualTo("objectId", friend.getObjectId());
                                                    sent_query.findInBackground(new FindCallback<ParseUser>() {
                                                        @Override
                                                        public void done(List<ParseUser> objects, ParseException e) {
                                                            // If an object exists, then the other user has a request
                                                            // sent to them, so allow the user to remove that request
                                                            if (objects.size() > 0) {
                                                                btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                                                btnHaveARequest.setVisibility(View.INVISIBLE);
                                                                btnSendRequest.setVisibility(View.INVISIBLE);
                                                                btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                                                btnBlockedBy.setVisibility(View.INVISIBLE);
                                                                btnBlocked.setVisibility(View.INVISIBLE);
                                                                btnRemoveRequest.setVisibility(View.VISIBLE);
                                                                return;
                                                            }

                                                            // If the user is not accepting requests, show
                                                            // the proper button
                                                            if (friend.getBoolean("friendable") == false) {
                                                                btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                                                btnHaveARequest.setVisibility(View.INVISIBLE);
                                                                btnSendRequest.setVisibility(View.INVISIBLE);
                                                                btnRemoveRequest.setVisibility(View.INVISIBLE);
                                                                btnBlockedBy.setVisibility(View.INVISIBLE);
                                                                btnBlocked.setVisibility(View.INVISIBLE);
                                                                btnNotAcceptingFriends.setVisibility(View.VISIBLE);
                                                                return;
                                                            }

                                                            // If the user has no special properties, show the
                                                            // send request button
                                                            btnRemoveRequest.setVisibility(View.INVISIBLE);
                                                            btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                                            btnHaveARequest.setVisibility(View.INVISIBLE);
                                                            btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                                            btnBlockedBy.setVisibility(View.INVISIBLE);
                                                            btnBlocked.setVisibility(View.INVISIBLE);
                                                            btnSendRequest.setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                        else {
                                            btnRemoveRequest.setVisibility(View.INVISIBLE);
                                            btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                            btnHaveARequest.setVisibility(View.INVISIBLE);
                                            btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                            btnBlocked.setVisibility(View.INVISIBLE);
                                            btnBlockedBy.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                            else {
                                btnRemoveRequest.setVisibility(View.INVISIBLE);
                                btnAlreadyFriends.setVisibility(View.INVISIBLE);
                                btnHaveARequest.setVisibility(View.INVISIBLE);
                                btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                btnBlockedBy.setVisibility(View.INVISIBLE);
                                btnBlocked.setVisibility(View.VISIBLE);
                            }
                        }
                    });


                }
            });

            // Add an onClick listener to the send request button to send a friend request
            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    // When clicked, add the request to the queue and the
                    // user's requests
                    friending = true;

                    // Add the requests to the user's requests
                    ParseRelation<ParseUser> sent_requests = curUser.getRelation("sent_requests");
                    sent_requests.add(friend);
                    curUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Unable to save user to sent requests", e);
                            }
                        }
                    });

                    // Add the request to the queue
                    Friend_queue queue = new Friend_queue();
                    queue.setUser(friend);
                    queue.setFriend(curUser);
                    queue.setMode("request");
                    queue.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Unable to request save to queue", e);
                            }
                            else {
                                btnSendRequest.setVisibility(View.INVISIBLE);
                                btnRemoveRequest.setVisibility(View.VISIBLE);
                            }
                            friending = false;
                        }
                    });


                }
            });


            // Add an onClick listener to the remove friend request button to
            // remove the request from the queue
            btnRemoveRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    friending = true;

                    // Find request in set_requests and remove it
                    ParseRelation<ParseUser> sent_requests = curUser.getRelation("sent_requests");
                    sent_requests.remove(friend);
                    curUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Unable to remove user from sent requests", e);
                            }
                        }
                    });

                    // Find the request in the queue and remove it
                    ParseQuery<Friend_queue> q = new ParseQuery<Friend_queue>("Friend_queue");
                    q.whereEqualTo("user", friend);
                    q.whereEqualTo("friend", curUser);
                    q.whereEqualTo("mode", "request");
                    q.findInBackground(new FindCallback<Friend_queue>() {
                        @Override
                        public void done(List<Friend_queue> objects, ParseException e) {
                            if (objects.size() == 0 || e != null) {
                                Log.e(TAG, "Unable to remove request from queue", e);
                                friending = false;
                                return;
                            }

                            objects.get(0).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Error removing request from queue", e);
                                    }
                                    else {
                                        Log.i(TAG, "Removed request from queue");
                                        btnRemoveRequest.setVisibility(View.INVISIBLE);
                                        btnNotAcceptingFriends.setVisibility(View.INVISIBLE);
                                        btnSendRequest.setVisibility(View.VISIBLE);
                                    }
                                    friending = false;
                                }
                            });
                        }
                    });
                }
            });


        }
    }
}
