package com.baato.baatoandroiddemo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.baato.baatolibrary.models.SearchDataModel;
import com.example.baatoandroiddemo.R;
import com.baato.baatolibrary.models.Place;
import com.baato.baatolibrary.models.PlaceAPIResponse;
//import com.baato.baatolibrary.models.SearchDataModel;
import com.baato.baatolibrary.services.BaatoPlace;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    List<SearchDataModel> places;
    String query = "";
    private Context context;

    public SearchAdapter(List<SearchDataModel> places, Context context) {
        this.places = places;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_result_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchDataModel place = places.get(position);
        try {
            holder.txtAddress.setText(place.getAddress());
            holder.txtName.setText(place.getName());
        } catch (Exception e) {
        }
        holder.itemView.setOnClickListener(view -> {
            getPlaceDetails(place.getPlaceId());
        });

    }

    private void getPlaceDetails(int placeId) {
        new BaatoPlace(context)
                .setAccessToken(context.getString(R.string.baato_access_token))
                .setPlaceId(placeId)
                .withListener(new BaatoPlace.BaatoPlaceListener() {
                    @Override
                    public void onSuccess(PlaceAPIResponse place) {
                        Log.d(TAG, "onSuccess:place " + place.getData().size());
                        if (!place.getData().isEmpty())
                            showDialog(place.getData().get(0));
                        else Toast.makeText(context, "No details found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Throwable error) {
                        Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .doRequest();
    }

    private void showDialog(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Place Details!")
                .setMessage(place.toString())
                .show();
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtAddress;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtAddress = itemView.findViewById(R.id.txtAddress);
        }
    }

}
