package de.mephisto.vpin.server.tournaments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentTablesRepository extends JpaRepository<TournamentTableInfo, Long> {

  Optional<TournamentTableInfo> findByTournamentIdAndVpsTableIdAndVpsTableVersionId(long tournamentId, String vpsTableId, String vpsVersionId);

  List<TournamentTableInfo> findByTournamentIdAndStarted(long tournamentId, boolean started);

  void deleteAllByTournamentId(long tournamentId);

  List<TournamentTableInfo> findAllByTournamentId(long tournamentId);
}
