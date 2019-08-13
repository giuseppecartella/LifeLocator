package com.example.lifelocator360.FragmentManagement;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.lifelocator360.MapManagement.MapsFragment;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.ZOOM_TO_MARKER;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.mainMapsFragment;


public class SlideShowDialogFragment extends DialogFragment implements View.OnClickListener {
    private String TAG = SlideShowDialogFragment.class.getSimpleName();

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0;
    private FloatingActionButton floatingActionButton;

    public static int tagImageClicked;

    static SlideShowDialogFragment newInstance() {
        SlideShowDialogFragment f = new SlideShowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.photo_view_pager);
        lblCount = (TextView) v.findViewById(R.id.photo_counter);
        lblTitle = (TextView) v.findViewById(R.id.photo_title);
        lblDate = (TextView) v.findViewById(R.id.photo_date);


        floatingActionButton = v.findViewById(R.id.showPhotoInMap);
        floatingActionButton.setOnClickListener(this);

        selectedPosition = getArguments().getInt("position");

        Log.e(TAG, "position: " + selectedPosition);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //	page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " di " + NavigationDrawerActivity.photos.size());

        File image = NavigationDrawerActivity.photos.get(position);
        lblTitle.setText(image.getName());
        Date date = new Date(image.lastModified());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        lblDate.setText(dateFormat.format(date));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showPhotoInMap: {

                String filePath = NavigationDrawerActivity.photos.get(tagImageClicked).getAbsolutePath();

                //Leggo i metadata del file
                try {
                    ExifInterface exifInterface = new ExifInterface(filePath);
                    float[] latlng = new float[2];
                    exifInterface.getLatLong(latlng);

                    if(latlng[0] != 0 || latlng[1] != 0) {
                        MapsFragment.moveCamera(new LatLng(latlng[0], latlng[1]), ZOOM_TO_MARKER);
                        returnToMap();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        returnToMap();
    }

    private void returnToMap() {
        NavigationDrawerActivity.currentFragment = "Mappa";
        NavigationDrawerActivity.uncheckAllNavigationItems();

        //Hide return to map button
        getActivity().invalidateOptionsMenu();

        getFragmentManager().popBackStack();

        this.dismiss();

    }

    //	adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_full_screen_preview, container, false);

            ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);

            File image = NavigationDrawerActivity.photos.get(position);

            File file = NavigationDrawerActivity.photos.get(position);

            Uri imageUri = Uri.fromFile(file);
            Glide.with(getActivity()).load(imageUri)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .into(imageViewPreview);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return NavigationDrawerActivity.photos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}