package com.example.kadir.agricultureprojectclientside;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;


import com.example.kadir.agricultureprojectclientside.ShortCut.ShortCut;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Module;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Product;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ProductData;
import com.example.kadir.agricultureprojectclientside.datatypes.math.Vector2;
import com.example.kadir.agricultureprojectclientside.vendors.snatik.polygon.Line;
import com.example.kadir.agricultureprojectclientside.vendors.snatik.polygon.Point;
import com.example.kadir.agricultureprojectclientside.vendors.snatik.polygon.Polygon;

import java.util.ArrayList;
import java.util.Random;

public class FarmEditView extends android.support.v7.widget.AppCompatImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "FarmEditView";

    private static final float MAX_SCALE_X = 6;
    private static final float MAX_SCALE_Y = 6;

    private static final float MIN_SCALE_X = 1;
    private static final float MIN_SCALE_Y = 1;

    //INTERFACES
    public OnLongClickListener on_long_click_listener;
    public OnProductClick on_product_click_listener;
    public OnModuleClick on_module_click_listener;
    public OnPhaseChange on_phase_change_listener;
    public OnDisplayPhaseChange on_display_phase_change;
    public OnFixupCellClick on_fixup_cell_click;

    //FLAGS
    private boolean allow_putting_module = false;
    private boolean can_remove_module = false;
    private boolean can_fix_cell       = false;

    //BOOKEPING
    private EditPhases current_edit_phase;
    private DisplayPhases current_display_phase;
    private Control current_control;

    //GESTURE DETECTORS
    private GestureDetector gesture_detector;
    private ScaleGestureDetector scale_gesture_detector;

    //DATA
    private Farm edited_farm; // which farm is currently edited

    //FOR EDIT PHASE
    private ArrayList<Product> products; // keeps track of all the placed products
    private Vector2 unsnapped_mpos = new Vector2(); // unnsaped pointer position in normalized space which means it's between 0 and farm size

    //FOR OUTLINE DRAW PHASE
    private ArrayList<Vector2> outline_points;
    private ArrayList<Vector2> temp_points;
    private Vector2 start_offset = new Vector2(32, 32);  // HOW FAR IT WILL START DRAWING FROM x and y : for this instance it will start from + 32 , offset from x and y
    private Vector2 current_snapped_point = new Vector2(0, 0); // THIS IS IN NORMALIZED SPACE FOR INSTANCE IF FARM SIZE IS 16 by 16 it will be whole numbers from 0 to 16
    private boolean is_snapped = false;
    private Vector2 snapped_mpos = new Vector2(0, 0); // pointer position for normalized space and it's rounded to closest whole number

    //UTILS AND BOUNDS
    private Canvas canvas_ref; // might need in the futre
    private Vector2 canvas_bounds = new Vector2(0, 0); // keeps the canvas width and height as a vector
    private Vector2 dxdy = new Vector2(0, 0); // difference between two dots this measured in pixel space

    //CAMERA
    private Vector2 scale = new Vector2(1, 1); // camera scale factor more larger the value more bigger the scale
    private Vector2 offset = new Vector2(0, 0); // this moves the camera
    private Vector2 avg_mpos = new Vector2(0, 0); // average of the pointer position in normalized space between 0 and farm size

    //FOR COLLISION DETECTION
    private Polygon outline_shape; // This 3rd party class allows to create a polygon from given vertices which then can be used for pointer intersection and collision

    //PAINT
    private Paint cell_paint = new Paint();
    private Paint paint = new Paint();
    private Paint text_paint = new Paint();
    private Paint product_paint = new Paint();
    private Paint fixup_cell_paint = new Paint();

    //PRECALCULATED DATA
    private ArrayList<FixupCellData> fixup_cell_data; // This represents the each single cell and, whether it's condition is good or bad and provides data for drawing
    private ArrayList<Line> grid_lines;  //pre calculated grids to draw faster

    public FarmEditView(Context context) {
        super(context);
        init(context);
    }

    public FarmEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FarmEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void load_farm(Farm f, boolean is_display_only) {
        load_farm(f);
        if (is_display_only) change_edit_phase(EditPhases.DISPLAY_ONLY_PHASE);
    }

    public void load_farm(Farm f) {
        if (f == null) {
            Log.v(TAG, "Loaded farm is nulll");
            return;
        }

        Log.v(TAG, f.farm_id + " is loaded");

        if (f.outline_points != null && f.outline_points.size() >= 3 && check_if_outline_is_closed(f.outline_points)) {
            edited_farm = f;
            products = f.products;
            outline_points = f.outline_points;
            change_edit_phase(EditPhases.PRODUCT_PLACEMENT_PHASE);
            current_display_phase = DisplayPhases.DISPLAY_PRODUCTS;

            build_outline_shape();
            initilize_fixup_cell_data();
            initilze_line_grid();
        } else {
            ShortCut.displayMessageToast(getContext(), "Farm data is not valid something went wrong");
        }
    }

    //constructs the polygon from the given vertices
    private void build_outline_shape() {
        if (outline_points.size() < 3) return;
        Polygon.Builder builder = Polygon.Builder();

        for (Vector2 v : outline_points) {
            builder.addVertex(v.toPoint());
        }

        outline_shape = builder.build();
    }

    public void save_farm() {
        if (outline_points != null && check_if_outline_is_closed(outline_points)) {
            edited_farm.outline_points = outline_points;
            if (products != null)
                edited_farm.products = products;
            Log.v(TAG, edited_farm.toString());
        } else {
            Toast.makeText(getContext(), "Önce Çizimi Tamamlayın", Toast.LENGTH_LONG).show();
        }

    }

    private void init(Context context) {
        current_control = Control.EDIT;
        outline_points = new ArrayList<>();
        temp_points = new ArrayList<>();
        products = new ArrayList<>();
        fixup_cell_data = new ArrayList<>();
        grid_lines = new ArrayList<>();

        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(8);

        cell_paint.setColor(Color.BLUE);
        cell_paint.setAlpha(120);
        cell_paint.setStyle(Paint.Style.FILL);

        product_paint.setColor(Color.GREEN);
        product_paint.setAlpha(190);
        product_paint.setStyle(Paint.Style.FILL);

        fixup_cell_paint.setColor(Color.GREEN);
        fixup_cell_paint.setAlpha(190);
        fixup_cell_paint.setStyle(Paint.Style.FILL);

        text_paint.setColor(Color.BLACK);
        text_paint.setTextSize(10);
        text_paint.setTextAlign(Paint.Align.CENTER);
        text_paint.setFakeBoldText(true);


        scale_gesture_detector = new ScaleGestureDetector(getContext(), this);
        gesture_detector = new GestureDetector(getContext(), this);
    }


    private void init_bounds(Canvas canvas) {
        this.canvas_ref = canvas;
        canvas_bounds.x = this.canvas_ref.getWidth();
        canvas_bounds.y = this.canvas_ref.getHeight();

        dxdy.x = (canvas_bounds.x + start_offset.x) / edited_farm.size;
        dxdy.y = (canvas_bounds.y + start_offset.y) / edited_farm.size;
    }

    private boolean check_if_outline_is_closed(ArrayList<Vector2> lines /* each two vector is one line */ ) {
        if (lines == null || lines.size() < 3) return false;
        return lines.get(0).equals(lines.get(lines.size() - 1) /* if end and start are the same out line is closed */ );
    }

    public void clean() {
        products.clear();
        change_edit_phase(EditPhases.PRODUCT_PLACEMENT_PHASE);
        scale.x = 1;
        scale.y = 1;
        offset.x = 0;
        offset.y = 0;
    }

    private void draw_line_from_points(Canvas canvas, ArrayList<Vector2> points /* two vectors is one line */ ) {
        Vector2 first_point = null;
        for (Vector2 v : points) {
            if (first_point == null) {
                first_point = v;
            } else {
                float x1 = first_point.x * dxdy.x + start_offset.x;
                float y1 = first_point.y * dxdy.y + start_offset.y;
                float x2 = v.x * dxdy.x + start_offset.x;
                float y2 = v.y * dxdy.y + start_offset.y;
                //Log.v(TAG, "DRAING AT <" + x1 + " , " + y1 + " , " + x2 + " , " + y2 + "> ");
                canvas.drawLine(x1, y1, x2, y2, paint);
                first_point = v;
            }
        }
    }

    private void draw_fixup_cell(Canvas canvas) {
        Path path = new Path();

        for (FixupCellData f : fixup_cell_data) {
            if (f.condition) {
                //IN GOOD CONDITION
                fixup_cell_paint.setColor(Color.GREEN);
            } else {
                //IS NOT IN GOOD CONTDION
                fixup_cell_paint.setColor(Color.RED);
            }
            path.reset();
            path.moveTo(f.points.get(0).x * dxdy.x + start_offset.x, f.points.get(0).y * dxdy.y + start_offset.y);
            for (Vector2 p : f.points) {
                path.lineTo(p.x * dxdy.x + start_offset.x, p.y * dxdy.y + start_offset.y);
            }

            canvas.drawPath(path, fixup_cell_paint);
        }

    }

    private void draw_grids(Canvas canvas) {
        for (Line l : grid_lines) {
            Point start = l.getStart();
            Point end = l.getEnd();
            canvas.drawLine(
                    (float) start.x * dxdy.x + start_offset.x,
                    (float) start.y * dxdy.y + start_offset.y,
                    (float) end.x * dxdy.x + start_offset.x,
                    (float) end.y * dxdy.y + start_offset.y,
                    paint);
        }
    }

    private void draw_product_cells(Canvas canvas, boolean show_module_ids) {
        Path path = new Path();

        for (Product product : products) {
            path.reset();
            path.moveTo(product.points.get(0).x * dxdy.x + start_offset.x, product.points.get(0).y * dxdy.y + start_offset.y);
            Vector2 avg = new Vector2();
            for (Vector2 point : product.points) {
                path.lineTo(point.x * dxdy.x + start_offset.x, point.y * dxdy.y + start_offset.y);
                avg = avg.add(new Vector2(point.x * dxdy.x + start_offset.x, point.y * dxdy.y + start_offset.y));
            }
            avg = avg.div(product.points.size());


            byte[] bytes = product.product_data.product_id.getBytes(); // generate random color from given string
            int l = bytes.length;

            product_paint.setARGB(255, bytes[3 % l] * bytes[0 % l], bytes[2 % l] * bytes[1 % l], bytes[3 % l] + bytes[4 % l]);

            canvas.drawPath(path, product_paint);

            if (show_module_ids) {
                canvas.drawText(product.product_data.product_name, avg.x, avg.y, text_paint);
            }

        }
    }

    private void draw_module_cells(Canvas canvas, boolean show_module_ids) {
        Path path = new Path();

        for (Module module : edited_farm.modules) {
            path.reset();
            path.moveTo(module.points.get(0).x * dxdy.x + start_offset.x, module.points.get(0).y * dxdy.y + start_offset.y);
            Vector2 avg = new Vector2();
            for (Vector2 point : module.points) {
                path.lineTo(point.x * dxdy.x + start_offset.x, point.y * dxdy.y + start_offset.y);
                avg = avg.add(new Vector2(point.x * dxdy.x + start_offset.x, point.y * dxdy.y + start_offset.y));
            }
            avg = avg.div(module.points.size());

            canvas.drawPath(path, cell_paint);

            if (show_module_ids) {
                canvas.drawText(module.module_id, avg.x, avg.y, text_paint);
            }

        }
    }

    private void draw_dot_matrix(Canvas canvas, float radius, Paint paint) {
        for (int y = 0; y < edited_farm.size; y++) {
            for (int x = 0; x < edited_farm.size; x++) {
                //convert normalized space into pixel space
                float x0 = x * dxdy.x + start_offset.x;
                float y0 = y * dxdy.y + start_offset.y;
                if (outline_points.contains(new Vector2(x, y)))
                    canvas.drawCircle(x0, y0, radius * 1.8f, paint);
                else if (current_edit_phase == EditPhases.OUTLINE_DRAW_PHASE)
                    canvas.drawCircle(x0, y0, radius, paint);

            }
        }
    }

    //Tries to cast a rectangular or triangular area wrt outline_shape by the given position
    //only valid if returned array size > 2
    //all of these calculations are in normalized space this means each dot is 1 unit away from each other
    private ArrayList<Vector2> cast_fill(float x, float y) {
        float mx = x;
        float my = y;

        ArrayList<Vector2> m = new ArrayList<>();

        Vector2 center = new Vector2((float) Math.floor(mx) + 0.5f, (float) Math.floor(my) + 0.5f);

        /*
         *       *********
         *       **  a  **
         *       **    * *
         *       * *  *  *
         *       *d **  b*
         *       *  **   *
         *       * *  *  *
         *       **  c  **
         *       *********
         * */

        boolean a = false;
        boolean b = false;
        boolean c = false;
        boolean d = false;

        if (outline_shape.contains(center.add(Vector2.up.mult(0.2f)).toPoint())) {
            a = true;
        }
        if (outline_shape.contains(center.add(Vector2.right.mult(0.2f)).toPoint())) {
            b = true;
        }
        if (outline_shape.contains(center.add(Vector2.down.mult(0.2f)).toPoint())) {
            c = true;
        }
        if (outline_shape.contains(center.add(Vector2.left.mult(0.2f)).toPoint())) {
            d = true;
        }


        if (a & b & c & d) {
            m.add(new Vector2(center.add(new Vector2(-0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, 0.5f))));
            m.add(new Vector2(center.add(new Vector2(-0.5f, 0.5f))));
            Log.v(TAG, "ABCD");
        } else if (a & b) {
            m.add(new Vector2(center.add(new Vector2(-0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, 0.5f))));
            Log.v(TAG, "AB");
        } else if (a & d) {
            m.add(new Vector2(center.add(new Vector2(-0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(-0.5f, 0.5f))));
            Log.v(TAG, "AD");
        } else if (c & b) {
            m.add(new Vector2(center.add(new Vector2(0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, 0.5f))));
            m.add(new Vector2(center.add(new Vector2(-0.5f, 0.5f))));
            Log.v(TAG, "CB");
        } else if (c & d) {
            m.add(new Vector2(center.add(new Vector2(-0.5f, -0.5f))));
            m.add(new Vector2(center.add(new Vector2(0.5f, 0.5f))));
            m.add(new Vector2(center.add(new Vector2(-0.5f, 0.5f))));
            Log.v(TAG, "CD");
        }


        return m;
    }

    //this is controller a function which is meant to be called from outside of this class
    public Product remove_data_last_selected_place() {
        Product removed_product = null;
        Product m = new Product();
        m.points = cast_fill(unsnapped_mpos.x, unsnapped_mpos.y);
        if (m.points.size() >= 2 && can_remove_module) {
            int index = -1;
            if ((index = products.indexOf(m)) >= 0) {
                removed_product = products.get(index);
                products.remove(index);
                can_remove_module = false;
            }
        }

        return removed_product;
    }

    //this is controller a function which is meant to be called from outside of this class
    public void fixup_cell_in_last_selected_place() {
        FixupCellData f = new FixupCellData();
        f.points = cast_fill(unsnapped_mpos.x, unsnapped_mpos.y);
        int index = -1;
        if ((index = fixup_cell_data.indexOf(f)) >= 0) {
            fixup_cell_data.get(index).condition = true;
            //initilize_fixup_cell_data();
        }
    }

    //this is controller a function which is meant to be called from outside of this class
    public void put_data_onto_last_selected_place(ProductData productData) {
        if (!allow_putting_module) return;

        Product m = new Product();
        m.points = cast_fill(unsnapped_mpos.x, unsnapped_mpos.y);

        if (m.points.size() <= 2) {
            Log.v(TAG, "Module cannot be added");
            return;
        } else {
            Log.v(TAG, productData.product_id + " is added to " + m.toString());
            m.product_data = productData;
            products.add(m);
        }
    }

    //TODO: REFACTOR HERE PLZ
    private void finger_down_behaviour_product_placement_phase(MotionEvent event) {

        Product m = new Product();
        m.points = cast_fill(unsnapped_mpos.x, unsnapped_mpos.y);
        FixupCellData f = new FixupCellData();
        f.points = m.points;

        if (m.points.size() <= 2) {
            allow_putting_module = false;
        } else {
            if(current_display_phase == DisplayPhases.DISPLAY_FIXES){
                if(!fixup_cell_data.get( fixup_cell_data.indexOf(f)).condition){
                    can_fix_cell = true;
                }else{
                    can_fix_cell = false;
                }
            }
            if (current_display_phase == DisplayPhases.DISPLAY_PRODUCTS){
                int index = products.indexOf(m);
                if (index >= 0) {
                    allow_putting_module = false;
                    can_remove_module = true;
                } else {
                    allow_putting_module = true;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //at each touch calculate the average
        avg_mpos = avg_mpos.mult(0);
        for (int i = 0; i < event.getPointerCount(); i++) {
            avg_mpos = avg_mpos.add(new Vector2(event.getX(i), event.getY(i)));
        }
        avg_mpos = avg_mpos.div(event.getPointerCount());

        //is edit phase display only no need for touch detection so let parent do the touches
        if (current_edit_phase == EditPhases.DISPLAY_ONLY_PHASE) return super.onTouchEvent(event);

        //allow detectors to do thier stuff
        scale_gesture_detector.onTouchEvent(event);
        gesture_detector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        //scale pointer x and y positions to normalized space so it's between 0 and farm size
        unsnapped_mpos.x = (x - start_offset.x * scale.x - offset.x) / (dxdy.x * scale.x);
        unsnapped_mpos.y = (y - start_offset.y * scale.y - offset.y) / (dxdy.y * scale.y);

        //same as above but rounded to nearest int
        snapped_mpos.x = (int) Math.round((x - start_offset.x * scale.x - (offset.x)) / (dxdy.x * scale.x));
        snapped_mpos.y = (int) Math.round((y - start_offset.y * scale.y - (offset.y)) / (dxdy.y * scale.y));

        if (snapped_mpos.x >= 0 && snapped_mpos.x < edited_farm.size && snapped_mpos.y >= 0 && snapped_mpos.y < edited_farm.size) {
            if (current_edit_phase == EditPhases.PRODUCT_PLACEMENT_PHASE) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        current_control = Control.EDIT;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        finger_down_behaviour_product_placement_phase(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
            }
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas_ref == null) {
            init_bounds(canvas); // RUNS ONLY ONE IN THE BEGGING
        }

        canvas.save();
        canvas.translate(offset.x, offset.y);
        canvas.scale(scale.x, scale.y);

        if (current_edit_phase == EditPhases.OUTLINE_DRAW_PHASE) {
            draw_dot_matrix(canvas, 8, paint);
            if (is_snapped) {
                draw_line_from_points(canvas, temp_points);
            }

        } else if (current_edit_phase == EditPhases.PRODUCT_PLACEMENT_PHASE || current_edit_phase == EditPhases.DISPLAY_ONLY_PHASE) {
            draw_grids(canvas);
            if (current_display_phase == DisplayPhases.DISPLAY_MODULES)
                draw_module_cells(canvas, true);
            else if (current_display_phase == DisplayPhases.DISPLAY_PRODUCTS)
                draw_product_cells(canvas, true);
            else if(current_display_phase == DisplayPhases.DISPLAY_FIXES)
                draw_fixup_cell(canvas);
        }

        draw_line_from_points(canvas, outline_points);

        canvas.restore();
        invalidate();
        super.onDraw(canvas);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // Log.v(TAG , "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //  Log.v(TAG , "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // Log.v(TAG , "onSingleTapUp");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.v(TAG , "onScroll " + e1.getPointerCount() + " , " + e2.getPointerCount());

        if ((e1.getPointerCount() == 1 && e2.getPointerCount() == 2 || e1.getPointerCount() == 2 && e2.getPointerCount() == 1)) {
            current_control = Control.DRAG;
            offset.x -= distanceX;
            offset.y -= distanceY;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        on_long_click_listener.onLongClick(this);
        //Log.v(TAG , "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Log.v(TAG , "Fling " + e1.toString() + " , " + e2.toString() + " <" + velocityX + "," + velocityY + ">");
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // Log.v(TAG , "onSingleTapConfiremd");
        if (current_display_phase == DisplayPhases.DISPLAY_MODULES) {
            Module p = new Module();
            p.points = cast_fill(unsnapped_mpos.x, unsnapped_mpos.y);
            int index = -1;
            if ((index = edited_farm.modules.indexOf(p)) >= 0) {
                on_module_click_listener.on_module_click(edited_farm.modules.get(index).module_id);
            }
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Log.v(TAG , "onDoubleTap");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scf = detector.getScaleFactor();
        boolean cannot_zoom = false;
        current_control = Control.ZOOM;

        scale.x *= scf;
        scale.y *= scf;

        if (scale.x > MAX_SCALE_X) {
            scale.x = MAX_SCALE_X;
            cannot_zoom = true;
        }
        if (scale.x < MIN_SCALE_X) {
            scale.x = MIN_SCALE_X;
            cannot_zoom = true;
        }

        if (scale.y > MAX_SCALE_Y) {
            scale.y = MAX_SCALE_Y;
            cannot_zoom = true;
        }
        if (scale.y < MIN_SCALE_Y) {
            scale.y = MIN_SCALE_Y;
            cannot_zoom = true;
        }

        if (!cannot_zoom) {
            float d = detector.getCurrentSpan() - detector.getPreviousSpan();
            Vector2 temp = avg_mpos.sub(offset).normalized();
            offset.x -= temp.x * d * 2;
            offset.y -= temp.y * d * 2;
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //Log.v(TAG , "onScaleBegin");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // Log.v(TAG , "onScaleEnd");
    }


    private void change_edit_phase(EditPhases nextPhase) {
        EditPhases prev = current_edit_phase;
        current_edit_phase = nextPhase;
        if (on_phase_change_listener == null) return;
        on_phase_change_listener.on_phase_change(prev, current_edit_phase);
    }

    private void change_display_phase(DisplayPhases nextPhase) {
        DisplayPhases prev = current_display_phase;
        current_display_phase = nextPhase;
        if (on_display_phase_change == null) return;
        on_display_phase_change.on_display_phase_change(prev, current_display_phase);
    }


    public Farm getEdited_farm() {
        return edited_farm;
    }

    public boolean can_fix_cell() {
        return can_fix_cell;
    }

    public boolean allow_putting_module() {
        return allow_putting_module;
    }

    public EditPhases current_edit_phase() {
        return current_edit_phase;
    }

    public DisplayPhases current_display_phase() {
        return current_display_phase;
    }

    public boolean can_remove_module() {
        return can_remove_module;
    }

    public void next_display_phase() {
        change_display_phase(DisplayPhases.values()[(current_display_phase.ordinal() + 1) % DisplayPhases.values().length]);
    }

    //fills the fixup cell data with random data
    public void initilize_fixup_cell_data() {
        Random random = new Random();
        for (int y = 0; y < edited_farm.size; y++) {
            for (int x = 0; x < edited_farm.size; x++) {
                Product p = new Product();
                p.points = cast_fill(x + 0.5f, y + 0.5f);
                if (p != null && p.points.size() >= 3) {
                    FixupCellData f = new FixupCellData();
                    f.points = p.points;
                    f.condition = random.nextBoolean();
                    fixup_cell_data.add(f);
                }
            }
        }
    }

    //calculated the positions of the square grid in side of the polygon
    public void initilze_line_grid() {
        for (int y = 0; y < edited_farm.size; y++) {
            for (int x = 0; x < edited_farm.size; x++) {
                float x0 = x;
                float y0 = y;
                if (outline_shape.contains(new Point(x + 0.2f, y + 0.1f))) {
                    grid_lines.add(new Line(new Point(x0, y0), new Point((x + 1), y0)));
                }
                if (outline_shape.contains(new Point(x + 0.1f, y + 0.2f))) {
                    grid_lines.add(new Line(new Point(x0, y0), new Point(x0, (y + 1))));
                }
            }
        }
    }

    public enum EditPhases {OUTLINE_DRAW_PHASE, PRODUCT_PLACEMENT_PHASE, FIX_PHASE, DISPLAY_ONLY_PHASE}

    public enum DisplayPhases {DISPLAY_MODULES, DISPLAY_PRODUCTS, DISPLAY_FIXES}

    private enum Control {EDIT, ZOOM, DRAG}

    public interface OnProductClick {
        void on_product_click(ProductData productData);
    }

    public interface OnModuleClick {
        void on_module_click(String module_id);
    }

    public interface OnFixupCellClick {
        void on_fixup_cell_click(FixupCellData fixupCellData);
    }

    public interface OnPhaseChange {
        void on_phase_change(EditPhases prev_phase, EditPhases current_phase);
    }

    public interface OnDisplayPhaseChange {
        void on_display_phase_change(DisplayPhases prev_phase, DisplayPhases current_phase);
    }

    //Represents the condition of the current cell veya 1 dönüm arazi
    //EACH CELL == 1 dönüm arazi
    private class FixupCellData {
        public ArrayList<Vector2> points;
        /* false: BAD , true: Good */
        public boolean condition = false;
        public FixupCellData() {
            points = new ArrayList<>();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FixupCellData) {
                FixupCellData m = (FixupCellData) obj;
                boolean flag = false;
                for (Vector2 v1 : m.points) {
                    flag = false;
                    for (Vector2 v2 : points) {
                        if (v1.equals(v2)) {
                            flag = true;
                        }
                    }
                    if (flag == false) {
                        return false;
                    }
                }

                return true;
            }
            return false;
        }
    }
}
