package com.example.phnloinhn.src.View;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phnloinhn.R;
import com.example.phnloinhn.src.Model.History;

import android.widget.ImageView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.example.phnloinhn.src.Utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends ListAdapter<History, HistoryAdapter.ViewHolder> {


    private final String TAG = "HistoryAdapter";
    private Context context;

    private static final Map<String, Integer> VARIANT_NAME_TO_RES_ID = new HashMap<>();

    static {
        VARIANT_NAME_TO_RES_ID.put("ido", R.string.ido);
        VARIANT_NAME_TO_RES_ID.put("tieu", R.string.tieu);
        VARIANT_NAME_TO_RES_ID.put("xuong", R.string.xuong);
        VARIANT_NAME_TO_RES_ID.put("thanh_nhan", R.string.thanh_nhan);
    }

    public HistoryAdapter(Context context, OnHistoryDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.deleteListener = deleteListener;
    }
    public interface OnHistoryDeleteListener {
        void onDelete(History history);
    }
    private final OnHistoryDeleteListener deleteListener;
    private static final DiffUtil.ItemCallback<History> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<History>() {
                @Override
                public boolean areItemsTheSame(@NonNull History oldItem, @NonNull History newItem) {
                    // Use timestamp or another unique ID if available
                    return oldItem.getTimestamp().equals(newItem.getTimestamp());
                }

                @Override
                public boolean areContentsTheSame(@NonNull History oldItem, @NonNull History newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textName, textTimestamp;
        ImageButton btnDelete; // new

        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.history_img);
            textName = view.findViewById(R.id.variantName);
            textTimestamp = view.findViewById(R.id.timestamp);
            btnDelete = view.findViewById(R.id.btn_delete); // new
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = getItem(position);

        String variant_name = history.getVariantName();
        Integer resId = VARIANT_NAME_TO_RES_ID.get(variant_name);

        if (resId != null) {
            holder.textName.setText(context.getString(resId));
        } else {
            holder.textName.setText(variant_name); // fallback
        }

        // Convert and format timestamp
        String rawTimestamp = history.getTimestamp(); // e.g., "20250928-173045"

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        try {
            Date date = inputFormat.parse(rawTimestamp);
            String formattedDate = outputFormat.format(date);
            holder.textTimestamp.setText(formattedDate);
        } catch (ParseException e) {
            holder.textTimestamp.setText(R.string.invalid_date);
        } catch (Exception e) {
            Log.e(TAG, "Error in set date: " +  e);
            throw new RuntimeException(e);
        }

        Glide.with(holder.itemView.getContext())
                .load(history.getImageUrl())
                .placeholder(R.drawable.ic_history_black_24dp) // add a default drawable
                .error(R.drawable.ic_password)       // add an error drawable
                .into(holder.imageView);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(history);
            }
        });
    }
}

