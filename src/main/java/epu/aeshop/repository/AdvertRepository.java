package epu.aeshop.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Advert;

@Repository
public interface AdvertRepository extends CrudRepository<Advert, Long> {
}
