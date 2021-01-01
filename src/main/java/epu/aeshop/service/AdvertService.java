package epu.aeshop.service;


import java.util.List;

import epu.aeshop.entity.Advert;

public interface AdvertService {

    List<Advert> getAll();

    public Advert saveAdvert(Advert advert);

    public List<Advert> getAdverts();

    public Advert getAdvertById(Long id);

    public void deleteAdvert(Advert advert);

}
