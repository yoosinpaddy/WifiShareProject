package com.passowrd.key.wifishare.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.activity.CancelSharingActivity;
import com.passowrd.key.wifishare.activity.FeedbackActivity;
import com.passowrd.key.wifishare.activity.InstructionsActivity;
import com.passowrd.key.wifishare.activity.SettingsActivity;
import com.passowrd.key.wifishare.activity.SigninnActivity;
import com.passowrd.key.wifishare.databinding.FragmentMoreBinding;


public class MoreFragment extends Fragment {

    private FragmentMoreBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_more, container, false);
        b.fabFeedBack.setOnClickListener(this::launchfeedback);
        b.fabFollowUs.setOnClickListener(this::openFaceBookShare);
        b.fabRateUs.setOnClickListener(this::openRateUrl);
        b.fabInvite.setOnClickListener(v -> {
            inviteIntent("");
        });
        b.fabFeedBack.setOnClickListener(this::launchfeedback);
        b.fabSettings.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SettingsActivity.class));
        });
        b.fabUpdate.setOnClickListener(v -> {
            Toast.makeText(getContext(), "You already have the latest version", Toast.LENGTH_SHORT).show();
        });
        b.fabFAQ.setOnClickListener(v -> {
            Intent intent = new Intent(new Intent(getContext(), InstructionsActivity.class));
            intent.putExtra("num", 1);
            startActivity(intent);
        });
        b.fabCancelSharing.setOnClickListener(v -> {
            Intent intent = new Intent(new Intent(getContext(), CancelSharingActivity.class));
            startActivity(intent);
        });


        return b.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Glide.with(b.imgLogin)
                    .load(mAuth.getCurrentUser().getPhotoUrl())
                    .into(b.imgLogin);
            b.toolbar.setTitle(mAuth.getCurrentUser().getDisplayName());
            b.toolbar.setSubtitle(mAuth.getCurrentUser().getEmail());
        } else {
            b.toolbar.setTitle("Login");
            b.toolbar.setSubtitle("Free WiFi access anytime anywhere");
            b.imgLogin.setImageResource(R.drawable.photo_female_1);
        }
        b.imgLogin.setOnClickListener(v -> startActivity(new Intent(getContext(), SigninnActivity.class)));
    }

    public void openLink(String Url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
        startActivity(browserIntent);
    }

    public void openFaceBookShare(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=#https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.passowrd.key.wifishare%26hl%3Den"));
        startActivity(browserIntent);
    }

    public void openRateUrl(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.passowrd.key.wifishare&hl=en"));
        startActivity(browserIntent);
    }

    public void inviteIntent(String Url) {
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        /*This will be the actual content you wish you share.*/
        String shareBody = "Checkout this awesome WiFi sharing app! https://play.google.com/store/apps/details?id=com.passowrd.key.wifishare&hl=en";
        /*The type of the content is text, obviously.*/
        intent.setType("text/plain");
        /*Applying information Subject and Body.*/
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "WIFI Share App");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Share Using"));
    }

    public void launchfeedback(View v) {
        startActivity(new Intent(getContext(), FeedbackActivity.class));
    }
}