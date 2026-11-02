package id.laukpauk.ui.market;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.market.Pasar;
import id.laukpauk.databinding.ViewMarketPasarLainSectionBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;

/**
 * One "Pasar Lain" section (legacy PasarLainSection / PasarLainDalamCA / PasarLainLuarCA): spinner while loading,
 * then a name search over the already loaded markets, the horizontal list or the empty note.
 */
class PasarLainSection {

    private final ViewMarketPasarLainSectionBinding b;
    private final PasarLainAdapter adapter;
    private List<Pasar> all = Collections.emptyList();
    private String query = "";

    PasarLainSection(ViewMarketPasarLainSectionBinding b, int title, int emptyText, boolean showDistance,
                     PasarLainAdapter.Listener listener) {
        this.b = b;
        this.adapter = new PasarLainAdapter(showDistance, listener);
        b.title.setText(title);
        b.emptyText.setText(emptyText);
        Images.asset(b.emptyImage, "empty.png");
        b.list.setLayoutManager(new LinearLayoutManager(b.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        b.list.setAdapter(adapter);
        SearchBox.bind(b.search, b.getRoot().getContext().getString(R.string.market_cari_pasar), "", this::filter, () -> filter(""));
    }

    void render(Resource<List<Pasar>> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            b.progress.setVisibility(View.VISIBLE);
            b.body.setVisibility(View.GONE);
            return;
        }
        b.progress.setVisibility(View.GONE);
        b.body.setVisibility(View.VISIBLE);
        all = res.isSuccess() && res.data != null ? res.data : Collections.<Pasar>emptyList();
        filter(query);
    }

    private void filter(String text) {
        query = text == null ? "" : text;
        String lc = query.toLowerCase(Locale.ROOT);
        List<Pasar> shown = new ArrayList<>();
        for (Pasar p : all) {
            if (p.nama != null && p.nama.toLowerCase(Locale.ROOT).contains(lc)) {
                shown.add(p);
            }
        }
        adapter.submitList(shown);
        b.list.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
        b.empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
