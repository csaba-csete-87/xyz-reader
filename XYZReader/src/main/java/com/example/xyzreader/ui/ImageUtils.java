package com.example.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.xyzreader.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by Csabi on 16-Jan-16.
 */
public class ImageUtils {

    public static ImageLoaderConfiguration getImageLoaderConfig(Context c) {
        return new ImageLoaderConfiguration.Builder(c)
                .threadPoolSize(4)
                .threadPriority(Thread.MAX_PRIORITY)
                .imageDownloader(new AuthImageDownloader(c, 10, 5))
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(ImageUtils.getDefaultDisplayImageOptions())
                .build();
    }

    public static DisplayImageOptions getDefaultDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public static DisplayImageOptions getRoundedDisplayOptions(Context c) {
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(c.getResources().getDimensionPixelSize(R.dimen.rounded_corner_radius)))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public static DisplayImageOptions getCircleDisplayOptions() {
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

}
