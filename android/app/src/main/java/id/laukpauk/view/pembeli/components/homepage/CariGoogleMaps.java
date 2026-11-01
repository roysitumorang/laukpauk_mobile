package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/HomePage/CariGoogleMaps.dart: an address search box
 * (flutter_typeahead TypeAheadField -> EditText + an inline suggestion list) backed by the Google
 * Places "textsearch" web API. onSuggestionSelected receives
 * {location: {lat, lng}, formattedAddress: String}.
 */
public class CariGoogleMaps extends LinearLayout {
    private static final long DEBOUNCE_MS = 300L;

    private final Async.Done<JsonObject> onSuggestionSelected;
    private final String apiKeyState;
    private final EditText field;
    private final LinearLayout suggestionsHolder;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int requestCounter = 0;
    private Runnable pendingQuery;

    /** ScrollView limited to 300dp (the suggestion box of TypeAheadField). */
    private static final class MaxHeightScroll extends ScrollView {
        private final int maxHeightPx;

        MaxHeightScroll(Context ctx, int maxHeightPx) {
            super(ctx);
            this.maxHeightPx = maxHeightPx;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST));
        }
    }

    public CariGoogleMaps(Context ctx, Async.Done<JsonObject> onSuggestionSelected) {
        super(ctx);
        this.onSuggestionSelected = onSuggestionSelected;
        setOrientation(VERTICAL);
        Ui.pad(this, 10, 0, 10, 0);
        float hb = Ui.hb(ctx);

        apiKeyState = LocalStorage.getGoogleMapsApiKey();

        // Material(elevation: 5, borderRadius: 50) around the TypeAheadField's TextField
        LinearLayout fieldCard = Ui.column(ctx);
        Ui.background(fieldCard, CustomColors.white, 50);
        fieldCard.setElevation(Ui.dp(ctx, 5));

        field = new EditText(ctx);
        field.setHint("Cari Alamat di Sini");
        field.setHintTextColor(0x99000000);
        field.setTextSize(16f);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setBackground(null);
        field.setTypeface(Ui.font(ctx));
        field.setPadding(Ui.dp(ctx, 25 + 12), Ui.dp(ctx, 12), Ui.dp(ctx, 12), Ui.dp(ctx, 12));
        fieldCard.addView(field, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(fieldCard, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        suggestionsHolder = Ui.column(ctx);
        MaxHeightScroll scroll = new MaxHeightScroll(ctx, Ui.dp(ctx, 300));
        scroll.addView(suggestionsHolder, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        Ui.background(scroll, CustomColors.white, 4);
        scroll.setElevation(Ui.dp(ctx, 4));
        scroll.setVisibility(View.GONE);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        scrollLp.topMargin = Ui.dp(ctx, 5);
        addView(scroll, scrollLp);

        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                query(field.getText().toString());
            } else {
                hideSuggestions();
            }
        });
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!field.hasFocus()) {
                    return;
                }
                final String pattern = s.toString();
                if (pendingQuery != null) {
                    handler.removeCallbacks(pendingQuery);
                }
                pendingQuery = () -> query(pattern);
                handler.postDelayed(pendingQuery, DEBOUNCE_MS);
            }
        });
    }

    private ScrollView suggestionsScroll() {
        return (ScrollView) suggestionsHolder.getParent();
    }

    private void hideSuggestions() {
        requestCounter++;
        suggestionsScroll().setVisibility(View.GONE);
    }

    /** BLOCKING. Dart: getWilayahFromGMaps(string: ...). */
    private JsonArray getWilayahFromGMaps(String string) {
        if (string.length() >= 1) {
            Http.Response httpResponse = Http.get(
                    "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + Http.enc(string)
                            + "&key=" + apiKeyState);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "results");
            }
        }
        return new JsonArray();
    }

    private void query(final String pattern) {
        final int mine = ++requestCounter;
        HomeUi.act(getContext()).async(() -> getWilayahFromGMaps(pattern), results -> {
            if (mine != requestCounter || !field.hasFocus()) {
                return;
            }
            showSuggestions(results);
        });
    }

    private void showSuggestions(JsonArray results) {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        suggestionsHolder.removeAllViews();
        if (results == null || results.size() == 0) {
            // noItemsFoundBuilder
            LinearLayout none = Ui.column(ctx);
            none.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(none, 0, 25, 0, 25);
            none.addView(Ui.asset(ctx, "empty.png", hb * 30));
            none.addView(HomeUi.t(ctx, "Lokasi yang kamu cari tidak ada", hb * 3.5f));
            suggestionsHolder.addView(none, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            for (int i = 0; i < results.size(); i++) {
                final JsonObject suggestion = Json.objAt(results, i);
                LinearLayout tile = Ui.column(ctx);
                Ui.pad(tile, 16, 5 + 8, 16, 8);
                tile.addView(Ui.text(ctx, Json.str(suggestion, "name"), 15f, CustomColors.green, true));
                tile.addView(HomeUi.t(ctx, Json.str(suggestion, "formatted_address"), 12f));
                Ui.onTap(tile, () -> select(suggestion));
                suggestionsHolder.addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        }
        suggestionsScroll().setVisibility(View.VISIBLE);
    }

    private void select(JsonObject detailSuggestion) {
        JsonObject geometry = Json.object(detailSuggestion, "geometry");
        JsonObject latLong = Json.object(geometry, "location");
        String formattedAddress = Json.str(detailSuggestion, "formatted_address");

        JsonObject selectedSuggestion = new JsonObject();
        selectedSuggestion.add("location", latLong);
        selectedSuggestion.addProperty("formattedAddress", formattedAddress);

        BaseActivity a = HomeUi.act(getContext());
        field.clearFocus();
        a.hideKeyboard();
        hideSuggestions();
        if (onSuggestionSelected != null) {
            onSuggestionSelected.run(selectedSuggestion);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (pendingQuery != null) {
            handler.removeCallbacks(pendingQuery);
        }
        super.onDetachedFromWindow();
    }
}
