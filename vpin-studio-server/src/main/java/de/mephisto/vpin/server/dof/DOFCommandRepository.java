package de.mephisto.vpin.server.dof;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DOFCommandRepository extends JpaRepository<DOFCommand, Long> {

  List<DOFCommand> findByTrigger(@NonNull String trigger);
}
