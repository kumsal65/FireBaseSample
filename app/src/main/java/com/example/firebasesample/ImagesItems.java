package com.example.firebasesample;

import android.widget.Adapter;

import androidx.recyclerview.widget.RecyclerView;

public class ImagesItems {
     private String ImageName,ImageURL;
    public ImagesItems(String imageName, String imageURL) {
        ImageName = imageName;
        ImageURL = imageURL;
    }
    public String ImageNameGetter(String ImageName){
        return ImageName;
    }
    public void ImageNameSetter(String ImageName){
        this.ImageName=ImageName;
    }
    public String ImagesURLgetter(String ImageURL){
        return ImageURL;
    }
    public void ImageURLsetter(String ImagesURL){
        this.ImageURL=ImagesURL;
    }
}
