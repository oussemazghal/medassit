package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import androidx.recyclerview.widget.RecyclerView;



public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final DatabaseHelper dbHelper;
    private final Runnable refreshCallback;

    public ContactAdapter(Context context, Cursor cursor, DatabaseHelper dbHelper, Runnable refreshCallback) {
        this.context = context;
        this.cursor = cursor;
        this.dbHelper = dbHelper;
        this.refreshCallback = refreshCallback;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContact, icEdit, icDelete;
        TextView tvName, tvPhone;

        public ContactViewHolder(View itemView) {
            super(itemView);
            imgContact = itemView.findViewById(R.id.img_contact);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            icEdit = itemView.findViewById(R.id.ic_e);
            icDelete = itemView.findViewById(R.id.iconDelete);
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));

            holder.tvName.setText(name);
            holder.tvPhone.setText(phone);

            // Chargement de l'image avec Glide
            if (!imageUri.isEmpty()) {
                Glide.with(context)
                        .load(Uri.parse(imageUri))
                        .placeholder(R.drawable.placeholder)
                        .into(holder.imgContact);
            } else {
                holder.imgContact.setImageResource(R.drawable.placeholder);
            }

            // Action sur clic téléphone
            holder.tvPhone.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                context.startActivity(intent);
            });

            // Suppression
            holder.icDelete.setOnClickListener(v -> {
                boolean deleted = dbHelper.deleteEmergencyContact(id);
                if (deleted) {
                    Toast.makeText(context, "Contact supprimé", Toast.LENGTH_SHORT).show();
                    refreshCallback.run();
                } else {
                    Toast.makeText(context, "Erreur de suppression", Toast.LENGTH_SHORT).show();
                }
            });

            // Édition (à améliorer si tu veux pré-remplir les champs)
            holder.icEdit.setOnClickListener(v -> {
                if (context instanceof EmergencyActivity) {
                    ((EmergencyActivity) context).showAddContactDialog(id, name, phone, imageUri);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }
}
