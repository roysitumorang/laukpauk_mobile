package id.laukpauk.data.model.search;

/** A saved search ("title" + "onTime", kept in SharedPreferences under "historiPencarian"). */
public class HistoriPencarian {
    public String title;
    public String onTime;

    public HistoriPencarian() {
    }

    public HistoriPencarian(String title, String onTime) {
        this.title = title;
        this.onTime = onTime;
    }
}
