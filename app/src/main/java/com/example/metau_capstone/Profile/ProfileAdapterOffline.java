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
import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineHelpers;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import Fragments.Main.ProfileDetailFragment;
import Fragments.Main.ProfileDetailOfflineFragment;

/**
 ** Adapter used to manage fortune loading for all Fortune in the Profile Fragment
 ** when the user is offline
 */
public class ProfileAdapterOffline extends RecyclerView.Adapter<ProfileAdapterOffline.ViewHolder> {

    private final static String TAG = "ProfileAdapterOffline";

    // List in the recycler view
    public List<FortuneDB> fortunes;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    // User to load the profile for
    ParseUser user;

    // Used to format dates
    dateFormatter df;

    // Used to work with offline data
    offlineHelpers h;

    /**
     * Initialize the adapter
     * @param fortunes A list of FortuneDB objects to initialize the list with
     * @param user The user to load fortunes for
     * @param context What object is using this adapter?
     * @param manager The manager which manages the fragments this adapter is in
     * @param mode The user mode to load the fortunes ^
     */
    public ProfileAdapterOffline(List<FortuneDB> fortunes, ParseUser user, Context context, FragmentManager manager, int mode) {
        this.fortunes = fortunes;
        this.user = user;
        this.context = context;
        fragmentManager = manager;

        df = new dateFormatter();
        h = new offlineHelpers();
    }

    @NonNull
    @Override
    public ProfileAdapterOffline.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view and inflate it
        View view = LayoutInflater.from(context).inflate(R.layout.item_fortune, parent, false);

        // Return the view
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapterOffline.ViewHolder holder, int position) {
        // Get the item at the given position
        FortuneDB fortune = fortunes.get(position);

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

        // Fortune in this view
        FortuneDB fortune;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the element contents
            tvDate = itemView.findViewById(R.id.tvDate);
            tvFortune = itemView.findViewById(R.id.tvFortune);
            ivLiked = itemView.findViewById(R.id.ivLiked);

            // Go into the detailed view when a fortune is clicked
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Setup the fragment switch
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    // Create the fragment with paramters
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    ProfileDetailOfflineFragment fragmentProfileDetail = ProfileDetailOfflineFragment.newInstance(fortune, user, fortunes);

                    // Change the fragment
                    ft.replace(R.id.flContainer, fragmentProfileDetail);
                    ft.commit();
                }
            });
        }

        // Given a fortune, bind data to this object
        public void bind(FortuneDB fortune) {
            this.fortune = fortune;

            // Get the date in the proper form and set it
            tvDate.setText(df.toMonthDay(h.toDate(fortune.date)));

            // Get the message
            String message = fortune.message;
            if (message.length() > 50) {
                message = message.substring(0, 50) + "...";
            }
            tvFortune.setText(message);

            // Is the fortune liked?
            if (fortune.liked) {
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
        }


    }
}
