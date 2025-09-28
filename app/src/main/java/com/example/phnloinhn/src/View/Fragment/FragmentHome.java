package com.example.phnloinhn.src.View.Fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.phnloinhn.R;
import com.example.phnloinhn.databinding.FragmentHomeBinding;
import com.example.phnloinhn.databinding.ItemBulletBinding;
import com.example.phnloinhn.databinding.ItemImageBinding;
import com.example.phnloinhn.databinding.ItemParagraphBinding;
import com.example.phnloinhn.databinding.ItemSectionTitleBinding;
import com.example.phnloinhn.src.Model.GrowingMethod;
import com.example.phnloinhn.src.Model.LonganVariant;
import com.example.phnloinhn.src.ViewModel.SharedViewModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding binding;
    private OnBackPressedCallback callback;
    private SharedViewModel viewModel;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private final String TAG = "FragmentHome";
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private Uri tempPhotoUri;


    private static final Map<String, Integer> VARIANT_IMAGES = new HashMap<>();
    static {
        VARIANT_IMAGES.put("ido", R.drawable.ido);
        VARIANT_IMAGES.put("tieu", R.drawable.tieu);
        VARIANT_IMAGES.put("xuong", R.drawable.xuong);
        VARIANT_IMAGES.put("thanh_nhan", R.drawable.thanh_nhan);
    }
    private static final Map<String, String> VARIANT_ID = new HashMap<>();
    static {
        VARIANT_ID.put("Nhãn Ido", "ido");
        VARIANT_ID.put("Nhãn Tiêu", "tieu");
        VARIANT_ID.put("Nhãn Xuồng", "xuong");
        VARIANT_ID.put("Thanh Nhãn", "thanh_nhan");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Add callback for initialize FragmentHome
        callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // Clear dynamic content and show initial instructions again
                binding.container.removeAllViews();
                displayInitialInformation();
            }
        };

        // Enalble callback
        callback.setEnabled(true);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                callback
        );

        // Assign viewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                            viewModel.classifyImage(bitmap, imageUri);
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempPhotoUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().getContentResolver(),
                                    tempPhotoUri
                            );
                            viewModel.classifyImage(bitmap, tempPhotoUri);
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Không đọc được ảnh chụp", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // Attach contract to fabAdd
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
        });
        // Attach contract to fabCamera
        binding.fabCamera.setOnClickListener(v -> {
            tempPhotoUri = createTempImageUri();
            takePhotoLauncher.launch(tempPhotoUri);
        });


        viewModel.getSelectedVariant().observe(getViewLifecycleOwner(), variant -> {
            if (variant == null) displayInitialInformation();
            else displayVariant(variant);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void displayInitialInformation() {
        callback.setEnabled(false);
        binding.container.removeAllViews();

        // Add fab buttons
        binding.fabAdd.setVisibility(VISIBLE);
        binding.fabAdd.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        binding.fabCamera.setVisibility(VISIBLE);
        binding.fabCamera.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        // Notes
        addParagraph(getString(R.string.note_title), "");
        addNoTitleBullet(R.string.note_1, 0);
        addNoTitleBullet(R.string.note_2, 0);
        addNoTitleBullet(R.string.note_3, 0);

        // Guides
        addParagraph(getString(R.string.guide_title), "");

        // Insert camera button inline
        addNoTitleBullet(R.string.guide_1, R.drawable.ic_camera_green_24dp);
        // Insert gallery button inline
        addNoTitleBullet(R.string.guide_2, R.drawable.ic_add_green_24dp);

    }

    private void displayVariant(LonganVariant variant) {
        binding.container.removeAllViews();

        // Hide and disable fab buttons until back to initial screen
        binding.fabAdd.setVisibility(GONE);
        binding.fabAdd.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        binding.fabCamera.setVisibility(GONE);
        binding.fabCamera.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // Section: Name
        addSectionTitle(variant.getName());

        // Image
        addImageView(variant.getName());

        // Paragraphs (localized titles)
        addParagraph(getString(R.string.variant_origin), variant.getOrigin());
        addParagraph(getString(R.string.variant_productivity), variant.getProductivity());
        addParagraph(getString(R.string.variant_description), variant.getDescription());
        addParagraph(getString(R.string.variant_tips), variant.getTips());

        if (variant.getGrowingMethods() != null) {
            for (Map.Entry<String, GrowingMethod> entry : variant.getGrowingMethods().entrySet()) {
                String methodName = entry.getKey();
                GrowingMethod method = entry.getValue();

                addSectionTitle(getString(R.string.variant_growing_method, methodName));

                addBullet(getString(R.string.growing_branch_pruning), method.getBranch_pruning());
                addBullet(getString(R.string.growing_fertilizer), method.getFertilizer());
                addBullet(getString(R.string.growing_fruit_pruning), method.getFruit_pruning());
                addBullet(getString(R.string.growing_pesticide), method.getPesticide());
                addBullet(getString(R.string.growing_plant_distance), method.getPlant_distance());
                addBullet(getString(R.string.growing_plant_time), method.getPlant_time());
                addBullet(getString(R.string.growing_soil), method.getSoil());
                addBullet(getString(R.string.growing_other), method.getOther());
            }
        }
    }

    private void addSectionTitle(String text) {
        ItemSectionTitleBinding itemBinding = ItemSectionTitleBinding.inflate(getLayoutInflater(), binding.container, false);
        itemBinding.tvSectionTitle.setText(text);
        binding.container.addView(itemBinding.getRoot());
    }
    private void addImageView(String variantName) {
        Log.d(TAG, "variantName: " + variantName);
        String variant_id = VARIANT_ID.get(variantName);
        Integer resId = VARIANT_IMAGES.get(variant_id);
        Log.d(TAG, "resId: " + resId);
        if (resId != null) {
            ItemImageBinding itemBinding = ItemImageBinding.inflate(getLayoutInflater(), binding.container, false);
            itemBinding.ivVariant.setImageResource(resId);
            binding.container.addView(itemBinding.getRoot());
        }
    }


    private void addParagraph(String title, String value) {
        if (value == null || value.trim().isEmpty()) return;
        ItemParagraphBinding itemBinding = ItemParagraphBinding.inflate(getLayoutInflater(), binding.container, false);
        itemBinding.tvParagraphTitle.setText(title);
        itemBinding.tvParagraphText.setText(value);
        binding.container.addView(itemBinding.getRoot());
    }

    private void addBullet(String label, String value) {
        if (value == null || value.trim().isEmpty()) return;
        ItemBulletBinding itemBinding = ItemBulletBinding.inflate(getLayoutInflater(), binding.container, false);
        itemBinding.tvBullet.setText(getString(R.string.tvBullet, label, value));
        binding.container.addView(itemBinding.getRoot());
    }

    /*
    * @DrawableRes int drawableResId: R.drawable.{icon} / 0*/
    private void addNoTitleBullet(@StringRes int textResId, @DrawableRes int drawableResId) {
        String textTemplate = getString(textResId);

        SpannableString spannable;
        if (drawableResId == 0) {
            // Case 1: Plain bullet (no icon, no placeholder replacement)
            spannable = new SpannableString(textTemplate);

        } else {
            // Case 2: Bullet with icon (replace placeholder with space, then insert ImageSpan)
            spannable = new SpannableString(String.format(textTemplate, " "));

            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = requireContext().getDrawable(drawableResId);
            if (drawable != null) {
                int lineHeight = (int) binding.container.getResources()
                        .getDimension(R.dimen.inline_icon_size);
                drawable.setBounds(0, 0, lineHeight, lineHeight);

                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);

                int start = spannable.toString().indexOf(" ");
                if (start >= 0) {
                    spannable.setSpan(imageSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        ItemBulletBinding itemBinding =
                ItemBulletBinding.inflate(getLayoutInflater(), binding.container, false);
        itemBinding.tvBullet.setText(spannable);
        binding.container.addView(itemBinding.getRoot());
    }

    private Uri createTempImageUri() {
        File photoFile = new File(requireContext().getFilesDir(), "temp_photo.jpg");
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                photoFile
        );
    }

}