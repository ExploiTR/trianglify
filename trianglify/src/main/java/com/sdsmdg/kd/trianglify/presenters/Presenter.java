package com.sdsmdg.kd.trianglify.presenters;

import android.os.Handler;
import android.os.Looper;

import com.sdsmdg.kd.trianglify.models.Triangulation;
import com.sdsmdg.kd.trianglify.utilities.Utilities;
import com.sdsmdg.kd.trianglify.utilities.colorizers.ColorInterface;
import com.sdsmdg.kd.trianglify.utilities.colorizers.FixedPointsColorInterface;
import com.sdsmdg.kd.trianglify.utilities.patterns.Circle;
import com.sdsmdg.kd.trianglify.utilities.patterns.Patterns;
import com.sdsmdg.kd.trianglify.utilities.patterns.Rectangle;
import com.sdsmdg.kd.trianglify.utilities.patterns.Triangle;
import com.sdsmdg.kd.trianglify.utilities.triangulator.DelaunayTriangulator;
import com.sdsmdg.kd.trianglify.utilities.triangulator.NotEnoughPointsException;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Vector2D;
import com.sdsmdg.kd.trianglify.views.TrianglifyView;
import com.sdsmdg.kd.trianglify.views.TrianglifyViewInterface;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <h1>Presenter.java</h1>
 * <b>Description :</b>
 * P of MVP implemented to present data generated using models
 * to a view.
 * <p>
 * ...
 */

public class Presenter {
    private final TrianglifyViewInterface view;
    private Triangulation triangulation;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ViewState viewState = ViewState.NULL_TRIANGULATION;

    public enum ViewState {
        NULL_TRIANGULATION,
        UNCHANGED_TRIANGULATION,
        PAINT_STYLE_CHANGED,
        COLOR_SCHEME_CHANGED,
        GRID_PARAMETERS_CHANGED
    }

    private boolean generateOnlyColor;

    public Presenter(TrianglifyViewInterface view) {
        this.view = view;
    }

    public void setGenerateOnlyColor(boolean generateOnlyColor) {
        this.generateOnlyColor = generateOnlyColor;
    }

    public void updateView() {
        viewState = view.getViewState();
        if (viewState == ViewState.PAINT_STYLE_CHANGED || viewState == ViewState.UNCHANGED_TRIANGULATION) {
            view.invalidateView(triangulation);
        } else if (viewState == ViewState.COLOR_SCHEME_CHANGED) {
            generateNewColoredSoupAndInvalidate();
        } else if (viewState == ViewState.GRID_PARAMETERS_CHANGED || viewState == ViewState.NULL_TRIANGULATION) {
            generateOnlyColor = false;
            generateSoupAndInvalidateView();
        }
    }

    private void generateNewColoredSoupAndInvalidate() {
        setGenerateOnlyColor(true);
        generateSoupAndInvalidateView();
    }

    private List<Vector2D> generateGrid() {
        int gridType = view.getTypeGrid();
        Patterns patterns;

        if (gridType == TrianglifyViewInterface.GRID_CIRCLE)
            patterns = new Circle(
                    view.getBleedX(), view.getBleedY(), 8, view.getGridHeight(),
                    view.getGridWidth(), view.getCellSize(), view.getVariance());
        else if (gridType == TrianglifyViewInterface.GRID_TRIANGLE)
            patterns = new Triangle(
                    view.getBleedX() + Utilities.dpToPx(250, ((TrianglifyView) this.view).getContext()),
                    view.getBleedY() + Utilities.dpToPx(250, ((TrianglifyView) this.view).getContext()), view.getGridHeight(),
                    view.getGridWidth(), view.getCellSize(), view.getVariance());
        else
            patterns = new Rectangle(
                    view.getBleedX(), view.getBleedY(), view.getGridHeight(),
                    view.getGridWidth(), view.getCellSize(), view.getVariance());

        return patterns.generate();
    }

    private Triangulation getSoup() throws NotEnoughPointsException {
        if (generateOnlyColor) {
            triangulation = generateColoredSoup(triangulation);
        } else {
            generateSoup();
        }
        return triangulation;
    }

    private void generateSoup() throws NotEnoughPointsException {
        triangulation = generateTriangulation(generateGrid());
        triangulation = generateColoredSoup(triangulation);
    }

    private Triangulation generateTriangulation(List<Vector2D> inputGrid) throws NotEnoughPointsException {
        DelaunayTriangulator triangulator = new DelaunayTriangulator(inputGrid);
        triangulator.triangulate();
        return new Triangulation(triangulator.getTriangles());
    }

    private Triangulation generateColoredSoup(Triangulation inputTriangulation) {
        ColorInterface colorInterface = new FixedPointsColorInterface(inputTriangulation,
                view.getPalette(), view.getGridHeight() + 2 * view.getBleedY(),
                view.getGridWidth() + 2 * view.getBleedX(), view.isRandomColoringEnabled());
        return colorInterface.getColororedTriangulation();
    }

    public void clearSoup() {
        triangulation = null;
        viewState = ViewState.NULL_TRIANGULATION;
    }

    //Have to rely on Firebase's internal logging system i guess. No way to get this outta here
    //anyways lets not use AsyncTask
    public void generateSoupAndInvalidateView() {
        Future<Triangulation> future = executorService.submit(() -> {
            try {
                return getSoup();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Triangulation triangulation = future.get();
                if (triangulation != null) {
                    view.invalidateView(triangulation);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
