package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

public interface ComponentInterface<T extends Type<T>, C extends Component<T, C>> extends Component<T, C> {
    double[] firstMomentPixelCoordinates();

    Double getWatershedLinePixelValueAverage();

    LabelRegion<Integer> getRegion();

    <T extends NativeType<T>> Img<T> getComponentImage(T pixelValue);
}
