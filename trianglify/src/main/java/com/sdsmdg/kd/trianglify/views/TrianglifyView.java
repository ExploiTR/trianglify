package com.sdsmdg.kd.trianglify.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.sdsmdg.kd.trianglify.R;
import com.sdsmdg.kd.trianglify.models.Palette;
import com.sdsmdg.kd.trianglify.models.Triangulation;
import com.sdsmdg.kd.trianglify.presenters.Presenter;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Triangle2D;

/**
 * @noinspection UnusedReturnValue, unused
 */
public class TrianglifyView extends View implements TrianglifyViewInterface {
    private int bleedX;
    private int bleedY;
    private int gridHeight;
    private int gridWidth;
    private int typeGrid;
    private int variance;
    private int cellSize;
    private boolean fillTriangle;
    private boolean drawStroke;
    private boolean randomColoring;
    private Palette palette;
    private Triangulation triangulation;
    private final Presenter presenter;
    private int bitmapQuality;
    private boolean fillViewCompletely;

    public TrianglifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TrianglifyView, 0, 0);
        attributeSetter(a);
        this.presenter = new Presenter(this);
        this.setDrawingCacheEnabled(true);
        this.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        setGridWidth(w);
        setGridHeight(h);
        smartUpdate();
    }

    private void attributeSetter(TypedArray typedArray) {
        bleedX = (int) typedArray.getDimension(R.styleable.TrianglifyView_bleedX, 0);
        bleedY = (int) typedArray.getDimension(R.styleable.TrianglifyView_bleedY, 0);
        variance = (int) typedArray.getDimension(R.styleable.TrianglifyView_variance, 10);
        cellSize = (int) typedArray.getDimension(R.styleable.TrianglifyView_cellSize, 40);
        typeGrid = typedArray.getInt(R.styleable.TrianglifyView_gridType, 0);
        fillTriangle = typedArray.getBoolean(R.styleable.TrianglifyView_fillTriangle, true);
        drawStroke = typedArray.getBoolean(R.styleable.TrianglifyView_fillStrokes, false);

        palette = Palette.getPalette(typedArray.getInt(R.styleable.TrianglifyView_palette, 0));

        randomColoring = typedArray.getBoolean(R.styleable.TrianglifyView_randomColoring, false);
        fillViewCompletely = typedArray.getBoolean(R.styleable.TrianglifyView_fillViewCompletely, false);
        typedArray.recycle();

        if (fillViewCompletely) {
            checkViewFilledCompletely();
        }
    }

    @Override
    public int getBleedX() {
        return bleedX;
    }

    public TrianglifyView setBleedX(int bleedX) {
        this.bleedX = bleedX;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) {
            checkViewFilledCompletely();
        }
        return this;
    }

    @Override
    public int getBleedY() {
        return bleedY;
    }

    public TrianglifyView setBleedY(int bleedY) {
        this.bleedY = bleedY;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) {
            checkViewFilledCompletely();
        }
        return this;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    public TrianglifyView setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        return this;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    public TrianglifyView setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        return this;
    }

    @Override
    public int getBitmapQuality() {
        return bitmapQuality;
    }

    public void setBitmapQuality(int bitmapQuality) {
        this.bitmapQuality = bitmapQuality;
        setDrawingCacheQuality(bitmapQuality);
    }

    @Override
    public int getTypeGrid() {
        return typeGrid;
    }

    public TrianglifyView setTypeGrid(int typeGrid) {
        this.typeGrid = typeGrid;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        return this;
    }

    @Override
    public int getVariance() {
        return variance;
    }

    public TrianglifyView setVariance(int variance) {
        this.variance = variance;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        return this;
    }

    @Override
    public int getCellSize() {
        return cellSize;
    }

    public TrianglifyView setCellSize(int cellSize) {
        this.cellSize = cellSize;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) {
            checkViewFilledCompletely();
        }
        return this;
    }

    public TrianglifyView setFillViewCompletely(boolean fillViewCompletely) {
        this.fillViewCompletely = fillViewCompletely;
        if (fillViewCompletely) {
            checkViewFilledCompletely();
        }
        return this;
    }

    @Override
    public boolean isFillViewCompletely() {
        return fillViewCompletely;
    }

    @Override
    public boolean isFillTriangle() {
        return fillTriangle;
    }

    public TrianglifyView setFillTriangle(boolean fillTriangle) {
        this.fillTriangle = fillTriangle;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED && presenter.viewState != Presenter.ViewState.COLOR_SCHEME_CHANGED) {
            presenter.viewState = Presenter.ViewState.PAINT_STYLE_CHANGED;
        }
        return this;
    }

    @Override
    public boolean isDrawStrokeEnabled() {
        return drawStroke;
    }

    public TrianglifyView setDrawStrokeEnabled(boolean drawStroke) {
        this.drawStroke = drawStroke;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED && presenter.viewState != Presenter.ViewState.COLOR_SCHEME_CHANGED) {
            presenter.viewState = Presenter.ViewState.PAINT_STYLE_CHANGED;
        }
        return this;
    }

    @Override
    public boolean isRandomColoringEnabled() {
        return randomColoring;
    }

    public TrianglifyView setRandomColoring(boolean randomColoring) {
        this.randomColoring = randomColoring;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED) {
            presenter.viewState = Presenter.ViewState.COLOR_SCHEME_CHANGED;
        }
        return this;
    }

    @Override
    public Palette getPalette() {
        return palette;
    }

    public TrianglifyView setPalette(Palette palette) {
        this.palette = palette;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED) {
            presenter.viewState = Presenter.ViewState.COLOR_SCHEME_CHANGED;
        }
        return this;
    }

    @Override
    public Presenter.ViewState getViewState() {
        return presenter.viewState;
    }

    private TrianglifyView setTriangulation(Triangulation triangulation) {
        this.triangulation = triangulation;
        return this;
    }

    public void clearView() {
        presenter.clearSoup();
    }

    @Override
    public void invalidateView(Triangulation triangulation) {
        this.setTriangulation(triangulation);
        invalidate();
        presenter.viewState = Presenter.ViewState.UNCHANGED_TRIANGULATION;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        gridHeight = getHeight();
        gridWidth = getWidth();
        if (triangulation != null) {
            plotOnCanvas(canvas);
        } else {
            generateAndInvalidate();
        }
    }

    public void smartUpdate() {
        presenter.updateView();
    }

    public void generateAndInvalidate() {
        presenter.setGenerateOnlyColor(false);
        presenter.generateSoupAndInvalidateView();
    }

    private void plotOnCanvas(Canvas canvas) {
        for (Triangle2D triangle : triangulation.getTriangleList()) {
            drawTriangle(canvas, triangle);
        }
    }

    private void drawTriangle(Canvas canvas, Triangle2D triangle2D) {
        Paint paint = getPaint(triangle2D);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(triangle2D.a.x - bleedX, triangle2D.a.y - bleedY);
        path.lineTo(triangle2D.b.x - bleedX, triangle2D.b.y - bleedY);
        path.lineTo(triangle2D.c.x - bleedX, triangle2D.c.y - bleedY);
        path.close();

        canvas.drawPath(path, paint);
    }

    @NonNull
    private Paint getPaint(Triangle2D triangle2D) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int color = triangle2D.getColor();
        color += 0xff000000;
        paint.setColor(color);
        paint.setStrokeWidth(1);
        paint.setStyle(getPaintStyle());
        paint.setAntiAlias(true);
        return paint;
    }

    private Paint.Style getPaintStyle() {
        if (isFillTriangle() && isDrawStrokeEnabled()) {
            return Paint.Style.FILL_AND_STROKE;
        } else if (isFillTriangle()) {
            return Paint.Style.FILL;
        } else {
            return Paint.Style.STROKE;
        }
    }

    private void checkViewFilledCompletely() {
        if (bleedY <= cellSize || bleedX <= cellSize) {
            throw new IllegalArgumentException("bleedY and bleedX should be larger than cellSize for view to be completely filled.");
        }
    }

    public Bitmap getBitmap() {
        Bitmap resultBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
        this.destroyDrawingCache();
        return resultBitmap;
    }
}
