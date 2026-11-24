package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import id.laukpauk.data.model.address.BuyerAddress;
import id.laukpauk.data.model.setting.SettingResponses;
import id.laukpauk.data.remote.ApiClient;

/** Delivery cost = {@code distance_km} of the delivery address × {@code delivery_cost_per_km} of buyer/setting. */
public class DeliveryCostTest {

    @Test
    public void cost_isDistanceTimesCostPerKm() {
        assertEquals(15000, CheckoutRepository.deliveryCost(3, 5000));
        assertEquals(0, CheckoutRepository.deliveryCost(0, 5000));
        assertEquals(0, CheckoutRepository.deliveryCost(-1, 5000));
    }

    @Test
    public void setting_readsDeliveryCostPerKm() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"vacation_mode\":false,\"delivery_origin_longitude\":\"98.66\","
                + "\"delivery_origin_latitude\":\"3.58\",\"delivery_cost_per_km\":5000,"
                + "\"delivery_max_distance_km\":20,\"payment_terms\":\"1h0m0s\"}}";
        SettingResponses.Buyer body = ApiClient.createMoshi().adapter(SettingResponses.Buyer.class).fromJson(json);
        assertEquals(5000, body.data.deliveryCostPerKm);
        assertEquals(20, body.data.deliveryMaxDistanceKm);
    }

    @Test
    public void address_isFoundByItsPoint() {
        BuyerAddress home = address("3.5952", "98.6722", 3);
        BuyerAddress office = address("3.6", "98.7", 7);
        List<BuyerAddress> all = Arrays.asList(home, office);
        assertSame(office, AddressRepository.at(all, "3.60", "98.70"));
        assertSame(home, AddressRepository.at(all, " 3.5952", "98.6722 "));
        assertNull(AddressRepository.at(all, "3.5953", "98.6722"));
        assertNull(AddressRepository.at(all, null, "98.6722"));
        assertNull(AddressRepository.at(null, "3.6", "98.7"));
    }

    private static BuyerAddress address(String lat, String lng, long km) {
        BuyerAddress a = new BuyerAddress();
        a.id = lat;
        a.destinationLatitude = lat;
        a.destinationLongitude = lng;
        a.distanceKm = km;
        return a;
    }
}
