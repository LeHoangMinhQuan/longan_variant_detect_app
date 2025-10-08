package com.quan.phnloinhn.src.View;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quan.phnloinhn.R;
import com.quan.phnloinhn.src.Model.History;

import android.widget.ImageView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends ListAdapter<History, HistoryAdapter.ViewHolder> {

    private final Context context;

    private static final Map<String, Integer> VARIANT_NAME_TO_RES_ID = new HashMap<>();

    static {
        VARIANT_NAME_TO_RES_ID.put("ido", R.string.ido);
        VARIANT_NAME_TO_RES_ID.put("tieu", R.string.tieu);
        VARIANT_NAME_TO_RES_ID.put("xuong", R.string.xuong);
        VARIANT_NAME_TO_RES_ID.put("thanh_nhan", R.string.thanh_nhan);
    }

    public interface OnHistoryDeleteListener {
        void onDelete(History history);
    }

    public interface OnHistoryClickListener {
        void onClick(History history);
    }

    private final OnHistoryDeleteListener deleteListener;
    private final OnHistoryClickListener clickListener;

    public HistoryAdapter(Context context, OnHistoryDeleteListener deleteListener, OnHistoryClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<History> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull History oldItem, @NonNull History newItem) {
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
        ImageButton btnDelete;
        View cardView;

        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.history_img);
            textName = view.findViewById(R.id.variantName);
            textTimestamp = view.findViewById(R.id.timestamp);
            btnDelete = view.findViewById(R.id.btn_delete);
            cardView = view.findViewById(R.id.history_card_foreground);
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
            holder.textName.setText(variant_name);
        }

        // Convert and format timestamp
        String rawTimestamp = history.getTimestamp();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        try {
            Date date = inputFormat.parse(rawTimestamp);
            String formattedDate = outputFormat.format(date);
            holder.textTimestamp.setText(formattedDate);
        } catch (ParseException e) {
            holder.textTimestamp.setText(R.string.invalid_date);
        } catch (Exception e) {
            String TAG = "HistoryAdapter";
            Log.e(TAG, "Error in set date: " + e);
            throw new RuntimeException(e);
        }

        Glide.with(holder.itemView.getContext())
                .load(history.getImageUrl())
                .placeholder(R.drawable.ic_history_black_24dp)
                .error(R.drawable.ic_password)
                .into(holder.imageView);

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(history);
            }
        });

        // Card click for viewing details
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(history);
            }
        });

        // Also make the whole item clickable
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(history);
            }
        });
    }
}