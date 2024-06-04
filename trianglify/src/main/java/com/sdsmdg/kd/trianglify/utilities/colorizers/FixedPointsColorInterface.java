package com.sdsmdg.kd.trianglify.utilities.colorizers;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.sdsmdg.kd.trianglify.models.Palette;
import com.sdsmdg.kd.trianglify.models.Triangulation;
import com.sdsmdg.kd.trianglify.utilities.ExtendedColor;
import com.sdsmdg.kd.trianglify.utilities.Point;
import com.sdsmdg.kd.trianglify.utilities.ThreadLocalRandom;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Triangle2D;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Vector2D;

/**
 * <h1>Fixed Point Colorizer</h1>
 * <b>Description :</b>
 * Fixed point colorizer contains methods that colorize triangles
 * based on the color palette provided in the constructor.
 *
 * @author suyash
 * @since 24/3/17.
 */

public class FixedPointsColorInterface implements ColorInterface {
    private final ThreadLocalRandom random;
    private Triangulation triangulation;
    private Palette colorPalette;

    private final int gridWidth;
    private final int gridHeight;

    private final Boolean randomColoring;

    public Palette getColorPalette() {
        return colorPalette;
    }

    public void setColorPalette(Palette colorPalette) {
        this.colorPalette = colorPalette;
    }

    public Triangulation getTriangulation() {
        return triangulation;
    }

    public void setTriangulation(Triangulation triangulation) {
        this.triangulation = triangulation;
    }

    public FixedPointsColorInterface(Triangulation triangulation, Palette colorPalette,
                                     int gridHeight, int gridWidth) {
        this(triangulation, colorPalette, gridHeight, gridWidth, false);
    }

    public FixedPointsColorInterface(Triangulation triangulation, Palette colorPalette,
                                     int gridHeight, int gridWidth, Boolean randomColoring) {
        this.randomColoring = randomColoring;
        random = new ThreadLocalRandom(System.currentTimeMillis());
        this.triangulation = triangulation;
        this.colorPalette = colorPalette;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    @Override
    public Triangulation getColororedTriangulation() {
        if (triangulation != null) {
            for (Triangle2D triangle : triangulation.triangleList()) {
                triangle.setColor(getColorForPoint(triangle.getCentroid()));
            }
        } else {
            Log.i(TAG, "colorizeTriangulation: Triangulation cannot be null!");
        }
        return getTriangulation();
    }

    /**
     * Returns color corresponding to the point passed in parameter by
     * calculating average of specified by the palette.
     * <p>
     * <p>
     * Relation between palette color and position on rectangle is
     * depicted in the following figure:
     * <p>
     * (c1 are corresponding int values representing color in ColorPalette.java)
     * c0              c1                c2
     * +-------------+--------------+
     * |             |              |
     * |     r1      |      r2      |
     * |             |              |
     * c7 +------------c8--------------+ c3
     * |             |              |
     * |     r3      |      r4      |
     * |             |              |
     * +-------------+--------------+
     * c6              c5                c4
     * <p>
     *
     * <b>Algorithm</b>
     * Grid provided is divided into four regions r1 to r4. Each of the region
     * is considered independent on calculating color for a point.
     * <p>
     * Sub-rectangle in which given point lies has four vertices, denoted by
     * Point topLeft, topRight, bottomLeft and bottomRight. Algorithm then
     * calculates weighted mean of color corresponding to vertices (separately
     * in x-axis and y-axis). Result of this calculation is returned as int.
     *
     * @param point Point to get color for
     * @return Color corresponding to current point
     */

    // Sorry for such long method, here's a ASCII potato
    //          __
    //         /   \
    //        /  o  \
    //       |     o \
    //      / o      |
    //     /    o    |
    //     \______o__/
    //
    private int getColorForPoint(Vector2D point) {
        if (randomColoring) {
            return colorPalette.getColor(random.nextInt(9));
        } else {

            ExtendedColor topLeftColor, topRightColor;
            ExtendedColor bottomLeftColor, bottomRightColor;

            Point topLeft, topRight;
            Point bottomLeft, bottomRight;

            // Following if..else identifies which sub-rectangle given point lies
            if (point.x < (float) gridWidth / 2 && point.y < (float) gridHeight / 2) {
                topLeftColor = new ExtendedColor(colorPalette.getColor(0));
                topRightColor = new ExtendedColor(colorPalette.getColor(1));
                bottomLeftColor = new ExtendedColor(colorPalette.getColor(7));
                bottomRightColor = new ExtendedColor(colorPalette.getColor(8));
            } else if (point.x >= (float) gridWidth / 2 && point.y < (float) gridHeight / 2) {
                topLeftColor = new ExtendedColor(colorPalette.getColor(1));
                topRightColor = new ExtendedColor(colorPalette.getColor(2));
                bottomLeftColor = new ExtendedColor(colorPalette.getColor(8));
                bottomRightColor = new ExtendedColor(colorPalette.getColor(3));
            } else if (point.x >= (float) gridWidth / 2 && point.y >= (float) gridHeight / 2) {
                topLeftColor = new ExtendedColor(colorPalette.getColor(8));
                topRightColor = new ExtendedColor(colorPalette.getColor(3));
                bottomLeftColor = new ExtendedColor(colorPalette.getColor(5));
                bottomRightColor = new ExtendedColor(colorPalette.getColor(4));
            } else {
                topLeftColor = new ExtendedColor(colorPalette.getColor(7));
                topRightColor = new ExtendedColor(colorPalette.getColor(8));
                bottomLeftColor = new ExtendedColor(colorPalette.getColor(6));
                bottomRightColor = new ExtendedColor(colorPalette.getColor(5));
            }

            // Calculate corners of sub rectangle in which point is identified
            topLeft = new Point(
                    (point.x >= (float) gridWidth / 2) ? gridWidth / 2 : 0,
                    (point.y >= (float) gridHeight / 2) ? gridHeight / 2 : 0);
            topRight = new Point(
                    (point.x >= (float) gridWidth / 2) ? gridWidth : gridWidth / 2,
                    (point.y >= (float) gridHeight / 2) ? gridHeight / 2 : 0);
            bottomLeft = new Point(
                    (point.x >= (float) gridWidth / 2) ? gridWidth / 2 : 0,
                    (point.y >= (float) gridHeight / 2) ? gridHeight : gridHeight / 2);
            bottomRight = new Point(
                    (point.x >= (float) gridWidth / 2) ? gridWidth : gridWidth / 2,
                    (point.y >= (float) gridHeight / 2) ? gridHeight : gridHeight / 2);

            // Calculates weighted mean of colors
            ExtendedColor weightedTopColor = new ExtendedColor(
                    (int) ((topRightColor.r * (point.x - topLeft.x) + (topLeftColor.r) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((topRightColor.g * (point.x - topLeft.x) + (topLeftColor.g) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((topRightColor.b * (point.x - topLeft.x) + (topLeftColor.b) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x)))
            );
            ExtendedColor weightedBottomColor = new ExtendedColor(
                    (int) ((bottomRightColor.r * (point.x - topLeft.x) + (bottomLeftColor.r) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((bottomRightColor.g * (point.x - topLeft.x) + (bottomLeftColor.g) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((bottomRightColor.b * (point.x - topLeft.x) + (bottomLeftColor.b) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x)))

            );
            ExtendedColor weightedLeftColor = new ExtendedColor(
                    (int) ((bottomLeftColor.r * (point.y - topLeft.y) + (topLeftColor.r) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y))),
                    (int) ((bottomLeftColor.g * (point.y - topLeft.y) + (topLeftColor.g) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y))),
                    (int) ((bottomLeftColor.b * (point.y - topLeft.y) + (topLeftColor.b) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y)))
            );

            ExtendedColor weightedRightColor = new ExtendedColor(
                    (int) ((bottomRightColor.r * (point.y - topRight.y)
                            + (topRightColor.r) * (bottomRight.y - point.y))
                            / ((bottomRight.y - topRight.y))),
                    (int) ((bottomRightColor.g * (point.y - topRight.y)
                            + (topRightColor.g) * (bottomRight.y - point.y))
                            / ((bottomRight.y - topRight.y))),
                    (int) ((bottomRightColor.b * (point.y - topRight.y)
                            + (topRightColor.b) * (bottomRight.y - point.y))
                            / ((bottomRight.y - topRight.y)))
            );


            ExtendedColor weightedYColor = new ExtendedColor(
                    (int) ((weightedRightColor.r * (point.x - topLeft.x)
                            + (weightedLeftColor.r) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((weightedRightColor.g * (point.x - topLeft.x)
                            + (weightedLeftColor.g) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x))),
                    (int) ((weightedRightColor.b * (point.x - topLeft.x)
                            + (weightedLeftColor.b) * (topRight.x - point.x))
                            / ((topRight.x - topLeft.x)))
            );


            ExtendedColor weightedXColor = new ExtendedColor(
                    (int) ((weightedBottomColor.r * (point.y - topLeft.y)
                            + (weightedTopColor.r) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y))),
                    (int) ((weightedBottomColor.g * (point.y - topLeft.y)
                            + (weightedTopColor.g) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y))),
                    (int) ((weightedBottomColor.b * (point.y - topLeft.y)
                            + (weightedTopColor.b) * (bottomLeft.y - point.y))
                            / ((bottomLeft.y - topLeft.y)))

            );

            return ExtendedColor.avg(weightedXColor, weightedYColor).toInt();
        }
    }
}
