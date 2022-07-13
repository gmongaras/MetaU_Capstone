package com.example.metau_capstone.Profile;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.metau_capstone.dateFormatter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import Fragments.Main.ProfileDetailFragment;

/**
 ** Adapter used to manage fortune loading for all views in the Profile Fragment
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private final static String TAG = "ProfileAdapter";

    // List in the recycler view
    List<Fortune> fortunes;

    // List of liked fortune IDs
    List<String> likedFortunes;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    // User to load the profile for
    ParseUser user;

    // What mode should the profile be put in?
    // 0 - Current user
    // 1 - Friend
    // 2 - Other user
    // 3 - Other user blocked by logged in user
    // 4 - Logged in user blocked by other user
    int mode;

    // Used to convert the date
    dateFormatter df = new dateFormatter();

    /**
     * Initialize the adapter
     * @param fortunes A list of fortunes to initialize the list with
     * @param user The user to load fortunes for
     * @param context What object is using this adapter?
     * @param manager The manager which manages the fragments this adapter is in
     * @param mode The user mode to load the fortunes ^
     */
    public ProfileAdapter(List<Fortune> fortunes, ParseUser user, Context context, FragmentManager manager, int mode) {
        this.fortunes = fortunes;
        this.user = user;
        this.context = context;
        fragmentManager = manager;
        this.mode = mode;

        // Get all liked fortunes from this user
        ParseRelation<Fortune> likedRel = ParseUser.getCurrentUser().getRelation("liked");
        ParseQuery<Fortune> query = likedRel.getQuery();
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> objects, ParseException e) {
                if (objects == null) {
                    return;
                }
                likedFortunes = new ArrayList<>();
                for (Fortune f : objects) {
                    likedFortunes.add(f.getObjectId());
                }
            }
        });
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_fortune, parent, false);

        // Return the view
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        // Get the item at the given position
        Fortune fortune = fortunes.get(position);

        // Bind the post to the view holder
        holder.bind(fortune);
    }

    @Override
    public int getItemCount() {
        return fortunes.size();
    }

    // The view holder to hold each post
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Elements in the item view
        TextView tvDate;
        TextView tvFortune;
        ImageView ivLiked;

        // Used to detect double tapping
        GestureDetector gestureDetector;

        // Fortune in this view
        Fortune fortune;

        // Is the fortune liked?
        boolean liked;

        // Is the fortune changing states?
        boolean changing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the element contents
            tvDate = itemView.findViewById(R.id.tvDate);
            tvFortune = itemView.findViewById(R.id.tvFortune);
            ivLiked = itemView.findViewById(R.id.ivLiked);

            // Create a new gesture detector to handle double taps
            gestureDetector = new GestureDetector(itemView.getContext(), new GestureListener());

            // Handle double taps on the entire view
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    itemView.performClick();
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        // Given a fortune, bind data to this object
        public void bind(Fortune fortune) {
            this.fortune = fortune;
            changing = false;

            // Get the date in the proper form and set it
            tvDate.setText(df.toMonthDay(fortune.getCreatedAt()));

            String message = fortune.getMessage();
            if (message.length() > 50) {
                message = message.substring(0, 50) + "...";
            }
            tvFortune.setText(message);


            // If the liked list is null, get the list
            if (likedFortunes == null) {
                changing = true;
                // Get the user's liked fortunes
                ParseRelation<Fortune> likedRel = ParseUser.getCurrentUser().getRelation("liked");
                ParseQuery<Fortune> query = likedRel.getQuery();
                query.whereEqualTo("objectId", fortune.getObjectId());
                query.findInBackground(new FindCallback<Fortune>() {
                    @Override
                    public void done(List<Fortune> objects, ParseException e) {
                        // If the objects returned is 0, the fortune is not liked
                        if (objects.size() == 0) {
                            liked = false;
                        } else {
                            liked = true;
                        }

                        // Change the drawable based on the liked state
                        if (liked) {
                            Glide.with(itemView.getContext())
                                    .load(R.drawable.like_filled)
                                    .circleCrop()
                                    .into(ivLiked);
                        } else {
                            Glide.with(itemView.getContext())
                                    .load(R.drawable.like)
                                    .circleCrop()
                                    .into(ivLiked);
                        }

                        changing = false;
                    }
                });
            }

            // If the liked list is loaded, load in the image
            else {

                changing = true;

                // Does the user like this fortune
                if (likedFortunes.contains(fortune.getObjectId())) {
                    liked = true;
                } else {
                    liked = false;
                }

                // Change the drawable based on the liked state
                if (liked) {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.like_filled)
                            .circleCrop()
                            .into(ivLiked);
                } else {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.like)
                            .circleCrop()
                            .into(ivLiked);
                }

                changing = false;
            }
        }

        // Clicks on the view
        private class GestureListener extends GestureDetector.SimpleOnGestureListener {
            // Detect clicks on the view
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            // Like the fortune if a double tap occurs
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // If the fortune is already being liked, do nothing
                if (changing) {
                    return true;
                }

                changing = true;
                // If the item is already liked, unlike it
                if (liked) {
                    // Update the backend
                    ParseRelation<Fortune> likedRel = user.getRelation("liked");
                    likedRel.remove(fortune);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving unlike", e);
                            }
                        }
                    });
                    fortune.setLikeCt(fortune.getLikeCt()-1);
                    fortune.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving like count", e);
                            }
                            changing = false;
                        }
                    });

                    // Update the stored states
                    liked = false;
                    likedFortunes.remove(fortune.getObjectId());

                    // Update the image;
                    Glide.with(itemView.getContext())
                            .load(R.drawable.like)
                            .circleCrop()
                            .into(ivLiked);
                }
                // If the item is not liked, like it
                else {
                    // Update the backend
                    ParseRelation<Fortune> likedRel = user.getRelation("liked");
                    likedRel.add(fortune);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving like", e);
                            }
                        }
                    });
                    fortune.setLikeCt(fortune.getLikeCt()+1);
                    fortune.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving like count", e);
                            }
                            changing = false;
                        }
                    });

                    // Update the stored states
                    liked = true;
                    likedFortunes.add(fortune.getObjectId());

                    // Update the image;
                    Glide.with(itemView.getContext())
                            .load(R.drawable.like_filled)
                            .circleCrop()
                            .into(ivLiked);
                }


                return true;
            }

            // Go into the detailed view when a single tap occurs
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Setup the fragment switch
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ProfileDetailFragment fragmentProfileDetail = ProfileDetailFragment.newInstance(fortune, user, mode);

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentProfileDetail);
                ft.commit();

                return super.onSingleTapConfirmed(e);
            }
        }
    }
}
