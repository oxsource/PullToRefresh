package com.pizzk.refresh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MovieListAdapter extends RecyclerView.Adapter<MovieViewHolder> {
    private int count = 0;

    public MovieListAdapter() {
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_list, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.setName(String.format("%02d超时空同居", position));
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setItemCount(int num) {
        this.count = num;
    }
}
