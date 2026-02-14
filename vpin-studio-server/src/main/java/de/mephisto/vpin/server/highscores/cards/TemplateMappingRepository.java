package de.mephisto.vpin.server.highscores.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateMappingRepository extends JpaRepository<TemplateMapping, Long> {
}
