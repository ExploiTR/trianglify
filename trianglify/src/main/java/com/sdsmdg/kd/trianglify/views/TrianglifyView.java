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
import com.sdsmdg.kd.trianglify.utilities.BitmapUtils;
import com.sdsmdg.kd.trianglify.utilities.triangulator.Triangle2D;

import java.security.SecureRandom;

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

    /*changes*/
    private float strokeSizeF = 2.5f;

    //no risk taken

    private Paint reusePaint;
    private Paint reusePaint_RFS;
    private final Path reusePath;
    private final SecureRandom random = new SecureRandom();
    private boolean shouldUpdate = false;
    private boolean isRandomizeFillStrokeEnabled = false;
    private int randomStaticFillColor = 0x00000000;
    private boolean externalPaintEnabled = false;

    public TrianglifyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TrianglifyView, 0, 0);
        attributeSetter(a);
        this.presenter = new Presenter(this);

        reusePath = new Path();
        reusePath.setFillType(Path.FillType.EVEN_ODD);

        reusePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        reusePaint_RFS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        updatePaint();
    }

    //ofc its a library
    public void setPaintOverride(@NonNull Paint normalFillPaint, @NonNull Paint randomFillPaint) {
        this.reusePaint = normalFillPaint;
        this.reusePaint_RFS = randomFillPaint;
        externalPaintEnabled = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        setGridWidth(w);
        setGridHeight(h);
        smartUpdate();
        shouldUpdate = true;
    }

    private void attributeSetter(TypedArray typedArray) {
        bleedX = (int) typedArray.getDimension(R.styleable.TrianglifyView_bleedX, 100);
        bleedY = (int) typedArray.getDimension(R.styleable.TrianglifyView_bleedY, 100);
        variance = (int) typedArray.getDimension(R.styleable.TrianglifyView_variance, 10);
        cellSize = (int) typedArray.getDimension(R.styleable.TrianglifyView_cellSize, 40);
        typeGrid = typedArray.getInt(R.styleable.TrianglifyView_gridType, 0);
        fillTriangle = typedArray.getBoolean(R.styleable.TrianglifyView_fillTriangle, true);
        drawStroke = typedArray.getBoolean(R.styleable.TrianglifyView_fillStrokes, false);
        strokeSizeF = typedArray.getFloat(R.styleable.TrianglifyView_strokeSize, strokeSizeF);
        palette = Palette.getPalette(typedArray.getInt(R.styleable.TrianglifyView_palette, 0));
        randomColoring = typedArray.getBoolean(R.styleable.TrianglifyView_randomColoring, false);
        fillViewCompletely = typedArray.getBoolean(R.styleable.TrianglifyView_fillViewCompletely, false);
        isRandomizeFillStrokeEnabled = typedArray.getBoolean(R.styleable.TrianglifyView_randomizeFillWithStatic, false);
        randomStaticFillColor = typedArray.getColor(R.styleable.TrianglifyView_randomizeFillColor, randomStaticFillColor);

        typedArray.recycle();

        if (fillViewCompletely) checkViewFilledCompletely();
    }

    public TrianglifyView setRandomizeFillStrokeEnabled(boolean trigger) {
        this.isRandomizeFillStrokeEnabled = trigger;
        return this;
    }

    public boolean isRandomizeFillStrokeEnabled() {
        return isRandomizeFillStrokeEnabled;
    }

    public int getRandomStaticFillColor() {
        return randomStaticFillColor;
    }

    public TrianglifyView setRandomStaticFillColor(int randomStaticFillColor) {
        this.randomStaticFillColor = randomStaticFillColor;
        return this;
    }


    @Override
    public int getBleedX() {
        return bleedX;
    }

    public TrianglifyView setBleedX(int bleedX) {
        this.bleedX = bleedX;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) checkViewFilledCompletely();

        shouldUpdate = true;
        return this;
    }

    @Override
    public int getBleedY() {
        return bleedY;
    }

    public TrianglifyView setBleedY(int bleedY) {
        this.bleedY = bleedY;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) checkViewFilledCompletely();
        shouldUpdate = true;
        return this;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    public TrianglifyView setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    public TrianglifyView setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Deprecated
    @Override
    public int getBitmapQuality() {
        return bitmapQuality;
    }

    @Deprecated
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
        shouldUpdate = true;
        return this;
    }

    @Override
    public int getVariance() {
        return variance;
    }

    public TrianglifyView setVariance(int variance) {
        this.variance = variance;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public int getCellSize() {
        return cellSize;
    }

    public TrianglifyView setCellSize(int cellSize) {
        this.cellSize = cellSize;
        presenter.viewState = Presenter.ViewState.GRID_PARAMETERS_CHANGED;
        if (fillViewCompletely) checkViewFilledCompletely();
        shouldUpdate = true;
        return this;
    }

    public TrianglifyView setFillViewCompletely(boolean fillViewCompletely) {
        this.fillViewCompletely = fillViewCompletely;
        if (fillViewCompletely) checkViewFilledCompletely();
        shouldUpdate = true;
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
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED && presenter.viewState != Presenter.ViewState.COLOR_SCHEME_CHANGED)
            presenter.viewState = Presenter.ViewState.PAINT_STYLE_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public boolean isDrawStrokeEnabled() {
        return drawStroke;
    }

    public TrianglifyView setDrawStrokeEnabled(boolean drawStroke) {
        this.drawStroke = drawStroke;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED && presenter.viewState != Presenter.ViewState.COLOR_SCHEME_CHANGED)
            presenter.viewState = Presenter.ViewState.PAINT_STYLE_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public boolean isRandomColoringEnabled() {
        return randomColoring;
    }

    public TrianglifyView setRandomColoring(boolean randomColoring) {
        this.randomColoring = randomColoring;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED)
            presenter.viewState = Presenter.ViewState.COLOR_SCHEME_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public Palette getPalette() {
        return palette;
    }

    public TrianglifyView setPalette(Palette palette) {
        this.palette = palette;
        if (presenter.viewState != Presenter.ViewState.GRID_PARAMETERS_CHANGED)
            presenter.viewState = Presenter.ViewState.COLOR_SCHEME_CHANGED;
        shouldUpdate = true;
        return this;
    }

    @Override
    public Presenter.ViewState getViewState() {
        return presenter.viewState;
    }

    public TrianglifyView setTriangulation(Triangulation triangulation) {
        this.triangulation = triangulation;
        shouldUpdate = true;
        return this;
    }

    public void clearView() {
        presenter.clearSoup();
    }

    @Override
    public void invalidateView(Triangulation triangulation) {
        this.setTriangulation(triangulation);
        invalidate();
        shouldUpdate = true;
        presenter.viewState = Presenter.ViewState.UNCHANGED_TRIANGULATION;
    }

    private void drawTriangle(Paint paint, Canvas canvas, Triangle2D triangle2D) {
        reusePath.moveTo(triangle2D.a.x - bleedX, triangle2D.a.y - bleedY);
        reusePath.lineTo(triangle2D.b.x - bleedX, triangle2D.b.y - bleedY);
        reusePath.lineTo(triangle2D.c.x - bleedX, triangle2D.c.y - bleedY);
        reusePath.close();

        canvas.drawPath(reusePath, paint);
        reusePath.reset();
    }

    private void drawTriangle_RFS(Paint paint, Paint rfs_paint, Canvas canvas, Triangle2D triangle2D) {
        reusePath.moveTo(triangle2D.a.x - bleedX, triangle2D.a.y - bleedY);
        reusePath.lineTo(triangle2D.b.x - bleedX, triangle2D.b.y - bleedY);
        reusePath.lineTo(triangle2D.c.x - bleedX, triangle2D.c.y - bleedY);
        reusePath.close();

        canvas.drawPath(reusePath, random.nextBoolean() ? paint : rfs_paint);
        reusePath.reset();
    }

    private Paint.Style getPaintStyle() {
        if (isFillTriangle() && isDrawStrokeEnabled()) return Paint.Style.FILL_AND_STROKE;
        else if (isFillTriangle()) return Paint.Style.FILL;
        else return Paint.Style.STROKE;
    }


    private void updatePaint() {
        if (!externalPaintEnabled) {
            reusePaint.setStrokeWidth(strokeSizeF);
            reusePaint.setStyle(getPaintStyle());
            if (isRandomizeFillStrokeEnabled) {
                //why update anyways, we can but why?
                reusePaint_RFS.setStrokeWidth(strokeSizeF);
                reusePaint_RFS.setStyle(getPaintStyle());
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (shouldUpdate) {
            gridHeight = getHeight();
            gridWidth = getWidth();
            updatePaint();
            shouldUpdate = false;
        }

        if (triangulation != null) for (Triangle2D triangle : triangulation.triangleList()) {
            reusePaint.setColor(triangle.getColor() + 0xff000000);
            if (isRandomizeFillStrokeEnabled) {
                reusePaint_RFS.setColor(randomStaticFillColor);
                drawTriangle_RFS(reusePaint, reusePaint_RFS, canvas, triangle);
            } else drawTriangle(reusePaint, canvas, triangle);
        }
        else generateAndInvalidate();
    }

    public void smartUpdate() {
        presenter.updateView();
    }

    public void generateAndInvalidate() {
        presenter.setGenerateOnlyColor(false);
        presenter.generateSoupAndInvalidateView();
        shouldUpdate = true;
    }

    private void checkViewFilledCompletely() {
        if (bleedY <= cellSize || bleedX <= cellSize) {
            throw new IllegalArgumentException("bleedY and bleedX should be larger than cellSize for view to be completely filled.");
        }
    }

    public Bitmap getBitmap(int width, int height) {
        return BitmapUtils.createScaledBitmapWithBilinearSampling(this, width, height);
    }
}
