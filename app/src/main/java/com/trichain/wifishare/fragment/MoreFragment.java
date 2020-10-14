package com.trichain.wifishare.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.FeedbackActivity;
import com.trichain.wifishare.databinding.FragmentMoreBinding;


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


        return b.getRoot();

    }

    public void openLink(String Url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
        startActivity(browserIntent);
    }

    public void openFaceBookShare(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=#https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.trichain.wifishare%26hl%3Den"));
        startActivity(browserIntent);
    }

    public void openRateUrl(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.trichain.wifishare&hl=en"));
        startActivity(browserIntent);
    }

    public void inviteIntent(String Url) {
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        /*This will be the actual content you wish you share.*/
        String shareBody = "Checkout this awesome WiFi sharing app! https://play.google.com/store/apps/details?id=com.trichain.wifishare&hl=en";
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