package de.mephisto.vpin.server.competitions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CompetitionsRepository extends JpaRepository<Competition, Long> {

  List<Competition> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date now1, Date now2);

  List<Competition> findByWinnerInitialsIsNotNullAndEndDateLessThanEqualOrderByEndDate(Date now1);

  List<Competition> findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(Date now);

  List<Competition> findByWinnerInitials(String initials);

  List<Competition> findByGameId(int id);
}
