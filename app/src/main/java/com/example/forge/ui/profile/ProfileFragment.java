package com.example.forge.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.forge.LoginActivity;
import com.example.forge.MainActivity;
import com.example.forge.R;
import com.example.forge.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        TextView tv_username = view.findViewById(R.id.profileUsername);
        TextView tv_email = view.findViewById(R.id.profileEmail);
        TextView tv_password = view.findViewById(R.id.profilePassword);
        TextView tv_userRole = view.findViewById(R.id.profile_ac);
        Button logoutButton = view.findViewById(R.id.buttonLogout);
        Button deleteAccountButton = view.findViewById(R.id.buttonDeleteAcc);
        Button editProfileButton = view.findViewById(R.id.buttonChangePassword);

        tv_email.setText(auth.getCurrentUser().getEmail());
        tv_password.setText("******");

        FirebaseUser firebaseUser = auth.getCurrentUser();
        String displayName = firebaseUser.getDisplayName();

        tv_username.setText(displayName);

        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("UserRole", "");
        if (userRole.equals("Coach")) {
            tv_userRole.setText("Coach");
        } else {
            tv_userRole.setText("Athlete");
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog(false);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog(true);
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_edit);
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

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDelete) {
                    auth.getCurrentUser().delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        auth.signOut();
                                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    auth.signOut();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}