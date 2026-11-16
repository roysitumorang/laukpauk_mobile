package id.laukpauk.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentContactUsBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;

/** HubungiKami.dart: chat with Laukpauk on WhatsApp. */
public class ContactUsFragment extends Fragment {

    private static final String WHATSAPP_URL = "https://wa.me/62618367205";

    private FragmentContactUsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactUsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_contact_title));
        Images.asset(binding.image, "hubungi-kami.png");
        AppContainer c = AppContainer.from(requireContext());
        ContactUsViewModel viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new ContactUsViewModel(c.settingRepository)))
                .get(ContactUsViewModel.class);

        binding.chatWhatsapp.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL)));
            } catch (RuntimeException e) {
                Messages.info(this, getString(R.string.account_url_failed_title),
                        getString(R.string.account_url_failed_message, WHATSAPP_URL), null);
            }
        });
        viewModel.getAvailable().observe(getViewLifecycleOwner(), ok -> {
            binding.progress.setVisibility(ok == null ? View.VISIBLE : View.GONE);
            binding.contact.setVisibility(Boolean.TRUE.equals(ok) ? View.VISIBLE : View.GONE);
            binding.failed.setVisibility(Boolean.FALSE.equals(ok) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
