package de.mephisto.vpin.server.highscores.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "TemplateMappings")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String templateJson;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTemplateJson() {
    return templateJson;
  }

  public void setTemplateJson(String templateJson) {
    this.templateJson = templateJson;
  }

  public void setCardTemplate(CardTemplate cardTemplate) throws JsonProcessingException {
    this.templateJson = cardTemplate.toJson();
    this.id = cardTemplate.getId();
  }
}
