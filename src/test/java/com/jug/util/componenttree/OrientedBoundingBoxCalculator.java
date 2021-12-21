package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;

/*	Fits a minimum area rectangle into a ROI.
 *
 * 	It searches the for the minimum area bounding rectangles among the ones that have a side
 * 	which is collinear with an edge of the convex hull.
 *
 * 	Concept based on:
 * 	H. Freeman and R. Shapira. 1975. Determining the minimum-area encasing rectangle for an arbitrary
 * 	closed curve. Commun. ACM 18, 7 (July 1975), 409–413. DOI:https://doi.org/10.1145/360881.360919
 *
 * 	Many thanks to @mountain_man (https://forum.image.sc/u/mountain_man) for helping me correct
 * 	my initial (wrong) intuition about the right way to tackle this problem!
 *
 *  Code was adapted from: https://raw.githubusercontent.com/ndefrancesco/macro-frenzy/master/geometry/fitting/fitMinRectangle.ijm
 */

public class OrientedBoundingBoxCalculator {
    private OpService ops;

    public OrientedBoundingBoxCalculator(OpService ops) {
        this.ops = ops;
    }

    public Polygon2D calculate(AdvancedComponent<FloatType> component){
        return getOrientedRectangleWithMinimalArea(component);
    }

    public Polygon2D getOrientedRectangleWithMinimalArea(AdvancedComponent<FloatType> component){
        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);
        List<Double> xList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(0)).collect(Collectors.toList());
        List<Double> yList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(1)).collect(Collectors.toList());
        double[] x = ArrayUtils.toPrimitive(xList.toArray(new Double[0]));
        double[] y = ArrayUtils.toPrimitive(yList.toArray(new Double[0]));

        ValuePair<double[], double[]> res = getOrientedBoundingBoxCoordinates(x, y);
        Polygon2D orientedBoundingBoxPoly = GeomMasks.polygon2D(res.getA(), res.getB());
        return orientedBoundingBoxPoly;
    }

    public ValuePair<double[], double[]> getOrientedBoundingBoxCoordinates(double[] xp, double[] yp){
        int np = xp.length;

        double minArea = Double.MAX_VALUE;
        int imin = -1;
        int i2min = -1;
        int jmin = -1;

        double min_hmin = 0.0;
        double min_hmax = 0.0;

        for (int i = 0; i < np; i++) {
            double maxLD = 0.0;
            int imax = -1;
            int i2max = -1;
            int jmax = -1;
            int i2;
            if(i<np-1) i2 = i + 1; else i2 = 0;

            for (int j = 0; j < np; j++) {
                double d = Math.abs(perpDist(xp[i], yp[i], xp[i2], yp[i2], xp[j], yp[j]));
                if (maxLD < d) {
                    maxLD = d;
                    imax = i;
                    jmax = j;
                    i2max = i2;
                }
            }

            double hmin = 0.0;
            double hmax = 0.0;

            for (int k = 0; k < np; k++) { // rotating calipers
                double hd = parDist(xp[imax], yp[imax], xp[i2max], yp[i2max], xp[k], yp[k]);
                hmin = (hmin <= hd) ? hmin : hd;
                hmax = (hmax >= hd) ? hmax : hd;
            }


            double area = maxLD * (hmax - hmin);

            if (minArea > area){

                minArea = area;
                min_hmin = hmin;
                min_hmax = hmax;

                imin = imax;
                i2min = i2max;
                jmin = jmax;
            }
        }

        double pd = perpDist(xp[imin], yp[imin], xp[i2min], yp[i2min], xp[jmin], yp[jmin]); // signed feret diameter
        double pairAngle = Math.atan2(yp[i2min] - yp[imin], xp[i2min] - xp[imin]);
        double minAngle = pairAngle + Math.PI / 2;

        double[] nxp = new double[4];
        double[] nyp = new double[4];

        nxp[0] = xp[imin] + Math.cos(pairAngle) * min_hmax;
        nyp[0] = yp[imin] + Math.sin(pairAngle) * min_hmax;

        nxp[1] = nxp[0] + Math.cos(minAngle) * pd;
        nyp[1] = nyp[0] + Math.sin(minAngle) * pd;

        nxp[2] = nxp[1] + Math.cos(pairAngle) * (min_hmin - min_hmax);
        nyp[2] = nyp[1] + Math.sin(pairAngle) * (min_hmin - min_hmax);

        nxp[3] = nxp[2] + Math.cos(minAngle) * - pd;
        nyp[3] = nyp[2] + Math.sin(minAngle) * - pd;

        return new ValuePair<>(nxp, nyp);
    }

    private double dist2(double x1, double y1, double x2, double y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    private double perpDist(double p1x, double p1y, double p2x, double p2y, double x, double y){
        // signed distance from a point (x,y) to a line passing through p1 and p2
        return ((p2x - p1x)*(y - p1y) - (x - p1x)*(p2y - p1y))/Math.sqrt(dist2(p1x, p1y, p2x, p2y));
    }

    private double parDist(double p1x, double p1y, double p2x, double p2y, double x, double y){
        // signed projection of vector (x,y)-p1 into a line passing through p1 and p2
        return ((p2x - p1x)*(x - p1x) + (y - p1y)*(p2y - p1y))/Math.sqrt(dist2(p1x, p1y, p2x, p2y));
    }
}


