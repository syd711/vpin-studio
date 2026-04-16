package de.mephisto.vpin.server.emulators;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmulatorDetailsRepository extends JpaRepository<EmulatorDetails, Integer> {

  Optional<EmulatorDetails> findByEmulatorId(int emulatorId);

}
