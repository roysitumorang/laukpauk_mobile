package id.laukpauk.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.ClientPath;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.components.intropage.IntroPageIndicator;
import id.laukpauk.view.components.intropage.IntroPageItem;

/** lib/views/IntroPage.dart: two full-screen intro images in a pager with dot indicator. */
public class IntroPageActivity extends BaseActivity {
    private final List<JsonObject> introPageData = new ArrayList<>();
    private IntroPageIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPageData.add(Json.of("img", ClientPath.assetsImg + "/intro-2.jpg"));
        introPageData.add(Json.of("img", ClientPath.assetsImg + "/intro-3.jpg"));

        FrameLayout root = new FrameLayout(this);

        ViewPager2 pager = new ViewPager2(this);
        pager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                IntroPageItem item = new IntroPageItem(IntroPageActivity.this, viewType, introPageData.get(viewType));
                item.setLayoutParams(new RecyclerView.LayoutParams(Ui.MATCH, Ui.MATCH));
                return new RecyclerView.ViewHolder(item) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getItemCount() {
                return introPageData.size();
            }
        });
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                indicator.setActivePage(position + 1);
            }
        });
        root.addView(pager, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        indicator = new IntroPageIndicator(this, introPageData.size(), 1);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        lp.bottomMargin = Ui.dp(this, 40);
        root.addView(indicator, lp);

        setRoot(root);
    }
}
