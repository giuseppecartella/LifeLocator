package com.example.lifelocator360.FragmentManagement;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

class UISupport {

    static void updateBackground(ArrayList arrayList, TextView textView, ImageView imageView){
        if (arrayList.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
        }
    }
}
