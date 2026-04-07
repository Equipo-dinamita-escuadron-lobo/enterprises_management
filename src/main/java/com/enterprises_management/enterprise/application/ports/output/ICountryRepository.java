package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Puerto de salida para el acceso a datos de países.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Repository
public interface ICountryRepository extends JpaRepository<Country, Long> {
}