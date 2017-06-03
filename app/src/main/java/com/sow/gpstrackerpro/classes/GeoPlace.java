package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by renato on 21/02/2017.
 */

@Keep
public class GeoPlace {

    public Fence fence;
    public String name;
    public Circle circle;
    public CircleOptions circleOptions;
    public Marker marker;

    public GeoPlace(Fence fence, String name, Circle circle, CircleOptions circleOptions, Marker marker) {
        this.fence = fence;
        this.name = name;
        this.circle = circle;
        this.circleOptions = circleOptions;
        this.marker = marker;
    }

    public GeoPlace() {
    }

    public Fence getFence() {
        return fence;
    }

    public void setFence(Fence fence) {
        this.fence = fence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public CircleOptions getCircleOptions() {
        return circleOptions;
    }

    public void setCircleOptions(CircleOptions circleOptions) {
        this.circleOptions = circleOptions;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
