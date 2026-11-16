package id.laukpauk.ui.home;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;

/** A konten block with its items ({@code items} null = still loading). */
public final class ContentSection {
    public final Content content;
    @Nullable
    public final List<ContentItem> items;

    public ContentSection(Content content, @Nullable List<ContentItem> items) {
        this.content = content;
        this.items = items;
    }
}
