package id.laukpauk.ui.common;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;

import id.laukpauk.databinding.ViewSearchBoxBinding;

/**
 * Binds {@code layout/view_search_box.xml} (include it with an id, e.g. {@code binding.search}): the IME
 * search action and the search button call {@code onSearch}; the clear button (visible while there is text)
 * empties the field and calls {@code onClear}.
 */
public final class SearchBox {
    private SearchBox() {}

    public interface OnSearch {
        void onSearch(String query);
    }

    public static void bind(ViewSearchBoxBinding b, @Nullable CharSequence hint, @Nullable String initialText,
                            OnSearch onSearch, @Nullable Runnable onClear) {
        if (hint != null) {
            b.field.setHint(hint);
        }
        b.field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                b.clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
        setText(b, initialText);
        b.field.setOnEditorActionListener((v, actionId, event) -> {
            onSearch.onSearch(b.field.getText().toString());
            return true;
        });
        b.searchButton.setOnClickListener(v -> onSearch.onSearch(b.field.getText().toString()));
        b.clearButton.setOnClickListener(v -> {
            setText(b, "");
            if (onClear != null) {
                onClear.run();
            }
        });
    }

    public static void setText(ViewSearchBoxBinding b, @Nullable String text) {
        String t = text == null ? "" : text;
        b.field.setText(t);
        b.field.setSelection(t.length());
    }
}
