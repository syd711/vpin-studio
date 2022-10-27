package de.mephisto.vpin.server.jpa;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionsRepository extends JpaRepository<Competition, Long> {

}
