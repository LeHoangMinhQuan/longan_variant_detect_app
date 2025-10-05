package com.quan.phnloinhn.src.View.Fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quan.phnloinhn.R;
import com.quan.phnloinhn.databinding.FragmentHomeBinding;
import com.quan.phnloinhn.databinding.ItemBulletBinding;
import com.quan.phnloinhn.databinding.ItemImageBinding;
import com.quan.phnloinhn.databinding.ItemParagraphBinding;
import com.quan.phnloinhn.src.Model.GrowingMethod;
import com.quan.phnloinhn.src.Model.LonganVariant;
import com.quan.phnloinhn.src.ViewModel.SharedViewModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding binding;
    private OnBackPressedCallback callback;
    private SharedViewModel viewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
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

        // Permission request launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(requireContext(), "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.Source source =
                                        ImageDecoder.createSource(requireContext().getContentResolver(), imageUri);
                                bitmap = ImageDecoder.decodeBitmap(source);
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                        requireContext().getContentResolver(), imageUri);
                            }
                            // 🔍 Send bitmap to your ViewModel or use it
                            viewModel.classifyImage(bitmap, imageUri);

                        } catch (IOException e) {
                            Log.e(TAG, "Error: " + e);
                            Toast.makeText(requireContext(), "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // 🔹 Take photo launcher (Camera)
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempPhotoUri != null) {
                        try {
                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.Source source =
                                        ImageDecoder.createSource(requireContext().getContentResolver(), tempPhotoUri);
                                bitmap = ImageDecoder.decodeBitmap(source);
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                        requireContext().getContentResolver(), tempPhotoUri);
                            }
                            viewModel.classifyImage(bitmap, tempPhotoUri);
                        } catch (IOException e) {
                            Log.e(TAG, "Error: " + e);
                            Toast.makeText(requireContext(), "Không đọc được ảnh chụp", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Attach contract to fabAdd
        binding.fabAdd.setOnClickListener(v -> checkGalleryPermissionAndOpen());
        // Attach contract to fabCamera
        binding.fabCamera.setOnClickListener(v -> {
            tempPhotoUri = createTempImageUri();
            takePhotoLauncher.launch(tempPhotoUri);
        });


        viewModel.getSelectedVariant().observe(getViewLifecycleOwner(), variant -> {
            if (variant == null) displayInitialInformation();
            else displayVariant(variant);
        });
    }

    // Create URI for camera photo (compatible with FileProvider)
    private Uri createTempImageUri() {
        File photoFile = new File(requireContext().getFilesDir(), "temp_photo.jpg");
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                photoFile
        );
    }

    /** Check permission (API 24–35 safe) */
    private void checkGalleryPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 7–12
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    /** 🔸 Open gallery */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void displayInitialInformation() {
        callback.setEnabled(false);

        // Show FABs
        binding.fabAdd.setVisibility(View.VISIBLE);
        binding.fabCamera.setVisibility(View.VISIBLE);

        // Hide all plant info
        setVisible(binding.plantNameTitle, null, false);
        setVisible(null, binding.plantImage, false);

        setVisible(binding.general, null, false);
        setVisible(binding.description, null, false);
        setVisible(binding.tips, null, false);
        setVisible(binding.growingMethods, null, false);

        setVisible(binding.plantOrigin, binding.img1, false);
        setVisible(binding.plantProductivity, binding.img2, false);
        setVisible(binding.plantDescription, binding.img3, false);
        setVisible(binding.plantTips, binding.img4, false);
        setVisible(binding.branchPruning, binding.img5, false);
        setVisible(binding.fertilizer, binding.img6, false);
        setVisible(binding.fruitPruning, binding.img7, false);
        setVisible(binding.pesticide, binding.img8, false);
        setVisible(binding.plantDistance, binding.img9, false);
        setVisible(binding.plantTime, binding.img10, false);
        setVisible(binding.soil, binding.img11, false);
        setVisible(binding.other, binding.img12, false);

        // Show notes dynamically
        addParagraph(getString(R.string.note_title), "");
        addNoTitleBullet(R.string.note_1, 0);
        addNoTitleBullet(R.string.note_2, 0);
        addNoTitleBullet(R.string.note_3, 0);
        addParagraph(getString(R.string.guide_title), "");
        addNoTitleBullet(R.string.guide_1, R.drawable.ic_camera_green_24dp);
        addNoTitleBullet(R.string.guide_2, R.drawable.ic_add_green_24dp);
    }
    private void displayVariant(LonganVariant variant) {
        // Enable callback
        callback.setEnabled(true);
        // Hide FABs
        binding.fabAdd.setVisibility(View.GONE);
        binding.fabCamera.setVisibility(View.GONE);

        // Hide init info
        binding.initSection.setVisibility(GONE);

        // Show basic plant info
        setVisible(binding.plantNameTitle, null, true);
        Log.d("Image","getImageResourceByName(variant.getName()) :" + getImageResourceByName(variant.getName()));
        binding.plantImage.setImageResource(getImageResourceByName(variant.getName()));
        setVisible(null, binding.plantImage, true);
        setVisible(binding.general, null, true);
        setVisible(binding.description, null, true);
        setVisible(binding.tips, null, true);

        setVisible(binding.plantOrigin, binding.img1, true);
        setVisible(binding.plantProductivity, binding.img2, true);
        setVisible(binding.plantDescription, binding.img3, true);
        setVisible(binding.plantTips, binding.img4, true);

        // Set plant info
        binding.plantNameTitle.setText(variant.getName());
        binding.plantImage.setImageResource(getImageResourceByName(variant.getName()));
        binding.plantOrigin.setText(variant.getOrigin());
        binding.plantProductivity.setText(variant.getProductivity());
        binding.plantDescription.setText(variant.getDescription());
        binding.plantTips.setText(variant.getTips());

        // Growing Methods
        if (variant.getGrowingMethods() != null && !variant.getGrowingMethods().isEmpty()) {
            GrowingMethod method = variant.getGrowingMethods().values().iterator().next();

            setVisible(binding.growingMethods, null, true);
            setVisible(binding.branchPruning, binding.img5, true);
            setVisible(binding.fertilizer, binding.img6, true);
            setVisible(binding.fruitPruning, binding.img7, true);
            setVisible(binding.pesticide, binding.img8, true);
            setVisible(binding.plantDistance, binding.img9, true);
            setVisible(binding.plantTime, binding.img10, true);
            setVisible(binding.soil, binding.img11, true);
            setVisible(binding.other, binding.img12, true);

            binding.branchPruning.setText(method.getBranch_pruning());
            binding.fertilizer.setText(method.getFertilizer());
            binding.fruitPruning.setText(method.getFruit_pruning());
            binding.pesticide.setText(method.getPesticide());
            binding.plantDistance.setText(method.getPlant_distance());
            binding.plantTime.setText(method.getPlant_time());
            binding.soil.setText(method.getSoil());
            binding.other.setText(method.getOther());
        }
    }

    // Helper method to get drawable by plant name
    private int getImageResourceByName(String variantName) {
        String resName = VARIANT_ID.get(variantName); // get mapped resource name
        if (resName == null) {
            Log.d("Image", "No mapping found for variant: " + variantName);
            return R.drawable.ic_clear; // fallback image
        }

        // Resource name in drawable
        int resId = getResources().getIdentifier(resName, "drawable", requireContext().getPackageName());

        Log.d("Image", "Variant: " + variantName + ", ResName: " + resName + ", ResId: " + resId);

        return resId != 0 ? resId : R.drawable.ic_clear;
    }


    private void setVisible(TextView textView, ImageView imageView, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (textView != null) textView.setVisibility(visibility);
        if (imageView != null) imageView.setVisibility(visibility);
    }
    private void addParagraph(String title, String value) {
        if (value == null) return;
        ItemParagraphBinding itemBinding = ItemParagraphBinding.inflate(getLayoutInflater(), binding.initSection, false);
        itemBinding.tvParagraphTitle.setText(title);
        if(value.trim().isEmpty()){
            itemBinding.tvParagraphText.setVisibility(GONE);
        } else {
            itemBinding.tvParagraphText.setText(value);
        }
        binding.initSection.addView(itemBinding.getRoot());
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
                int lineHeight = (int) binding.initSection.getResources()
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
                ItemBulletBinding.inflate(getLayoutInflater(), binding.initSection, false);
        itemBinding.tvBullet.setText(spannable);
        binding.initSection.addView(itemBinding.getRoot());
    }
}