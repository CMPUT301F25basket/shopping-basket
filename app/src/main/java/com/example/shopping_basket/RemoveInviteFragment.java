package com.example.shopping_basket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class RemoveInviteFragment extends DialogFragment {

    interface RemoveCityDialogueListener{
        void PassResponse(boolean response);
    }

    private RemoveCityDialogueListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setTitle("Remove Invite?")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    listener.PassResponse(false);
                })
                .setPositiveButton("Remove", (dialog, which) -> {
                    listener.PassResponse(true);
                })
                .create();
    }
}
