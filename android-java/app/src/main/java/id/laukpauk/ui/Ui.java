package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.io.InputStream;

import id.laukpauk.core.ClientPath;

/**
 * View-building helpers that stand in for Flutter's widget constructors.
 *
 * Units: every "Dp" parameter is density-independent pixels (Flutter's logical pixels);
 * Ui.hb(ctx) is Flutter's SizeConfig.horizontalBlock (1% of the screen width) in dp, Ui.vb(ctx)
 * is SizeConfig.verticalBlock. Fonts are Poppins (Ui.font), as in the Flutter app.
 */
public final class Ui {
    private Ui() {}

    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    // ------------------------------------------------------------------ metrics

    public static float density(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    public static float screenWidthDp(Context ctx) {
        DisplayMetrics m = ctx.getResources().getDisplayMetrics();
        return m.widthPixels / m.density;
    }

    public static float screenHeightDp(Context ctx) {
        DisplayMetrics m = ctx.getResources().getDisplayMetrics();
        return m.heightPixels / m.density;
    }

    /** SizeConfig.horizontalBlock, in dp. */
    public static float hb(Context ctx) {
        return screenWidthDp(ctx) / 100f;
    }

    /** SizeConfig.verticalBlock, in dp. */
    public static float vb(Context ctx) {
        return screenHeightDp(ctx) / 100f;
    }

    /** dp -> px. */
    public static int dp(Context ctx, float dp) {
        return Math.round(dp * density(ctx));
    }

    // ------------------------------------------------------------------ typography

    private static Typeface poppins;

    public static Typeface font(Context ctx) {
        if (poppins == null) {
            try {
                poppins = Typeface.createFromAsset(ctx.getApplicationContext().getAssets(),
                        "fonts/Poppins-Regular.ttf");
            } catch (RuntimeException e) {
                poppins = Typeface.DEFAULT;
            }
        }
        return poppins;
    }

    public static TextView text(Context ctx, CharSequence s, Txt.Style style) {
        TextView tv = new TextView(ctx);
        tv.setText(s);
        style.apply(tv);
        return tv;
    }

    public static TextView text(Context ctx, CharSequence s, float sizeSp, int color) {
        return text(ctx, s, new Txt.Style(sizeSp).color(color));
    }

    public static TextView text(Context ctx, CharSequence s, float sizeSp, int color, boolean bold) {
        return text(ctx, s, new Txt.Style(sizeSp).color(color).bold(bold));
    }

    /** Plain text in the default style (textMD-ish black). */
    public static TextView text(Context ctx, CharSequence s) {
        return text(ctx, s, new Txt.Style(hb(ctx) * 3.5f));
    }

    // ------------------------------------------------------------------ containers

    /** Flutter Column. */
    public static LinearLayout column(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    /** Flutter Row (children vertically centred, like CrossAxisAlignment.center). */
    public static LinearLayout row(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** Flutter Stack. */
    public static FrameLayout stack(Context ctx) {
        return new FrameLayout(ctx);
    }

    /** Vertical scroll container holding `child` (Flutter SingleChildScrollView). */
    public static ScrollView scroll(Context ctx, View child) {
        ScrollView s = new ScrollView(ctx);
        s.setFillViewport(true);
        s.addView(child, new ViewGroup.LayoutParams(MATCH, WRAP));
        return s;
    }

    public static LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    /** Width 0 + weight (Flutter Expanded) inside a horizontal LinearLayout. */
    public static LinearLayout.LayoutParams weightW(float weight) {
        return new LinearLayout.LayoutParams(0, WRAP, weight);
    }

    /** Height 0 + weight (Flutter Expanded) inside a vertical LinearLayout. */
    public static LinearLayout.LayoutParams weightH(float weight) {
        return new LinearLayout.LayoutParams(MATCH, 0, weight);
    }

    /** Adds the child with its own (or default) layout params and returns it. */
    public static <V extends View> V add(ViewGroup parent, V child) {
        parent.addView(child);
        return child;
    }

    public static <V extends View> V add(ViewGroup parent, V child, int width, int height) {
        parent.addView(child, new LinearLayout.LayoutParams(width, height));
        return child;
    }

    /** Adds `child` to a horizontal row as Flutter's Expanded(child: ...). */
    public static <V extends View> V addExpanded(LinearLayout parent, V child) {
        parent.addView(child, weightW(1f));
        return child;
    }

    /** Flutter SizedBox(height: h). */
    public static View spaceH(Context ctx, float dp) {
        View v = new View(ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(ctx, dp)));
        return v;
    }

    /** Flutter SizedBox(width: w). */
    public static View spaceW(Context ctx, float dp) {
        View v = new View(ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, dp), 1));
        return v;
    }

    /** An invisible view that takes all remaining space (Flutter Spacer). */
    public static View spacer(Context ctx) {
        View v = new View(ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1f));
        return v;
    }

    public static View divider(Context ctx, int color, float heightDp) {
        View v = new View(ctx);
        v.setBackgroundColor(color);
        v.setLayoutParams(new LinearLayout.LayoutParams(MATCH, Math.max(1, dp(ctx, heightDp))));
        return v;
    }

    // ------------------------------------------------------------------ spacing

    /** EdgeInsets.fromLTRB (dp). */
    public static void pad(View v, float l, float t, float r, float b) {
        Context c = v.getContext();
        v.setPadding(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
    }

    /** EdgeInsets.all. */
    public static void padAll(View v, float dp) {
        pad(v, dp, dp, dp, dp);
    }

    /** EdgeInsets.symmetric. */
    public static void padXY(View v, float horizontal, float vertical) {
        pad(v, horizontal, vertical, horizontal, vertical);
    }

    /** Sets margins (dp). Creates LinearLayout params when the view has none yet. */
    public static void margin(View v, float l, float t, float r, float b) {
        Context c = v.getContext();
        ViewGroup.LayoutParams p = v.getLayoutParams();
        ViewGroup.MarginLayoutParams m;
        if (p instanceof ViewGroup.MarginLayoutParams) {
            m = (ViewGroup.MarginLayoutParams) p;
        } else if (p != null) {
            m = new ViewGroup.MarginLayoutParams(p);
        } else {
            m = new LinearLayout.LayoutParams(WRAP, WRAP);
        }
        m.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        v.setLayoutParams(m);
    }

    // ------------------------------------------------------------------ decoration

    /** BoxDecoration(color, borderRadius). */
    public static GradientDrawable shape(Context ctx, int fill, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(ctx, radiusDp));
        return g;
    }

    /** BoxDecoration(color, borderRadius, border). */
    public static GradientDrawable shape(Context ctx, int fill, float radiusDp, int strokeColor,
                                         float strokeWidthDp) {
        GradientDrawable g = shape(ctx, fill, radiusDp);
        g.setStroke(Math.max(1, dp(ctx, strokeWidthDp)), strokeColor);
        return g;
    }

    /** Circle BoxDecoration. */
    public static GradientDrawable circle(int fill) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        return g;
    }

    /** Shortcut: background colour with rounded corners. */
    public static void background(View v, int fill, float radiusDp) {
        v.setBackground(shape(v.getContext(), fill, radiusDp));
    }

    /** Clips the view's content to rounded corners (Flutter ClipRRect). */
    public static void clipRounded(View v, float radiusDp) {
        final float radiusPx = dp(v.getContext(), radiusDp);
        v.setClipToOutline(true);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
    }

    // ------------------------------------------------------------------ images

    private static final LruCache<String, Bitmap> ASSET_CACHE = new LruCache<String, Bitmap>(24) {
    };

    /** Loads app/src/main/assets/img/<path> (Flutter: Image.asset('assets/img/<path>')). Null when missing. */
    public static Bitmap assetBitmap(Context ctx, String path) {
        Bitmap cached = ASSET_CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        InputStream in = null;
        try {
            in = ctx.getApplicationContext().getAssets().open(ClientPath.assetsImg + "/" + path);
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (bmp != null) {
                ASSET_CACHE.put(path, bmp);
            }
            return bmp;
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // nothing to do
                }
            }
        }
    }

    /** Image.asset with a fixed width; the height follows the aspect ratio. */
    public static ImageView asset(Context ctx, String path, float widthDp) {
        ImageView iv = new ImageView(ctx);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageBitmap(assetBitmap(ctx, path));
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, widthDp), WRAP));
        return iv;
    }

    /** Image.asset with width and height (BoxFit.contain). */
    public static ImageView asset(Context ctx, String path, float widthDp, float heightDp) {
        ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageBitmap(assetBitmap(ctx, path));
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, widthDp), dp(ctx, heightDp)));
        return iv;
    }

    /** Sets an asset image on an existing ImageView. */
    public static void setAsset(ImageView iv, String path) {
        iv.setImageBitmap(assetBitmap(iv.getContext(), path));
    }

    /** A network image (Glide) with the "no image" placeholder; BoxFit.cover by default. */
    public static ImageView network(Context ctx, String url, float widthDp, float heightDp) {
        ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, widthDp), dp(ctx, heightDp)));
        loadNetwork(iv, url);
        return iv;
    }

    /** Loads `url` into the ImageView with placeholder/error images. Safe to call repeatedly. */
    public static void loadNetwork(ImageView iv, String url) {
        Context ctx = iv.getContext();
        if (ctx instanceof Activity && ((Activity) ctx).isDestroyed()) {
            return;
        }
        BitmapDrawable fallback = fallbackDrawable(ctx);
        Glide.with(iv).load(url).apply(new RequestOptions()
                .placeholder(fallback != null ? fallback : new ColorDrawable(0xFFEEEEEE))
                .error(fallback != null ? fallback : new ColorDrawable(0xFFEEEEEE)))
                .into(iv);
    }

    /** Same as loadNetwork but cropped to a circle (CircleAvatar / ClipOval). */
    public static void loadNetworkCircle(ImageView iv, String url) {
        Context ctx = iv.getContext();
        if (ctx instanceof Activity && ((Activity) ctx).isDestroyed()) {
            return;
        }
        BitmapDrawable fallback = fallbackDrawable(ctx);
        Glide.with(iv).load(url).apply(RequestOptions.circleCropTransform()
                .placeholder(fallback != null ? fallback : new ColorDrawable(0xFFEEEEEE))
                .error(fallback != null ? fallback : new ColorDrawable(0xFFEEEEEE)))
                .into(iv);
    }

    private static BitmapDrawable fallbackDrawable(Context ctx) {
        Bitmap b = assetBitmap(ctx, "no-image-img.png");
        return b == null ? null : new BitmapDrawable(ctx.getResources(), b);
    }

    /** A vector icon (see Icons) with a tint. */
    public static ImageView icon(Context ctx, int drawableRes, float sizeDp, int tintColor) {
        ImageView iv = new ImageView(ctx);
        iv.setImageResource(drawableRes);
        iv.setColorFilter(tintColor);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, sizeDp), dp(ctx, sizeDp)));
        return iv;
    }

    // ------------------------------------------------------------------ interaction / misc

    /** Makes the whole view tappable (Flutter InkWell/GestureDetector onTap). */
    public static <V extends View> V onTap(V v, final Runnable action) {
        v.setClickable(true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                action.run();
            }
        });
        return v;
    }

    public static void toast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }

    /** Hides the view (View.GONE) or shows it. Flutter: `cond ? widget : SizedBox()`. */
    public static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** Resources shortcut. */
    public static Resources res(Context ctx) {
        return ctx.getResources();
    }
}
