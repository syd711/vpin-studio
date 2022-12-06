package de.mephisto.vpin.server.competitions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CompetitionsRepository extends JpaRepository<Competition, Long> {

  List<Competition> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date now1, Date now2);

  List<Competition> findByEndDateLessThanEqualOrderByEndDate(Date now1);
}
