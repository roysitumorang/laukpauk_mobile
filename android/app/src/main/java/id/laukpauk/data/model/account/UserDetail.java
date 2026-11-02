package id.laukpauk.data.model.account;

/** The signed-in user ("detailUser" of User/getDetailUser). */
public class UserDetail {
    public String id;
    public String nama;
    public String username;
    public String alamat;
    public String nomorHP;
    public String foto;
    public String kodeReferal;
    public String createdAt;
    /** Server sends "1"/"0". */
    public boolean isSeller;
}
