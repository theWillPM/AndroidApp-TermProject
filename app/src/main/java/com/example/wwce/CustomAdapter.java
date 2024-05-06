package com.example.wwce;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Class necessary to handle my list items and paint the red rectangle on removal (swipe)
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private List<ItemRow> data;
    private RecyclerView recyclerView;
    Context context;

    // Static variable to hold the default background color
    private static int defaultBackgroundColor;

    public CustomAdapter(Context context, List<ItemRow> data, RecyclerView recyclerView) {
        this.data = data;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);

        // Initialize the default background color if not already initialized
        if (defaultBackgroundColor == 0) {
            // Get the root element from the parent layout
            View rootElement = parent.getRootView().findViewById(R.id.root_layout);
            if (rootElement != null) {
                // Get the background color of the root element
                Drawable backgroundDrawable = rootElement.getBackground();
                if (backgroundDrawable instanceof ColorDrawable) {
                    defaultBackgroundColor = ContextCompat.getColor(context, R.color.white);
                }
            }
        }

        // Return a new ViewHolder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemRow item = data.get(position);
        holder.nameTextView.setText(item.getName());
        holder.value1TextView.setText(item.getValue1());
        holder.value2TextView.setText(String.valueOf(item.getValue2()));
        holder.name2TextView.setText(item.getName2());

        // Reset the background color to default
        holder.resetBackgroundColor();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // Not used for removal
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        // Get the ViewHolder at the specified position
        ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            // Set background color to red
            viewHolder.setBackgroundColor(Color.RED);
        }

        // Remove the item from the dataset
        data.remove(position);

        // Notify the adapter that the item has been removed
        notifyItemRemoved(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView value1TextView;
        TextView value2TextView;
        TextView name2TextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            value1TextView = itemView.findViewById(R.id.value1TextView);
            value2TextView = itemView.findViewById(R.id.value2TextView);
            name2TextView = itemView.findViewById(R.id.valueTextView);
        }

        // Method to set the background color of the itemView
        public void setBackgroundColor(int color) {
            itemView.setBackgroundColor(color);
        }

        // Method to reset the background color to default
        public void resetBackgroundColor() {
            itemView.setBackgroundColor(defaultBackgroundColor);
        }
    }
}
