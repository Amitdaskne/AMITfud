package com.amit.fud.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.fud.R;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Map;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private List<Map<String, Object>> restaurantList;

    public RestaurantAdapter(List<Map<String, Object>> restaurantList) {
        this.restaurantList = restaurantList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> res = restaurantList.get(position);
        holder.tvName.setText((String) res.get("name"));
        holder.tvAddress.setText((String) res.get("address"));
        
        Glide.with(holder.itemView.getContext())
             .load((String) res.get("image"))
             .into(holder.ivImage);
    }

    @Override public int getItemCount() { return restaurantList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        ImageView ivImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvResName);
            tvAddress = itemView.findViewById(R.id.tvResAddress);
            ivImage = itemView.findViewById(R.id.ivResImage);
        }
    }
}
