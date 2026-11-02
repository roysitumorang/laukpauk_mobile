package id.laukpauk.data.model.seller;

/** Legacy {@code Json.parseInt(str, 0)}: the server sends numbers as strings, sometimes with decimals. */
public final class SellerNumbers {
    private SellerNumbers() {}

    public static int toInt(String s) {
        if (s == null) {
            return 0;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(t);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }
}
