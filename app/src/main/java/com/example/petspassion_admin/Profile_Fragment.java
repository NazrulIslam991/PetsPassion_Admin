package com.example.petspassion_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Profile_Fragment extends Fragment {

    private SessionManager sessionManager;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_, container, false);

        sessionManager = new SessionManager(getContext());

        logoutButton = rootView.findViewById(R.id.logout);

        logoutButton.setOnClickListener(v -> logout());

        return rootView;
    }




    //...................................... Logout method that clears the session..................................................
    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(getContext(), Login_Activity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
