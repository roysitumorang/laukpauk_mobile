package id.laukpauk.ui.home;

import id.laukpauk.data.model.home.LowestPrice;
import id.laukpauk.data.repository.BaseRepository;

/** Loads the lowest price of a product tile ({@code HomeRepository::getLowestPrice}). */
public interface PriceSource {
    void load(String idProduk, BaseRepository.ResultCallback<LowestPrice> callback);
}
