package com.baato.baatoandroiddemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baato.baatoandroiddemo.activities.MonochromeMapStyleActivity;
import com.baato.baatoandroiddemo.activities.NavigationActivity;
import com.baato.baatoandroiddemo.activities.RetroMapStyleActivity;
import com.baato.baatoandroiddemo.activities.SearchActivity;
import com.baato.baatoandroiddemo.models.HomeMenu;
import com.example.baatoandroiddemo.R;
import com.baato.baatoandroiddemo.activities.LocationPickerActivity;

import java.util.List;


public class HomeMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<HomeMenu> places;
    private Context context;

    public HomeMenuAdapter(List<HomeMenu> places, Context context) {
        this.places = places;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case HomeMenu.SERVICE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_service_item, parent, false);
                return new ServiceViewHolder(view);
            case HomeMenu.MAP_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_map_item, parent, false);
                return new MapViewHolder(view);
            case HomeMenu.TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_text, parent, false);
                return new TextViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        switch (places.get(position).type) {
            case 0:
                return HomeMenu.SERVICE_TYPE;
            case 1:
                return HomeMenu.MAP_TYPE;
            case 2:
                return HomeMenu.TEXT_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeMenu place = places.get(position);
        try {
            switch (place.type) {
                case HomeMenu.SERVICE_TYPE:
                    ((ServiceViewHolder) holder).itemIcon.setImageResource(place.icon);
                    ((ServiceViewHolder) holder).txtItem.setText(place.title);
                    break;
                case HomeMenu.MAP_TYPE:
                    ((MapViewHolder) holder).itemIcon.setImageResource(place.icon);
                    ((MapViewHolder) holder).txtItem.setText(place.title);
                    break;
                case HomeMenu.TEXT_TYPE:
                    ((TextViewHolder) holder).txtItem.setText(place.title);
                    break;
            }
        } catch (Exception e) {
        }
        holder.itemView.setOnClickListener(view -> {
            openRespectiveActivity(position);
        });

    }

    private void openRespectiveActivity(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(context, SearchActivity.class);

                break;
            case 1:
                intent = new Intent(context, LocationPickerActivity.class);
                break;
            case 2:
                intent = new Intent(context, NavigationActivity.class);
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                intent = new Intent(context, MonochromeMapStyleActivity.class);
                break;
            case 7:
                intent = new Intent(context, RetroMapStyleActivity.class);
                break;
            default:
                break;
        }
        if (intent != null)
            context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return places.size();
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView txtItem;
        ImageView itemIcon;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            txtItem = itemView.findViewById(R.id.txtItem);
        }
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {
        TextView txtItem;
        ImageView itemIcon;

        public MapViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            txtItem = itemView.findViewById(R.id.txtItem);
        }
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        TextView txtItem;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItem = itemView.findViewById(R.id.txtTitle);
        }
    }

}
