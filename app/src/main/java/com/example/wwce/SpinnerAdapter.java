package com.example.wwce;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Class to handle the dropdown items
 */
public class SpinnerAdapter extends ArrayAdapter<String> {
    private Context mContext;

    public SpinnerAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textView);
        ImageView flagImageView = convertView.findViewById(R.id.imageViewFlag);

        String itemText = getItem(position);
        // Set the text of the TextView
        textView.setText(itemText);

        // Set the flag image based on the item text (replace with your logic to get flag image)
        int flagResId = getFlagResourceForItem(itemText);
        flagImageView.setImageResource(flagResId);

        return convertView;
    }

    // Method to get the flag resource ID based on the item text
    private int getFlagResourceForItem(String itemText) {
        // Look up the currency in the currencyToFlagMap
        int flagResId = Dashboard.currencyToFlagMap.get(itemText);

        // Check if a flag resource ID is found for the currency
        if (flagResId != 0) {
            // Return the flag resource ID if found
            return flagResId;
        } else {
            // If no flag resource ID is found, return a placeholder flag image
            return R.drawable.baseline_outlined_flag_24; // Placeholder flag image, replace with actual flag images
        }
    }
}