package com.example.sehaplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdabterNurse extends RecyclerView.Adapter<AdabterNurse.NurseViewHolder> {

    Context context;
    ArrayList<Nurse> nurseArrayList;

    public AdabterNurse(Context context, ArrayList<Nurse> nurseArrayList) {
        this.context = context;
        this.nurseArrayList = nurseArrayList;
    }

    @NonNull
    @Override
    public  AdabterNurse.NurseViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View v= LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new NurseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdabterNurse.NurseViewHolder holder, int position){

        Nurse nurse =nurseArrayList.get(position);

        holder.firstName.setText(nurse.firstName);
        holder.speciality.setText(nurse.speciality);
        holder.price.setText(String.valueOf(nurse.price));
        holder.rating.setText(String.valueOf(nurse.rating));

    }

    @Override
    public  int getItemCount(){
        return nurseArrayList.size();
    }

    public static class NurseViewHolder extends RecyclerView.ViewHolder{

        TextView firstName , lastName , rating , speciality , price;
        public NurseViewHolder(@NonNull View itemView) {
            super(itemView);
            firstName= itemView.findViewById(R.id.nurseName);
            rating =itemView.findViewById(R.id.nurseRating);
            speciality = itemView.findViewById(R.id.nurseSpeciality);
            price = itemView.findViewById(R.id.nursePrice);

        }
    }

}
