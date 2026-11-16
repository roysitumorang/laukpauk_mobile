package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TransactionRepository;

/** State of Pesanan Saya: the selected status tab, today's status filter, the date range and the list. */
public class MyOrdersViewModel extends ViewModel {

    private final TransactionRepository repository;
    private final SettingRepository settings;

    private final MediatorLiveData<Resource<List<Transaction>>> list = new MediatorLiveData<>();
    private LiveData<Resource<List<Transaction>>> source;

    private int tab;
    private String todayStatus = "";
    private String startDate = "";
    private String endDate = "";
    private boolean started;

    public MyOrdersViewModel(TransactionRepository repository, SettingRepository settings, int initialTab) {
        this.repository = repository;
        this.settings = settings;
        this.tab = initialTab;
    }

    public LiveData<Resource<List<Transaction>>> getList() {
        return list;
    }

    public int getTab() {
        return tab;
    }

    public String getTodayStatus() {
        return todayStatus;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean hasDateRange() {
        return !startDate.isEmpty() && !endDate.isEmpty();
    }

    /** First load only (a rotation keeps the list). */
    public void start() {
        if (!started) {
            started = true;
            load();
        }
    }

    public void selectTab(int newTab) {
        if (newTab != tab) {
            tab = newTab;
            load();
        }
    }

    public void setTodayStatus(String value) {
        if (!value.equals(todayStatus)) {
            todayStatus = value;
            load();
        }
    }

    public void setStartDate(String value) {
        startDate = value;
        load();
    }

    public void setEndDate(String value) {
        endDate = value;
        load();
    }

    public void clearDates() {
        startDate = "";
        endDate = "";
        load();
    }

    public void load() {
        if (source != null) {
            list.removeSource(source);
        }
        source = repository.getRecords(tab, todayStatus, startDate, endDate);
        list.addSource(source, list::setValue);
    }

    public void orderReceived(String transactionId, BaseRepository.ResultCallback<Boolean> callback) {
        repository.orderReceived(transactionId, callback);
    }

    /** The WhatsApp contact number setting (legacy getContactUsWhatsappConfiguration). */
    public void contactWhatsapp(BaseRepository.ResultCallback<Object> callback) {
        settings.getContactUsWhatsappConfiguration(callback);
    }
}
