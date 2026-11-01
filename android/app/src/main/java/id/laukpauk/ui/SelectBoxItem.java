package id.laukpauk.ui;

/**
 * Port of `class SelectBoxItem` (declared in lib/global-components/CustomForm.dart): one entry of
 * {@link CustomForm#selectBox}. `CustomForm.SelectBoxItem` is the same type under its nested name,
 * so both spellings work for callers.
 */
public class SelectBoxItem {
    public Object value;
    public String text;

    public SelectBoxItem(Object value, String text) {
        this.value = value;
        this.text = text;
    }
}
