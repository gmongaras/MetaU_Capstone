package com.example.metau_capstone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Fragments.HomeFragment_countdown;
import Fragments.ProfileDetailFragment;
import Fragments.ProfileFragment;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    // List in the recycler view
    List<Fortune> fortunes;

    // Fragment manager for the home fragment
    FragmentManager fragmentManager;

    Context context;

    // Constructor to create the adapter with context and a list
    public ProfileAdapter(List<Fortune> fortunes, Context context, FragmentManager manager) {
        this.fortunes = fortunes;
        this.context = context;
        fragmentManager = manager;
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

        // Put an onClick listener on the view to go into the detailed view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup the fragment switch
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // Create the fragment with paramters
                ProfileDetailFragment fragmentProfileDetail = ProfileDetailFragment.newInstance(fortune);

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentProfileDetail);
                ft.commit();
            }
        });
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Get the element contents
            tvDate = itemView.findViewById(R.id.tvDate);
            tvFortune = itemView.findViewById(R.id.tvFortune);
        }

        // Given a fortune, bind data to this object
        public void bind(Fortune fortune) {
            tvDate.setText(fortune.getCreatedAt().toString());

            String message = fortune.getMessage().toString();
            if (message.length() > 50) {
                message = message.substring(0, 50) + "...";
            }
            tvFortune.setText(message);
        }
    }
}
