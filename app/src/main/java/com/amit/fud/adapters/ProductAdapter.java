package com.amit.fud.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.fud.R;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Map<String, Object>> productList;

    public ProductAdapter(List<Map<String, Object>> productList) {
        this.productList = productList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> prod = productList.get(position);
        holder.tvName.setText((String) prod.get("name"));
        
        Object priceObj = prod.get("price");
        Object discountObj = prod.get("discount");
        
        holder.tvPrice.setText("₹" + (priceObj != null ? priceObj.toString() : "0"));
        
        if (discountObj != null && !discountObj.toString().equals("0")) {
            holder.tvDiscount.setText(discountObj.toString() + "% OFF");
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }
        
        Glide.with(holder.itemView.getContext())
             .load((String) prod.get("image"))
             .into(holder.ivImage);

        holder.btnAdd.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), holder.tvName.getText() + " added to cart!", Toast.LENGTH_SHORT).show();
            // Yahan hum cart me add karne ka logic baad me jodenge
        });
    }

    @Override public int getItemCount() { return productList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDiscount;
        ImageView ivImage;
        Button btnAdd;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvDiscount = itemView.findViewById(R.id.tvProductDiscount);
            ivImage = itemView.findViewById(R.id.ivProduct);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
