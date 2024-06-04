package com.sdsmdg.kd.trianglify.utilities.triangulator;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * 2D triangle class implementation.
 * 
 * @author Johannes Diemke
 */
public class Triangle2D {

    public Vector2D a;
    public Vector2D b;
    public Vector2D c;
    private int color;

    /**
     * Constructor of the 2D triangle class used to create a new triangle
     * instance from three 2D vectors describing the triangle's vertices.
     * 
     * @param a
     *            The first vertex of the triangle
     * @param b
     *            The second vertex of the triangle
     * @param c
     *            The third vertex of the triangle
     */
    public Triangle2D(Vector2D a, Vector2D b, Vector2D c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Tests if a 2D point lies inside this 2D triangle. See Real-Time Collision
     * Detection, chap. 5, p. 206.
     * 
     * @param point
     *            The point to be tested
     * @return Returns true iff the point lies inside this 2D triangle
     */
    public boolean contains(Vector2D point) {
        final double pab = point.sub(a).cross(b.sub(a));
        final double pbc = point.sub(b).cross(c.sub(b));

        if (!hasSameSign(pab, pbc)) {
            return false;
        }

        double pca = point.sub(c).cross(a.sub(c));

        return hasSameSign(pab, pca);
    }

    /**
     * Tests if a given point lies in the circumcircle of this triangle. Let the
     * triangle ABC appear in counterclockwise (CCW) order. Then when det &gt;
     * 0, the point lies inside the circumcircle through the three points a, b
     * and c. If instead det &lt; 0, the point lies outside the circumcircle.
     * When det = 0, the four points are cocircular. If the triangle is oriented
     * clockwise (CW) the result is reversed. See Real-Time Collision Detection,
     * chap. 3, p. 34.
     * 
     * @param point
     *            The point to be tested
     * @return Returns true iff the point lies inside the circumcircle through
     *         the three points a, b, and c of the triangle
     */
    public boolean isPointInCircumcircle(Vector2D point) {
        final double a11 = a.x - point.x;
        final double a21 = b.x - point.x;
        final double a31 = c.x - point.x;

        final double a12 = a.y - point.y;
        final double a22 = b.y - point.y;
        final double a32 = c.y - point.y;

        final double a13 = (a.x - point.x) * (a.x - point.x) + (a.y - point.y) * (a.y - point.y);
        final double a23 = (b.x - point.x) * (b.x - point.x) + (b.y - point.y) * (b.y - point.y);
        final double a33 = (c.x - point.x) * (c.x - point.x) + (c.y - point.y) * (c.y - point.y);

        final double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33
                - a11 * a23 * a32;

        if (isOrientedCCW()) {
            return det > 0.0d;
        }

        return det < 0.0d;
    }

    /**
     * Test if this triangle is oriented counterclockwise (CCW). Let A, B and C
     * be three 2D points. If det &gt; 0, C lies to the left of the directed
     * line AB. Equivalently the triangle ABC is oriented counterclockwise. When
     * det &lt; 0, C lies to the right of the directed line AB, and the triangle
     * ABC is oriented clockwise. When det = 0, the three points are colinear.
     * See Real-Time Collision Detection, chap. 3, p. 32
     * 
     * @return Returns true iff the triangle ABC is oriented counterclockwise
     *         (CCW)
     */
    public boolean isOrientedCCW() {
        final double a11 = a.x - c.x;
        final double a21 = b.x - c.x;

        final double a12 = a.y - c.y;
        final double a22 = b.y - c.y;

        final double det = a11 * a22 - a12 * a21;

        return det > 0.0d;
    }

    /**
     * Returns true if this triangle contains the given edge.
     * 
     * @param edge
     *            The edge to be tested
     * @return Returns true if this triangle contains the edge
     */
    public boolean isNeighbour(Edge2D edge) {
        return (a == edge.a || b == edge.a || c == edge.a) && (a == edge.b || b == edge.b || c == edge.b);
    }

    /**
     * Returns the vertex of this triangle that is not part of the given edge.
     * 
     * @param edge
     *            The edge
     * @return The vertex of this triangle that is not part of the edge
     */
    public Vector2D getNoneEdgeVertex(Edge2D edge) {
        if (a != edge.a && a != edge.b) {
            return a;
        } else if (b != edge.a && b != edge.b) {
            return b;
        } else if (c != edge.a && c != edge.b) {
            return c;
        }

        return null;
    }

    /**
     * Returns true if the given vertex is one of the vertices describing this
     * triangle.
     * 
     * @param vertex
     *            The vertex to be tested
     * @return Returns true if the Vertex is one of the vertices describing this
     *         triangle
     */
    public boolean hasVertex(Vector2D vertex) {
        return a == vertex || b == vertex || c == vertex;
    }

    /**
     * Returns an EdgeDistancePack containing the edge and its distance nearest
     * to the specified point.
     * 
     * @param point
     *            The point the nearest edge is queried for
     * @return The edge of this triangle that is nearest to the specified point
     */
    public EdgeDistancePack findNearestEdge(Vector2D point) {
        EdgeDistancePack[] edges = new EdgeDistancePack[3];

        edges[0] = new EdgeDistancePack(new Edge2D(a, b),
                computeClosestPoint(new Edge2D(a, b), point).sub(point).mag());
        edges[1] = new EdgeDistancePack(new Edge2D(b, c),
                computeClosestPoint(new Edge2D(b, c), point).sub(point).mag());
        edges[2] = new EdgeDistancePack(new Edge2D(c, a),
                computeClosestPoint(new Edge2D(c, a), point).sub(point).mag());

        Arrays.sort(edges);
        return edges[0];
    }

    /**
     * Computes the closest point on the given edge to the specified point.
     * 
     * @param edge
     *            The edge on which we search the closest point to the specified
     *            point
     * @param point
     *            The point to which we search the closest point on the edge
     * @return The closest point on the given edge to the specified point
     */
    private Vector2D computeClosestPoint(Edge2D edge, Vector2D point) {
        final Vector2D ab = edge.b.sub(edge.a);
        float t = point.sub(edge.a).dot(ab) / ab.dot(ab);

        if (t < 0.0f) {
            t = 0.0f;
        } else if (t > 1.0f) {
            t = 1.0f;
        }

        return edge.a.add(ab.mult(t));
    }

    /**
     * Tests if the two arguments have the same sign.
     * 
     * @param a
     *            The first floating point argument
     * @param b
     *            The second floating point argument
     * @return Returns true iff both arguments have the same sign
     */
    private boolean hasSameSign(double a, double b) {
        return Math.signum(a) == Math.signum(b);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @NonNull
    @Override
    public String toString() {
        return "Triangle2D[" + a + ", " + b + ", " + c + "]";
    }

    public Vector2D getCentroid() {
        Vector2D centroid = new Vector2D(0,0);
        centroid.x = ((a.x) + (b.x) + (c.x))/3;
        centroid.y = ((a.y) + (b.y) + (c.y))/3;
        return centroid;
    }
}