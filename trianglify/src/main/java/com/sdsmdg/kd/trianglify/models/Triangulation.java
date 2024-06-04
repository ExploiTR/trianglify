package com.sdsmdg.kd.trianglify.models;

import com.sdsmdg.kd.trianglify.utilities.triangulator.Triangle2D;

import java.util.List;

/**
 * Created by shyam on 12-Mar-17.
 */

public record Triangulation(List<Triangle2D> triangleList) {
}
