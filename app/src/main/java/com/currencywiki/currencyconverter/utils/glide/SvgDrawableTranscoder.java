package com.currencywiki.currencyconverter.utils.glide;

import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.larvalabs.svgandroid.SVG;

/**
 * Convert the {@link SVG}'s internal representation to an Android-compatible one ({@link Picture}).
 */
public class SvgDrawableTranscoder implements ResourceTranscoder<SVG, PictureDrawable> {
  @Nullable
  @Override
  public Resource<PictureDrawable> transcode(
          @NonNull Resource<SVG> toTranscode, @NonNull Options options) {
    SVG svg = toTranscode.get();
    PictureDrawable drawable = svg.createPictureDrawable();
    return new SimpleResource<>(drawable);
  }
}
