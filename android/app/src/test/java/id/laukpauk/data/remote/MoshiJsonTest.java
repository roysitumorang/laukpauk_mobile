package id.laukpauk.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.squareup.moshi.Json;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.model.notifikasi.NotifikasiResponses;
import id.laukpauk.data.model.payment.ReturnedDataFromCheckout;

/** The app's Moshi setup ({@link ApiClient#createMoshi()}) against the backend's loosely typed JSON. */
public class MoshiJsonTest {

    private final Moshi moshi = ApiClient.createMoshi();

    public static class Item {
        public boolean flag;
        public Boolean boxedFlag;
        public int count;
        public long id;
        public double price;
        public String text;
        @Json(name = "nama_lain") public String renamed;
        public Nested nested;
        public List<Nested> list;
        public Object raw;
    }

    public static class Nested {
        public String value;
    }

    private Item item(String json) throws Exception {
        return moshi.adapter(Item.class).fromJson(json);
    }

    @Test
    public void booleansAcceptNumbersAndStrings() throws Exception {
        assertTrue(item("{\"flag\":\"1\"}").flag);
        assertTrue(item("{\"flag\":1}").flag);
        assertTrue(item("{\"flag\":true}").flag);
        assertTrue(item("{\"flag\":\"true\"}").flag);
        assertFalse(item("{\"flag\":\"0\"}").flag);
        assertFalse(item("{\"flag\":null}").flag);
        assertNull(item("{\"boxedFlag\":null}").boxedFlag);
    }

    @Test
    public void numbersAcceptStringsAndEmpty() throws Exception {
        assertEquals(12, item("{\"count\":\"12\"}").count);
        assertEquals(12, item("{\"count\":12.0}").count);
        assertEquals(0, item("{\"count\":\"\"}").count);
        assertEquals(0, item("{\"count\":null}").count);
        assertEquals(9007199254740993L, item("{\"id\":\"9007199254740993\"}").id);
        assertEquals(1.5, item("{\"price\":\"1,5\"}").price, 0.0);
    }

    @Test
    public void stringsAcceptScalarsAndKeepJsonOfObjects() throws Exception {
        assertEquals("1500", item("{\"text\":1500}").text);
        assertEquals("true", item("{\"text\":true}").text);
        assertEquals("{\"a\":1}", item("{\"text\":{\"a\":1}}").text);
        assertEquals("x", item("{\"nama_lain\":\"x\"}").renamed);
    }

    @Test
    public void falseOrEmptyMeansNoObject() throws Exception {
        assertNull(item("{\"nested\":false}").nested);
        assertNull(item("{\"nested\":\"\"}").nested);
        assertNull(item("{\"list\":false}").list);
        assertEquals("v", item("{\"nested\":\"{\\\"value\\\":\\\"v\\\"}\"}").nested.value);
        assertEquals("w", item("{\"list\":[{\"value\":\"w\"}]}").list.get(0).value);
    }

    @Test
    public void untypedTreesKeepWholeNumbersWhole() throws Exception {
        Item i = item("{\"raw\":{\"idTransaksi\":1500,\"total\":12.5,\"ok\":false,\"xs\":[1,2]}}");
        Map<String, Object> raw = id.laukpauk.util.Json.obj(i.raw);
        assertEquals(1500L, raw.get("idTransaksi"));
        assertEquals(12.5, raw.get("total"));
        assertEquals(Boolean.FALSE, raw.get("ok"));
        assertEquals("{\"idTransaksi\":1500,\"total\":12.5,\"ok\":false,\"xs\":[1,2]}",
                id.laukpauk.util.Json.toJson(raw));
        assertEquals("1500", id.laukpauk.util.Json.str(raw, "idTransaksi"));

        Nested fromTree = moshi.adapter(Nested.class).fromJsonValue(id.laukpauk.util.Json.of("value", 1500L));
        assertEquals("1500", fromTree.value);
    }

    @Test
    public void appDtosRoundTrip() throws Exception {
        NotifikasiResponses.KhususList k = moshi.adapter(NotifikasiResponses.KhususList.class).fromJson(
                "{\"notifikasi\":[{\"id\":7,\"judul\":\"Hi\",\"isRead\":\"1\"}]}");
        assertNotNull(k);
        assertEquals("7", k.notifikasi.get(0).id);
        assertTrue(k.notifikasi.get(0).isRead);

        String json = "{\"responseCheckout\":{\"idTransaksi\":\"55\",\"paymentResponse\":{\"totalBayar\":10000}},"
                + "\"dataCheckout\":{\"nama\":\"A\"}}";
        ReturnedDataFromCheckout r = moshi.adapter(ReturnedDataFromCheckout.class).fromJson(json);
        assertEquals("55", r.responseCheckout.idTransaksi);
        String again = moshi.adapter(ReturnedDataFromCheckout.class).toJson(r);
        assertTrue(again, again.contains("\"totalBayar\":10000"));
    }

    /** Every class in data/model must be readable by Moshi (no platform types, no inner classes). */
    @Test
    public void everyDtoHasAnAdapter() throws Exception {
        File root = new File(NotifikasiResponses.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File model = new File(root, "id/laukpauk/data/model");
        assertTrue("compiled classes not found at " + model, model.isDirectory());
        List<String> failures = new ArrayList<>();
        int checked = 0;
        for (String name : classNames(root, model)) {
            Class<?> c = Class.forName(name);
            if (c.isInterface() || Modifier.isAbstract(c.getModifiers()) || c.isAnonymousClass()
                    || c.getEnclosingClass() != null && !Modifier.isStatic(c.getModifiers())
                    || Modifier.isPrivate(c.getModifiers())) {
                continue;
            }
            try {
                moshi.adapter(c).fromJson("{}");
                checked++;
            } catch (Exception | Error e) {
                failures.add(name + ": " + e);
            }
        }
        if (!failures.isEmpty()) {
            fail(String.join("\n", failures));
        }
        assertTrue(checked > 50);
    }

    @Test
    public void listTypes() throws Exception {
        List<Nested> l = moshi.<List<Nested>>adapter(Types.newParameterizedType(List.class, Nested.class))
                .fromJson("[{\"value\":\"a\"}]");
        assertEquals("a", l.get(0).value);
    }

    private static List<String> classNames(File root, File dir) {
        List<String> out = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return out;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                out.addAll(classNames(root, f));
            } else if (f.getName().endsWith(".class")) {
                String rel = root.toURI().relativize(f.toURI()).getPath();
                out.add(rel.substring(0, rel.length() - ".class".length()).replace('/', '.'));
            }
        }
        return out;
    }
}
