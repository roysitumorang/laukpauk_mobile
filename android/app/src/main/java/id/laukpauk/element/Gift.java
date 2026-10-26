package id.laukpauk.element;
import com.google.gson.annotations.SerializedName;
/** Java data object corresponding to lib/element-class/Gift.dart. */
public final class Gift {
 @SerializedName("id") public Integer id;
 @SerializedName("foto") public String foto;
 @SerializedName("nama") public String nama;
 @SerializedName("minimalBelanja") public Integer minimalBelanja;
 @SerializedName("berlakuMulai") public String berlakuMulai;
 @SerializedName("berlakuSampai") public String berlakuSampai;
 @SerializedName("quota") public Integer quota;
 @SerializedName("deskripsi") public String deskripsi;
 @SerializedName("createdBy") public Integer createdBy;
 @SerializedName("createdAt") public String createdAt;
 @SerializedName("updatedBy") public Integer updatedBy;
 @SerializedName("updatedAt") public String updatedAt;
 public Gift() {}
}
