package id.laukpauk.ui.auth;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.auth.AuthResponses;
import id.laukpauk.databinding.ItemAuthPasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** ListPasar: horizontal list of markets, the selected one highlighted. */
class PasarAdapter extends ListAdapter<AuthResponses.Pasar, PasarAdapter.Holder> {

    interface OnClick {
        void onClick(String idPasar);
    }

    private final OnClick onClick;
    private String selected = "0";

    PasarAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<AuthResponses.Pasar>() {
            @Override
            public boolean areItemsTheSame(@NonNull AuthResponses.Pasar a, @NonNull AuthResponses.Pasar b) {
                return String.valueOf(a.id).equals(String.valueOf(b.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull AuthResponses.Pasar a, @NonNull AuthResponses.Pasar b) {
                return String.valueOf(a.nama).equals(String.valueOf(b.nama))
                        && String.valueOf(a.gambar).equals(String.valueOf(b.gambar));
            }
        });
        this.onClick = onClick;
    }

    void setSelected(String id) {
        selected = id == null ? "0" : id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAuthPasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        AuthResponses.Pasar p = getItem(position);
        h.binding.nama.setText(p.nama);
        Images.circle(h.binding.image,
                ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/" + p.gambar);
        h.binding.root.setBackgroundResource(String.valueOf(p.id).equals(selected)
                ? R.drawable.bg_auth_pasar_selected : 0);
        h.binding.root.setOnClickListener(v -> onClick.onClick(String.valueOf(p.id)));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemAuthPasarBinding binding;

        Holder(ItemAuthPasarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
