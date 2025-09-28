package com.example.phnloinhn.src.Utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpace;

    public SpacingItemDecoration(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Add bottom spacing to every item
        outRect.bottom = verticalSpace;

        // Optional: also add top spacing for the first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = verticalSpace;
        }
    }
}

