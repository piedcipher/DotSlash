package com.teamzero.easyedu.ui.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamzero.easyedu.GlideApp;
import com.teamzero.easyedu.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.profile_picture)
    ImageView profilePicture;
    @BindView(R.id.profile_username)
    TextView profileUsername;
    @BindView(R.id.profile_email)
    TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            //TODO: Set Place Holder
            GlideApp.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(profilePicture);
            profileUsername.setText(currentUser.getDisplayName());
            profileEmail.setText(currentUser.getEmail());
        }
    }
}
