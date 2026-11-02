package id.laukpauk.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Creates a ViewModel with its dependencies passed to the constructor (manual DI).
 *
 * <pre>
 * AppContainer c = AppContainer.from(requireContext());
 * viewModel = new ViewModelProvider(this,
 *         ViewModelFactory.of(() -&gt; new NotifikasiViewModel(c.notifikasiRepository)))
 *         .get(NotifikasiViewModel.class);
 * </pre>
 * The creator only runs when no ViewModel exists yet for that owner, so state survives rotation.
 */
public final class ViewModelFactory implements ViewModelProvider.Factory {

    /** Builds the ViewModel. */
    public interface Creator<T extends ViewModel> {
        T create();
    }

    private final Creator<? extends ViewModel> creator;

    private ViewModelFactory(Creator<? extends ViewModel> creator) {
        this.creator = creator;
    }

    public static ViewModelFactory of(Creator<? extends ViewModel> creator) {
        return new ViewModelFactory(creator);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ViewModel vm = creator.create();
        if (!modelClass.isInstance(vm)) {
            throw new IllegalArgumentException("Factory creates " + vm.getClass() + ", not " + modelClass);
        }
        return (T) vm;
    }
}
