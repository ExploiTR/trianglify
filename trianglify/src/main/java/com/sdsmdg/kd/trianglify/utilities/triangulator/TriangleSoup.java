package com.sdsmdg.kd.trianglify.utilities.triangulator;

import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Triangle soup class implementation.
 *
 * @author Johannes Diemke
 * <p>
 * Documentation might be outdated.
 */
class TriangleSoup {

    private final HashSet<Triangle2D> triangleSoup;

    /**
     * Constructor of the triangle soup class used to create a new triangle soup
     * instance.
     */
    public TriangleSoup() {
        this.triangleSoup = new HashSet<>();
    }

    /**
     * Adds a triangle to this triangle soup.
     *
     * @param triangle The triangle to be added to this triangle soup
     */
    public void add(Triangle2D triangle) {
        this.triangleSoup.add(triangle);
    }

    /**
     * Removes a triangle from this triangle soup.
     *
     * @param triangle The triangle to be removed from this triangle soup
     */
    public void remove(Triangle2D triangle) {
        this.triangleSoup.remove(triangle);
    }

    /**
     * Returns the triangles from this triangle soup.
     *
     * @return The triangles from this triangle soup
     */
    public List<Triangle2D> getTriangles() {
        return new ArrayList<>(this.triangleSoup);
    }

    /**
     * Returns the triangle from this triangle soup that contains the specified
     * point or null if no triangle from the triangle soup contains the point.
     *
     * @param point The point
     * @return Returns the triangle from this triangle soup that contains the
     * specified point or null
     */
    public Triangle2D findContainingTriangle(Vector2D point) {
        for (Triangle2D triangle : triangleSoup) {
            if (triangle.contains(point)) {
                return triangle;
            }
        }
        return null;
    }

    /**
     * Returns the neighbor triangle of the specified triangle sharing the same
     * edge as specified. If no neighbor sharing the same edge exists null is
     * returned.
     *
     * @param triangle The triangle
     * @param edge     The edge
     * @return The triangles neighbor triangle sharing the same edge or null if
     * no triangle exists
     */
    public Triangle2D findNeighbour(Triangle2D triangle, Edge2D edge) {
        for (Triangle2D triangleFromSoup : triangleSoup) {
            if (triangleFromSoup.isNeighbour(edge) && triangleFromSoup != triangle) {
                return triangleFromSoup;
            }
        }
        return null;
    }

    /**
     * Returns one of the possible triangles sharing the specified edge. Based
     * on the ordering of the triangles in this triangle soup the returned
     * triangle may differ. To find the other triangle that shares this edge use
     * the findNeighbour method.
     *
     * @param edge The edge
     * @return Returns one triangle that shares the specified edge
     */
    public Triangle2D findOneTriangleSharing(Edge2D edge) {
        for (Triangle2D triangle : triangleSoup) {
            if (triangle.isNeighbour(edge)) {
                return triangle;
            }
        }
        return null;
    }

    /**
     * Returns the edge from the triangle soup nearest to the specified point.
     *
     * @param point The point
     * @return The edge from the triangle soup nearest to the specified point
     */
    public Edge2D findNearestEdge(Vector2D point) {
        List<EdgeDistancePack> edgeList = new ArrayList<>();

        for (Triangle2D triangle : triangleSoup) {
            edgeList.add(triangle.findNearestEdge(point));
        }

        EdgeDistancePack[] edgeDistancePacks = new EdgeDistancePack[edgeList.size()];
        edgeList.toArray(edgeDistancePacks);

        Arrays.sort(edgeDistancePacks);
        return edgeDistancePacks[0].edge;
    }

    /**
     * Removes all triangles from this triangle soup that contain the specified
     * vertex.
     *
     * @param vertex The vertex
     */
    public void removeTrianglesUsing(Vector2D vertex) {
        List<Triangle2D> trianglesToBeRemoved = new ArrayList<>();

        for (Triangle2D triangle : triangleSoup) {
            if (triangle.hasVertex(vertex)) {
                trianglesToBeRemoved.add(triangle);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            trianglesToBeRemoved.forEach(triangleSoup::remove);
        else
            triangleSoup.removeAll(trianglesToBeRemoved); //what is this?
    }

}