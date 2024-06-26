package com.example.forge.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.forge.R;
import com.example.forge.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private ProfileViewModel viewModel;
    private ActivityResultLauncher<String> galleryLauncher;
    private ImageView profilePicture;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        progressBar = view.findViewById(R.id.baseline_loop);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        TextView tv_username = view.findViewById(R.id.profileUsername);
        TextView tv_email = view.findViewById(R.id.profileEmail);
        TextView tv_password = view.findViewById(R.id.profilePassword);
        TextView tv_userRole = view.findViewById(R.id.profile_ac);
        Button logoutButton = view.findViewById(R.id.buttonLogout);
        Button deleteAccountButton = view.findViewById(R.id.buttonDeleteAcc);
        Button editProfileButton = view.findViewById(R.id.buttonChangePassword);
        ImageButton addImage = view.findViewById(R.id.add_image);
        profilePicture = view.findViewById(R.id.imageViewAvatar);
        EditText sportEditText = view.findViewById(R.id.sport_ac);
        Button saveButton = view.findViewById(R.id.buttonSave_ac);

        FirebaseUser firebaseUser = auth.getCurrentUser();
        String displayName = firebaseUser.getDisplayName();

        tv_email.setText(auth.getCurrentUser().getEmail());
        tv_password.setText("******");
        tv_username.setText(displayName);

        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("UserRole", "");
        tv_userRole.setText(userRole);

        viewModel.getProfileImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            if (imageUrl.startsWith("@")) {
                profilePicture.setImageResource(R.drawable.baseline_downloading);
            } else {
                Glide.with(requireContext()).load(imageUrl).into(profilePicture);
            }

        });
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                progressBar.setVisibility(View.GONE);
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        viewModel.loadProfilePicture();

        addImage.setOnClickListener(v -> {
            ImagePickerDialogFragment dialogFragment = new ImagePickerDialogFragment();
            dialogFragment.show(getChildFragmentManager(), "ImagePickerDialogFragment");
        });

        AtomicBoolean isEditTextChangedByUser = new AtomicBoolean(false);
        sportEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEditTextChangedByUser.get()) {
                    if (s.length() > 0) {
                        saveButton.setVisibility(View.VISIBLE);
                    } else {
                        saveButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sportEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isEditTextChangedByUser.set(true);
            }
        });

        saveButton.setOnClickListener(v -> {
            String sportText = sportEditText.getText().toString().trim();
            if (!sportText.isEmpty()) {
                viewModel.updateUserField("sport", sportText);
                sportEditText.clearFocus();

                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(sportEditText.getWindowToken(), 0);
                }

                saveButton.setVisibility(View.GONE);
            }
        });

        viewModel.loadSport();

        viewModel.getSport().observe(getViewLifecycleOwner(), sport -> {
            sportEditText.setText(sport);
        });

        logoutButton.setOnClickListener(view1 -> showConfirmationDialog(false));

        deleteAccountButton.setOnClickListener(view12 -> showConfirmationDialog(true));

        editProfileButton.setOnClickListener(view13 -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_edit);
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        viewModel.uploadImageToFirebaseStorage(result);
                    }
                });

    }

    private void showConfirmationDialog(boolean isDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);

        TextView messageTextView = dialogView.findViewById(R.id.textViewMessage);
        Button cancelBtn = dialogView.findViewById(R.id.buttonCancel);
        Button confirmBtn = dialogView.findViewById(R.id.buttonConfirm);

        if (isDelete) {
            messageTextView.setText("Are you sure you want to delete your account?");
        } else {
            messageTextView.setText("Are you sure you want to log out from your account?");
        }

        AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            if (isDelete) {
                viewModel.deleteUserAccount(requireActivity());
            } else {
                viewModel.logoutUser(requireActivity());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
