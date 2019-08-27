package com.example.lifelocator360.FragmentManagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.photos;

public class PhotoFragment extends Fragment {

    private PhotoAdapter photoAdapter;
    private RecyclerView recyclerView;
    private View view;
    private ImageView imageMissingPhotos;
    private TextView textMissingPhotos;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_photo, container, false);
        imageMissingPhotos = view.findViewById(R.id.image_missing_photos);
        textMissingPhotos = view.findViewById(R.id.text_missing_photos);

        recyclerView = view.findViewById(R.id.recycler_view_photo);
        //recyclerView.setHasFixedSize(true);
        photoAdapter = new PhotoAdapter(getContext(), photos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4)); //GetApplicationContext?
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(photoAdapter);

        recyclerView.addOnItemTouchListener(new PhotoAdapter.RecyclerTouchListener(getContext(), recyclerView, new PhotoAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                SlideShowDialogFragment slideShowDialogFragment = SlideShowDialogFragment.newInstance();
                slideShowDialogFragment.setArguments(bundle);
                slideShowDialogFragment.show(fragmentTransaction, "slideshow");

                SlideShowDialogFragment.tagImageClicked = position;
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        updateMissingPhotosBackground();
        return view;
    }

    public void updateMissingPhotosBackground() {
        if (NavigationDrawerActivity.photos.isEmpty()) {
            imageMissingPhotos.setVisibility(View.VISIBLE);
            textMissingPhotos.setVisibility(View.VISIBLE);
        } else {
            imageMissingPhotos.setVisibility(View.INVISIBLE);
            textMissingPhotos.setVisibility(View.INVISIBLE);
        }
    }

}
