package com.vysh.subairoma.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.models.ImportantContactsModel;
import com.wordpress.priyankvex.smarttextview.SmartTextView;

import java.util.ArrayList;

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
        String title = contacts.get(position).getTitle();
        if (!title.equalsIgnoreCase("null")) {
            holder.tvTitle.setText(title);
        } else holder.tvTitle.setVisibility(View.GONE);

        String description = contacts.get(position).getDescription();
        if (!description.equalsIgnoreCase("null")) {
            holder.tvDescription.setText(description);
        } else holder.tvDescription.setVisibility(View.GONE);

        String address = contacts.get(position).getAddress();
        if (!address.equalsIgnoreCase("null")) {
            holder.tvAddress.setText(address);
        } else holder.tvAddress.setVisibility(View.GONE);

        String email = contacts.get(position).getEmail();
        if (!email.equalsIgnoreCase("null")) {
            holder.tvEmail.setText(email);
        } else holder.tvEmail.setVisibility(View.GONE);

        String website = contacts.get(position).getWebsite();
        if (!website.equalsIgnoreCase("null")) {
            holder.tvWebsite.setText(website);
        } else holder.tvWebsite.setVisibility(View.GONE);

        String phone = contacts.get(position).getPhone();
        if (!phone.equalsIgnoreCase("null")) {
            holder.tvPhone.setText(phone);
        } else holder.tvPhone.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        SmartTextView tvDescription, tvAddress, tvPhone, tvWebsite, tvEmail;
        TextView tvTitle;

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
