package de.mephisto.vpin.server.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, ComponentType> {

  Optional<Component> findByType(ComponentType type);
}
