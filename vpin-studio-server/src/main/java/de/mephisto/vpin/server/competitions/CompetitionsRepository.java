package de.mephisto.vpin.server.competitions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionsRepository extends JpaRepository<Competition, Long> {

  List<Competition> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date now1, Date now2);

  List<Competition> findByEndDateLessThanEqual(Date now);

  List<Competition> findByAndWinnerInitialsIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndType(Date now1, Date now2, String type);

  List<Competition> findByWinnerInitialsIsNotNull();

  List<Competition> findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(Date now);

  List<Competition> findByWinnerInitials(String initials);

  List<Competition> findByWinnerInitialsAndType(String initials, String type);

  List<Competition> findByTypeOrderByEndDateDesc(String type);

  List<Competition> findByGameId(int id);

  Optional<Competition> findByUuid(String id);

  List<Competition> findByTypeAndRomOrderByName(String type, String rom);
}
