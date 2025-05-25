package com.example.sehaplus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class AdabterNurse extends RecyclerView.Adapter<AdabterNurse.NurseViewHolder> {

    Context context;
    ArrayList<Nurse> nurseArrayList;

    public AdabterNurse(Context context, ArrayList<Nurse> nurseArrayList) {
        this.context = context;
        this.nurseArrayList = nurseArrayList;
    }

    @NonNull
    @Override
    public NurseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new NurseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NurseViewHolder holder, int position) {
        Nurse nurse = nurseArrayList.get(position);

        holder.firstName.setText(nurse.getFirstName());
        holder.speciality.setText(nurse.getSpeciality());
        holder.price.setText(nurse.getPrice() + " DA");
        holder.rating.setText(nurse.getRating() + " ★");

        holder.requestServiceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, LocationSelectionActivity.class);
            intent.putExtra("nurseId", nurse.getId());
            context.startActivity(intent);
        });

        holder.viewProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, NurseProfileActivity.class);
            intent.putExtra("nurseId", nurse.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return nurseArrayList.size();
    }

    public static class NurseViewHolder extends RecyclerView.ViewHolder {
        TextView firstName, rating, speciality, price;
        Button requestServiceBtn, viewProfileBtn;

        public NurseViewHolder(@NonNull View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.nurseName);
            rating = itemView.findViewById(R.id.nurseRating);
            speciality = itemView.findViewById(R.id.nurseSpeciality);
            price = itemView.findViewById(R.id.nursePrice);
            requestServiceBtn = itemView.findViewById(R.id.requestServiceBtn); // ✅ هذا الزر
            viewProfileBtn = itemView.findViewById(R.id.viewProfileBtn);       // ✅ وهذا الزر
        }
    }

    public void sortByRatingDescending() {
        Collections.sort(nurseArrayList, (n1, n2) -> n2.getRating().compareTo(n1.getRating()));
        notifyDataSetChanged();
    }

    public void sortByNameAscending() {
        Collections.sort(nurseArrayList, (n1, n2) -> n1.getFirstName().compareToIgnoreCase(n2.getFirstName()));
        notifyDataSetChanged();
    }
}
