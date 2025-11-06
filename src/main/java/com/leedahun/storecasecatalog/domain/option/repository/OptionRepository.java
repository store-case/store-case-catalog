package com.leedahun.storecasecatalog.domain.option.repository;

import com.leedahun.storecasecatalog.domain.option.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {

}
