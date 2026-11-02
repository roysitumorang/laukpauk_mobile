package id.laukpauk.ui.home;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.KontenItem;

/** A konten block with its items ({@code items} null = still loading). */
public final class KontenSection {
    public final Konten konten;
    @Nullable
    public final List<KontenItem> items;

    public KontenSection(Konten konten, @Nullable List<KontenItem> items) {
        this.konten = konten;
        this.items = items;
    }
}
