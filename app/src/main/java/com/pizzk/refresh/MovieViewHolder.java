package com.pizzk.refresh;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MovieViewHolder extends RecyclerView.ViewHolder {
    private TextView tvName;

    public MovieViewHolder(View view) {
        super(view);
        tvName = view.findViewById(R.id.tvName);
    }

    public void setName(String name) {
        tvName.setText(name);
    }
}
