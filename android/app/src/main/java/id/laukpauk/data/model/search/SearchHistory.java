package id.laukpauk.data.model.search;

/** A saved search ("title" + "onTime", kept in SharedPreferences under "historiPencarian"). */
public class SearchHistory {
    public String title;
    public String onTime;

    public SearchHistory() {
    }

    public SearchHistory(String title, String onTime) {
        this.title = title;
        this.onTime = onTime;
    }
}
