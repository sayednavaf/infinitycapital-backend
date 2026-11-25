package com.infinitycapital.api.advisory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvisoryServiceRepository extends JpaRepository<AdvisoryService, Long> {
    List<AdvisoryService> findAllByActiveTrueOrderByIdAsc();
}
