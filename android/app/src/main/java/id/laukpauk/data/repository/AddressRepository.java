package id.laukpauk.data.repository;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.address.AddressResponses;
import id.laukpauk.data.model.address.BuyerAddress;
import id.laukpauk.data.remote.AddressApi;

/** The signed-in buyer's delivery addresses ({@code buyer/addresses}). */
public class AddressRepository extends BaseRepository {

    /** Addresses per request (the largest page the backend allows). */
    static final int LIMIT = 100;
    /** Pages followed at most (safety net against a looping {@code next}). */
    private static final int MAX_PAGES = 10;

    private final AddressApi api;

    public AddressRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(AddressApi.class);
    }

    /** True when a buyer is signed in (the address book needs the JWT). */
    public boolean isSignedIn() {
        return !authToken().isEmpty();
    }

    /** All the buyer's addresses, the main one first ({@code data.links.next} pages are followed). */
    public void list(ResultCallback<List<BuyerAddress>> callback) {
        List<BuyerAddress> all = new ArrayList<>();
        enqueue(api.list(LIMIT, "main:desc"), AddressRepository::page, r -> collect(r, all, 1, callback));
    }

    private void collect(Resource<AddressResponses.Page> r, List<BuyerAddress> all, int pages,
                         ResultCallback<List<BuyerAddress>> callback) {
        if (!r.isSuccess()) {
            callback.onResult(Resource.<List<BuyerAddress>>error(r.message, r.code));
            return;
        }
        AddressResponses.Page page = r.data;
        if (page != null && page.rows != null) {
            for (BuyerAddress a : page.rows) {
                if (a != null && a.id != null) {
                    all.add(a);
                }
            }
        }
        String next = page == null || page.links == null ? null : page.links.next;
        if (next == null || next.trim().isEmpty() || pages >= MAX_PAGES
                || page.rows == null || page.rows.size() < LIMIT) {
            callback.onResult(Resource.success(all));
            return;
        }
        enqueue(api.page(next), AddressRepository::page, n -> collect(n, all, pages + 1, callback));
    }

    @Nullable
    private static AddressResponses.Page page(@Nullable AddressResponses.AddressList body) {
        return body == null ? null : body.data;
    }

    /** Creates ({@code address.id} null) or updates the address; answers the saved one. */
    public void save(BuyerAddress address, ResultCallback<BuyerAddress> callback) {
        Map<String, Object> body = body(address);
        boolean update = address.id != null && !address.id.isEmpty();
        enqueue(update ? api.update(address.id, body) : api.create(body),
                (AddressResponses.Saved s) -> s == null ? null : s.data, callback);
    }

    static Map<String, Object> body(BuyerAddress a) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("longitude", nz(a.longitude).trim());
        body.put("latitude", nz(a.latitude).trim());
        body.put("street_address", nz(a.streetAddress).trim());
        body.put("delivery_note", nz(a.deliveryNote).trim());
        body.put("label", nz(a.label).trim());
        body.put("recipient_name", nz(a.recipientName).trim());
        body.put("recipient_mobile_phone", nz(a.recipientMobilePhone).trim());
        body.put("main", a.main);
        return body;
    }

    private static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }
}
