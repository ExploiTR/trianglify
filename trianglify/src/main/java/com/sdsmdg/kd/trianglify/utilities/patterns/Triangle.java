package com.sdsmdg.kd.trianglify.utilities.patterns;

import com.sdsmdg.kd.trianglify.utilities.ThreadLocalRandom;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://music.youtube.com/watch?v=yiJ5k2vBtlw">Click this link to see documentation</a>
 */
public class Triangle implements Patterns {
    private final ThreadLocalRandom random;
    private final int bleedX;
    private final int bleedY;

    private final int height;
    private final int width;

    private final int cellSize;
    private final int variance;

    List<Vector2D> grid;

    public Triangle(int bleedX, int bleedY, int height, int width, int cellSize, int variance) {
        this.bleedX = bleedX;
        this.bleedY = bleedY;

        this.variance = variance;
        this.cellSize = 2 * cellSize;//not gonna say why

        this.height = height;
        this.width = width;

        random = new ThreadLocalRandom();

        grid = new ArrayList<>();
    }

    /**
     * Generates array of points arranged in a triangular grid with deviation from their positions
     * on the basis of bleed value.
     *
     * @return List of Vector2D containing points that resembles a triangular grid
     */
    @Override
    public List<Vector2D> generate() {
        grid.clear();

        int numRows = (height + 2 * bleedY) / cellSize;
        int numCols = (width + 2 * bleedX) / cellSize;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                float x = col * cellSize + (row % 2 == 0 ? 0 : (float) cellSize / 2) + random.nextInt(variance);
                float y = row * (float) Math.sqrt(3) / 2 * cellSize + random.nextInt(variance);

                grid.add(new Vector2D(x, y));
            }
        }

        return grid;
    }
}

