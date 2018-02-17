package com.vysh.subairoma.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.DatabaseTables;
import com.vysh.subairoma.models.ImportantContactsModel;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Vishal on 2/17/2018.
 */

public class ImportantContactsAdatper extends RecyclerView.Adapter<ImportantContactsAdatper.ContactHolder> {

    ArrayList<ImportantContactsModel> contacts;

    public ImportantContactsAdatper(ArrayList<ImportantContactsModel> contacts) {
        this.contacts = contacts;
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_important_contacts, parent, false));
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.tvTitle.setText(contacts.get(position).getTitle());
        holder.tvDescription.setText(contacts.get(position).getDescription());
        holder.tvAddress.setText(contacts.get(position).getAddress());
        holder.tvEmail.setText(contacts.get(position).getEmail());
        holder.tvWebsite.setText(contacts.get(position).getWebsite());
        holder.tvPhone.setText(contacts.get(position).getPhone());
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvAddress, tvPhone, tvWebsite, tvEmail;

        public ContactHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvWebsite = itemView.findViewById(R.id.tvWebsite);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}
