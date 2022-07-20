package com.example.metau_capstone.Friends;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.example.metau_capstone.translationManager;
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

import Fragments.Main.ProfileFragment;

/**
 ** Adapter used to manage the Friends Search Fragment recycler view
 */
public class FriendsSearchAdapter extends RecyclerView.Adapter<FriendsSearchAdapter.ViewHolder> {
    private static final String TAG = "FriendsSearchAdapter";

    // List in the recycler view
    List<ParseUser> users;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    // User info
    List<ParseUser> friends;
    List<ParseUser> requests;
    List<ParseUser> sent;
    List<ParseUser> blocked;

    // Colors that may be user
    int colorSecondary;
    int colorPrimary;
    int tertiaryColor;
    int colorRed;

    translationManager manager;

    /**
     * Initialize the adapter
     * @param users A list of ParseUser objects which we want to initialize the
     *                recycler view with.
     * @param context Context from the class using this adapter
     * @param fragmentManager Fragment manager using this adapter to handle back presses
     */
    public FriendsSearchAdapter(List<ParseUser> users, Context context, FragmentManager fragmentManager) {
        this.users = users;
        this.fragmentManager = fragmentManager;
        this.context = context;
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));
    }

    @NonNull
    @Override
    public FriendsSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_search, parent, false);

        // Get the theme colors to use later
        colorPrimary = androidx.constraintlayout.widget.R.attr.textFillColor;
        colorSecondary = androidx.constraintlayout.widget.R.attr.textColorSearchUrl;
        tertiaryColor = androidx.constraintlayout.widget.R.attr.textOutlineColor;
        colorRed = com.google.android.material.R.attr.colorError;

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

        // Get the mode of the current user
        // 0 - Current user
        // 1 - Friend
        // 2 - Other user
        // 3 - Other user blocked by logged in user
        // 4 - Logged in user blocked by other user
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

                // Create the fragment with parameters
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

    // Used to set dat ain the background
    public void setFriends(List<ParseUser> friends) {
        this.friends = friends;
    }
    public void setRequests(List<ParseUser> requests) {
        this.requests = requests;
    }
    public void setSent(List<ParseUser> sent) {
        this.sent = sent;
    }
    public void setBlocked(List<ParseUser> blocked) {
        this.blocked = blocked;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Elements in the item view
        ImageView ivFriend_search;
        TextView tvFriendUsername_search;
        TextView tvFriendFortuneCt_search;
        TextView tvNumForts;
        Button btnState;

        // The current user
        ParseUser curUser;

        // Is the user friending someone?
        boolean friending;

        // What mode should the profile be put in?
        // 0 - Current user
        // 1 - Friend
        // 2 - Other user
        // 3 - Other user blocked by logged in user
        // 4 - Logged in user blocked by other user
        public int mode;

        // Has a button been displayed yet?
        boolean displayed;

        // click listeners for the button
        View.OnClickListener sendRequest;
        View.OnClickListener removeRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the elements
            ivFriend_search = itemView.findViewById(R.id.ivFriend_search);
            tvFriendUsername_search = itemView.findViewById(R.id.tvFriendUsername_search);
            tvFriendFortuneCt_search = itemView.findViewById(R.id.tvFriendFortuneCt_search);
            btnState = itemView.findViewById(R.id.btnState);
            tvNumForts = itemView.findViewById(R.id.tvNumForts);
            friending = false;

            // Get the current user
            curUser = ParseUser.getCurrentUser();

            // Translate the text
            manager.addText(tvNumForts, R.string.numForts, context);
        }

        // Given a Friend (ParseUser), bind data to this object
        public void bind(ParseUser friend) {
            displayed = false;

            // Make all buttons invisible to start
            btnState.setVisibility(View.INVISIBLE);

            // Set the username
            tvFriendUsername_search.setText(friend.getUsername());

            // Store the user image
            ParseFile pic = friend.getParseFile("profilePic");
            if (pic == null) {
                ivFriend_search.setImageResource(R.drawable.default_pfp);
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
                    manager.addText(tvFriendFortuneCt_search, String.valueOf(objects.size()));
                }
            });



            // OnClick listeners for the button -->


            // onClick listener to remove a sent request
            removeRequest = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friending == true) {
                        return;
                    }

                    friending = true;

                    // Find request in sent_requests and remove it
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
                                // If the request is not in the queue, the other user must
                                // have it, so send a request to remove it

                                // Create the new request
                                Friend_queue item = new Friend_queue();
                                item.setFriend(ParseUser.getCurrentUser());
                                item.setMode("remove_request");
                                item.setUser(friend);

                                // Save the request
                                item.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "Unable to save remove request to queue", e);
                                        }
                                        else {
                                            displayButton("Send Friend Request", colorPrimary, colorSecondary, "send");
                                        }

                                        friending = false;
                                    }
                                });

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
                                        displayButton("Send Friend Request", colorPrimary, colorSecondary, "send");
                                    }
                                    friending = false;
                                }
                            });
                        }
                    });
                }
            };

            // Listener to send a request
            sendRequest = new View.OnClickListener() {
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
                                displayButton("Remove Friend Request", colorRed, colorPrimary, "remove");
                            }
                            friending = false;
                        }
                    });


                }
            };



            // Is the other user a friend of this user?
            for (ParseUser f : friends) {
                if (Objects.equals(f.getObjectId(), friend.getObjectId())) {
                    displayButton("Already Friends!", tertiaryColor, colorSecondary, null);
                    displayed = true;
                    break;
                }
            }

            // Is the other user blocked by the current user?
            if (displayed == false) {
                for (ParseUser b : blocked) {
                    if (Objects.equals(b.getObjectId(), friend.getObjectId())) {
                        displayButton("You blocked this user", colorRed, colorPrimary, null);
                        displayed = true;
                        break;
                    }
                }
            }

            // Did this user send a request to the other user?
            if (!displayed) {
                for (ParseUser s : sent) {
                    if (Objects.equals(s.getObjectId(), friend.getObjectId())) {
                        displayButton("Remove Friend Request", colorRed, colorPrimary, "remove");
                        displayed = true;
                        break;
                    }
                }
            }

            // Does this user have a request from the other user?
            if (!displayed) {
                for (ParseUser r : sent) {
                    if (Objects.equals(r.getObjectId(), friend.getObjectId())) {
                        displayButton("Currently have a request", tertiaryColor, colorSecondary, null);
                        displayed = true;
                        break;
                    }
                }
            }

            // Is the other user accepting friend requests?
            if (!displayed) {
                if (friend.getBoolean("friendable") == false) {
                    displayButton("User not currently\naccepting friend requests", tertiaryColor, colorSecondary, null);
                    displayed = true;
                }
            }

            // Is the current user blocked by the other user?
            if (!displayed) {
                ParseRelation<ParseUser> r = friend.getRelation("Blocked");
                ParseQuery<ParseUser> blockedQuery = r.getQuery();
                blockedQuery.whereEqualTo("objectId", curUser.getObjectId());
                blockedQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        // If the number of objects is not 0 and there is no error,
                        // the current user is blocked by the other user
                        if (e == null && objects.size() > 0) {
                            displayButton("This user has blocked you", colorRed, colorPrimary, null);
                            displayed = true;
                            return;
                        }

                        // Default to sending a request
                        displayButton("Send Friend Request", colorPrimary, colorSecondary, "send");
                        displayed = true;
                    }
                });
            }


        }


        /**
         * Given some text and a color, display the button with that text and background color
         * @param text The text to show in the button
         * @param colorId The id of the button color
         * @param textColorId The id of the button text color
         * @param mode The mode to add an onClick listener to the button:
         *             null: Don't add a listener
         *             "send": When the button is clicked, send a friend request
         *             "remove": When the button is clicked, remove a friend request
         */
        private void displayButton(String text, int colorId, int textColorId, @Nullable String mode) {
            // Get the color
            int color = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                final TypedValue value = new TypedValue ();
                context.getTheme().resolveAttribute(colorId, value, true);
                color = value.data;
            }

            // Get the text color
            int textColor = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                final TypedValue value = new TypedValue ();
                context.getTheme().resolveAttribute(textColorId, value, true);
                textColor = value.data;
            }

            // Show the button
            manager.addText(btnState, text);
            btnState.setBackgroundColor(color);
            btnState.setTextColor(textColor);
            btnState.setVisibility(View.VISIBLE);

            // If the mode is not null, add an onclick listener to the button
            if (mode == null) {
                btnState.setOnClickListener(null);
            }
            else {
                if (Objects.equals(mode, "send")) {
                    btnState.setOnClickListener(sendRequest);
                }
                else if (Objects.equals(mode, "remove")) {
                    btnState.setOnClickListener(removeRequest);
                }
            }
        }
    }
}
