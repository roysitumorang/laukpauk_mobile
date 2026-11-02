package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.HistoriPenjualan;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.HistoriPenjualanRepository;

/** State of HistoriPenjualan.dart: the selected tab, the list and each row's total. */
public class HistoriPenjualanViewModel extends ViewModel {

    private final HistoriPenjualanRepository repository;
    private final String idPenjual;

    private final MutableLiveData<Integer> tab = new MutableLiveData<>(0);
    private final MediatorLiveData<Resource<List<HistoriPenjualan>>> list = new MediatorLiveData<>();
    private LiveData<Resource<List<HistoriPenjualan>>> source;
    private final Map<String, Integer> totals = new HashMap<>();
    private List<HistoriPenjualan> items = new ArrayList<>();
    private int loadId;
    private boolean started;

    public HistoriPenjualanViewModel(HistoriPenjualanRepository repository, String idPenjual) {
        this.repository = repository;
        this.idPenjual = idPenjual;
    }

    public LiveData<Integer> getTab() {
        return tab;
    }

    public LiveData<Resource<List<HistoriPenjualan>>> getList() {
        return list;
    }

    public void start() {
        if (!started) {
            started = true;
            load();
        }
    }

    public void selectTab(int index) {
        Integer current = tab.getValue();
        if (current == null || current != index) {
            tab.setValue(index);
            load();
        }
    }

    /** Only the "Completed" tab filters (statusTransaksi = received), like the legacy. */
    public void load() {
        final int myLoad = ++loadId;
        Integer t = tab.getValue();
        if (source != null) {
            list.removeSource(source);
        }
        source = repository.getHistoriPenjualan(idPenjual, t != null && t == 2 ? "received" : null);
        list.addSource(source, res -> {
            if (myLoad != loadId) {
                return;
            }
            if (res.isSuccess() && res.data != null) {
                items = res.data;
                list.setValue(Resource.success(withTotals()));
                for (HistoriPenjualan h : items) {
                    if (h.id != null && !totals.containsKey(h.id)) {
                        repository.getTotalTransaksi(h.id, r -> {
                            totals.put(h.id, r.data == null ? 0 : r.data);
                            if (myLoad == loadId) {
                                list.setValue(Resource.success(withTotals()));
                            }
                        });
                    }
                }
            } else {
                list.setValue(res);
            }
        });
    }

    private List<HistoriPenjualan> withTotals() {
        List<HistoriPenjualan> out = new ArrayList<>();
        for (HistoriPenjualan h : items) {
            out.add(h.withTotal(h.id == null ? null : totals.get(h.id)));
        }
        return out;
    }

    public void resolveIdPenjual(BaseRepository.ResultCallback<String> callback) {
        repository.getIdPenjual(callback);
    }
}
