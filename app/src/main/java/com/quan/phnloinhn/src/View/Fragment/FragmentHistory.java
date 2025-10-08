package com.quan.phnloinhn.src.View.Fragment;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quan.phnloinhn.R;
import com.quan.phnloinhn.src.Model.History;
import com.quan.phnloinhn.src.Utils.Utils;
import com.quan.phnloinhn.src.View.ActivityMain;
import com.quan.phnloinhn.src.View.HistoryAdapter;
import com.quan.phnloinhn.src.ViewModel.SharedViewModel;

public class FragmentHistory extends Fragment {
    private SharedViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView emptyStateTextView;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(requireContext(),
                // Delete listener
                history -> {
                    Utils.showLoading(requireContext(), getString(R.string.processing));
                    viewModel.deleteHistory(history);
                },
                // Item click listener for viewing details
                this::showHistoryDetail
        );

        recyclerView.setAdapter(adapter);

        // Setup swipe-to-delete
        setUpItemTouchHelper();

        // Get shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe history list
        viewModel.getHistoryList().observe(getViewLifecycleOwner(), histories -> {
            if (histories != null && !histories.isEmpty()) {
                adapter.submitList(histories);
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    History history = adapter.getCurrentList().get(position);

                    Utils.showLoading(getActivity(), "Đang xoá...");
                    viewModel.deleteHistory(history);

                    Toast.makeText(getContext(), "Đã xoá lịch sử", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    View cardView = itemView.findViewById(R.id.history_card_foreground);
                    View deleteBackground = itemView.findViewById(R.id.delete_background);

                    if (cardView != null && deleteBackground != null) {
                        deleteBackground.setVisibility(View.VISIBLE);
                        cardView.setTranslationX(dX);

                        float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
                        cardView.setAlpha(Math.max(alpha, 0.3f));
                    } else {
                        drawDefaultSwipeBackground(c, itemView, dX);
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                View cardView = viewHolder.itemView.findViewById(R.id.history_card_foreground);
                View deleteBackground = viewHolder.itemView.findViewById(R.id.delete_background);

                if (cardView != null) {
                    cardView.setTranslationX(0);
                    cardView.setAlpha(1.0f);
                }

                if (deleteBackground != null) {
                    deleteBackground.setVisibility(View.GONE);
                }
            }

            private void drawDefaultSwipeBackground(Canvas c, View itemView, float dX) {
                Drawable deleteIcon = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_menu_delete);
                ColorDrawable background = new ColorDrawable(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));

                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                if (dX < 0) {
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(),
                            iconTop,
                            itemView.getRight() - iconMargin,
                            iconBottom);
                    background.draw(c);
                    deleteIcon.draw(c);
                }
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.7f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showHistoryDetail(History history) {
        if (history == null) {
            Toast.makeText(getContext(), "Lỗi: Không có dữ liệu lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update ViewModel to set history viewing mode
        viewModel.viewHistoryDetail(history);

        // Navigate to FragmentHome
        navigateToHome();
    }

    private void navigateToHome() {
        String TAG = "FragmentHistory";
        if (getActivity() instanceof ActivityMain mainActivity) {
            mainActivity.navigateToHome();
            Log.d(TAG, "Navigated to Home via ActivityMain method");
        } else {
            Log.e(TAG, "Failed to navigate - Activity is not ActivityMain");
            Toast.makeText(getContext(), "Không thể chuyển sang trang chủ", Toast.LENGTH_SHORT).show();
        }
    }
}