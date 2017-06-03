package com.sow.gpstrackerpro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sow.gpstrackerpro.R;
import com.sow.gpstrackerpro.classes.Fragments;

public class FragmentSignIn extends Fragment  {

    private SignInButton sign_in_button;
    private OnFragmentSignInFinishes onFragmentSignInFinishes;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        sign_in_button = (SignInButton) view.findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentSignInFinishes.onFragmentSignInFinishes(Fragments.SIGN_IN_REQUEST);
            }
        });

        return view;
    }

    public interface OnFragmentSignInFinishes {
        public void onFragmentSignInFinishes(Fragments fragmentName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFragmentSignInFinishes = (OnFragmentSignInFinishes) context;
    }
}