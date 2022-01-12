package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

public class SpineCalculator {
    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour, int pointsToAverage) {
        if (medialLine.size() < pointsToAverage) {
            pointsToAverage = 1;
        }
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();
        LinkedItem<Vector2D> linkedMedialLine = medialLine.toCircularLinkedList();

        List<Vector2D> diffs = GeomUtils.differences(medialLine.getVectorList());
        List<Vector2D> diffsAtStart = diffs.stream().skip(0).limit(pointsToAverage).collect(Collectors.toList());

//        medialLine.getVectorList().stream()
//        List<Vector2D> res = medialLine.getVectorList().stream().skip(0).limit(pointsToAverage).collect(Collectors.toList());
        Vector2D avgDirectionAtStart = GeomUtils.averageVectors(diffsAtStart).multiply(-1.0);

        ValuePair<Vector2D, Vector2D> interceptLinePoints = GeomUtils.getPointsOfInterceptingContourSegment(medialLine.get(0), avgDirectionAtStart, linkedContour);

//        medialLine.getVectorList().stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage) // do this at end of contour

        throw new NotImplementedException();
    }

    Vector2D calculateInterceptWithCounter(Vector2D startingPoint, Vector2D direction, LinkedItem<Vector2D> linkedContour) {

        throw new NotImplementedException();
    }

//    List<Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D direction, Vector2DPolyline contour){
//        double targetAngle = direction.getPolarAngle();
//
////        List<Double> angles = new ArrayList();
//        List<Vector2D> vectList = contour.getVectorList();
//        int nrVects = vectList.size();
//        int index1 = 0;
//        int index2 = nrVects;
//        while (true) {
////            Vector2D vect = vectList.get(index);
//            double angle1 = vectList.get(index1).minus(startingPoint).getPolarAngle();
//            double absDiff1 = Math.abs(targetAngle - angle1);
//            double angle2 = vectList.get(index2).minus(startingPoint).getPolarAngle();
//            double absDiff2 = Math.abs(targetAngle - angle2);
//            if (absDiff1 <= absDiff2) {
//                absDiff1
//            }
////            angles.add(vect.getPolarAngle());
//            break;
//        }
////        Object res = contour.getVectorList().stream().forEach(vect -> vect.getPolarAngle()).collect(Collectors.toList());
//
//        throw new NotImplementedException();
//    }

//
//    Vector2D (LinkedItem<Vector2D> linkedMedialLine, LinkedItem<Vector2D> linkedContour){
//
//    }
}
