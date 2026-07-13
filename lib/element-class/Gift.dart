class Gift{
  final int? id;
  final String? foto;
  final String? nama;
  final int? minimalBelanja;
  final String? berlakuMulai;
  final String? berlakuSampai;
  final int? quota;
  final String? deskripsi;
  final int? createdBy;
  final String? createdAt;
  final int? updatedBy;
  final String? updatedAt;

  Gift({this.id, this.foto, this.nama, this.minimalBelanja, this.berlakuMulai, this.berlakuSampai, this.quota, this.deskripsi, this.createdBy, this.createdAt, this.updatedBy, this.updatedAt});

  Gift.fromJson(Map<String, dynamic> json):
    id  = int.tryParse(json['id']),
    foto  = json['foto'],
    nama  = json['nama'],
    minimalBelanja  = int.tryParse(json['minimalBelanja']),
    berlakuMulai  = json['berlakuMulai'],
    berlakuSampai  = json['berlakuSampai'],
    quota  = int.tryParse(json['quota']),
    deskripsi  = json['deskripsi'],
    createdBy  = int.tryParse(json['createdBy']),
    createdAt  = json['createdAt'],
    updatedBy  = json['updatedBy'] == null ? null : int.tryParse(json['updatedBy']),
    updatedAt  = json['updatedAt'];

  Map<String, dynamic> toJson() => {
    'id'  : id,
    'foto'  : foto,
    'nama'  : nama,
    'minimalBelanja'  : minimalBelanja,
    'berlakuMulai'  : berlakuMulai,
    'berlakuSampai'  : berlakuSampai,
    'quota'  : quota,
    'deskripsi'  : deskripsi,
    'createdBy'  : createdBy,
    'createdAt'  : createdAt,
    'updatedBy'  : updatedBy,
    'updatedAt'  : updatedAt
  };
}