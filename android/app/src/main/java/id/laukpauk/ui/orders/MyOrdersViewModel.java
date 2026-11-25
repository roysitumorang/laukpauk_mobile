package id.laukpauk.ui.orders;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import id.laukpauk.data.Paged;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TransactionRepository;

/**
 * State of Pesanan Saya ({@code GET buyer/transactions}): the tab ("Hari Ini" with its status dropdown, or a
 * status tab: {@code status}), the date range
 * ({@code created_at_start} / {@code created_at_end}) and the list, which grows page by page ({@code data.links.next}).
 */
public class MyOrdersViewModel extends ViewModel {

    /** Tab 0: the transactions created today, filtered by the status dropdown ({@link #setTodayStatus}). */
    static final int TAB_TODAY = 0;

    /**
     * The status of each tab: Hari Ini (the dropdown's), Semua, Belum Bayar, Dibayar, Diproses, Dikirim, Selesai,
     * Dibatalkan.
     */
    static final Integer[] TAB_STATUS = {null, null, BuyerTransaction.STATUS_PENDING, BuyerTransaction.STATUS_PAID,
            BuyerTransaction.STATUS_PROCESSED, BuyerTransaction.STATUS_SHIPPED, BuyerTransaction.STATUS_COMPLETED,
            BuyerTransaction.STATUS_CANCELLED};

    private final TransactionRepository repository;
    private final SettingRepository settings;

    private final MutableLiveData<Resource<List<BuyerTransaction>>> list = new MutableLiveData<>();
    private final List<BuyerTransaction> rows = new ArrayList<>();
    @Nullable private String next;
    private boolean loadingMore;
    /** Bumped on every reload, so a late answer for an old filter is dropped. */
    private int generation;

    private int tab;
    /** The status chosen in the "Hari Ini" dropdown; null = all. */
    @Nullable private Integer todayStatus;
    private String startDate = "";
    private String endDate = "";
    private boolean started;

    public MyOrdersViewModel(TransactionRepository repository, SettingRepository settings, int initialTab) {
        this.repository = repository;
        this.settings = settings;
        this.tab = initialTab;
    }

    public LiveData<Resource<List<BuyerTransaction>>> getList() {
        return list;
    }

    public int getTab() {
        return tab;
    }

    @Nullable
    public Integer getTodayStatus() {
        return todayStatus;
    }

    /** The "Hari Ini" dropdown: a status 0..8, or null for all. */
    public void setTodayStatus(@Nullable Integer value) {
        if (value == null ? todayStatus != null : !value.equals(todayStatus)) {
            todayStatus = value;
            load();
        }
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean hasDateRange() {
        return !startDate.isEmpty() || !endDate.isEmpty();
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

    /** {@code value} "yyyy-MM-dd" (the day picked), "" to clear. */
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

    /** Reloads the first page for the current tab and dates. */
    public void load() {
        final int gen = ++generation;
        rows.clear();
        next = null;
        loadingMore = false;
        list.setValue(Resource.loading());
        Integer status;
        String start;
        String end;
        if (tab == TAB_TODAY) {
            // created today, in the phone's time zone
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
            status = todayStatus;
            start = dayStart(today);
            end = dayEnd(today);
        } else {
            status = tab >= 0 && tab < TAB_STATUS.length ? TAB_STATUS[tab] : null;
            start = dayStart(startDate);
            end = dayEnd(endDate);
        }
        repository.getBuyerTransactions(status, start, end, r -> {
            if (gen != generation) {
                return;
            }
            if (!r.isSuccess() || r.data == null) {
                list.setValue(Resource.error(r.message, r.code));
                return;
            }
            take(r.data);
        });
    }

    /** The next page, when there is one and none is loading. */
    public void loadMore() {
        if (next == null || loadingMore) {
            return;
        }
        loadingMore = true;
        final int gen = generation;
        repository.getBuyerTransactionsPage(next, r -> {
            if (gen != generation) {
                return;
            }
            loadingMore = false;
            if (r.isSuccess() && r.data != null) {
                take(r.data);
            }
        });
    }

    private void take(Paged<BuyerTransaction> page) {
        rows.addAll(page.items);
        next = page.next;
        list.setValue(Resource.success(new ArrayList<>(rows)));
    }

    /** 00:00:00 of the day in the phone's time zone, as RFC 3339 in UTC; null for "". */
    @Nullable
    static String dayStart(String day) {
        return bound(day, false);
    }

    /** 23:59:59 of the day in the phone's time zone, as RFC 3339 in UTC; null for "". */
    @Nullable
    static String dayEnd(String day) {
        return bound(day, true);
    }

    @Nullable
    private static String bound(@Nullable String day, boolean end) {
        if (day == null || day.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            in.setLenient(false);
            Calendar c = Calendar.getInstance();
            c.setTime(in.parse(day));
            if (end) {
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
            }
            // UTC with "Z": no "+" in the query string
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            out.setTimeZone(TimeZone.getTimeZone("UTC"));
            return out.format(c.getTime());
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public void orderReceived(String transactionId, BaseRepository.ResultCallback<Boolean> callback) {
        repository.orderReceived(transactionId, callback);
    }

    /** The WhatsApp contact number setting (legacy getContactUsWhatsappConfiguration). */
    public void contactWhatsapp(BaseRepository.ResultCallback<Object> callback) {
        settings.getContactUsWhatsappConfiguration(callback);
    }
}
