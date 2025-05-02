package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    private final List<String> navItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public NavAdapter(Context context, List<String> navItems, OnItemClickListener listener) {
        this.navItems = navItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NavAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_nav_button, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull NavAdapter.ViewHolder holder, int position) {
        String item = navItems.get(position);
        holder.textView.setText(navItems.get(position));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return navItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.navItemText);
        }
    }

}
