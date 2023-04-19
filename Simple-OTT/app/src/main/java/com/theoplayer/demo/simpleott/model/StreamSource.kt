package com.theoplayer.demo.simpleott.model;

import androidx.annotation.DrawableRes;

/**
 * Stream source definition.
 */
public class StreamSource {

    private String title;
    private String description;
    private String source;
    @DrawableRes
    private int imageResId;

    StreamSource(String title, String description, String source, @DrawableRes int imageResId) {
        this.title = title;
        this.description = description;
        this.source = source;
        this.imageResId = imageResId;
    }

    /**
     * Returns stream source title.
     *
     * @return the title of stream source.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns stream source description.
     *
     * @return the description of stream source.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns stream source content URL.
     *
     * @return the source URL of stream source.
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns stream source image drawable resource identifier.
     *
     * @return the drawable resource identifier of stream source image.
     */
    public int getImageResId() {
        return imageResId;
    }

}
