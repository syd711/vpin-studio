package de.mephisto.vpin.server.highscores.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.IncrementGenerator;

@Entity
@Table(name = "TemplateMappings")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateMapping {

  @Id
  @GenericGenerator(name = "templatemapping_gen", type = IncrementGenerator.class)
  @GeneratedValue(generator = "templatemapping_gen")
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

  public void setCardTemplate(CardTemplate cardTemplate) {
    try {
      this.templateJson = cardTemplate.toJson();
      this.id = cardTemplate.getId();
    }
    catch (JacksonException jpe) {
      throw new RuntimeException("cannot serialize card template " + cardTemplate.getName(), jpe);
    }
  }

  public CardTemplate getTemplate() {
    try {
      CardTemplate template = CardTemplate.fromJson(CardTemplate.class, templateJson);
      template.setId(id);
      return template;
    } catch (Exception e) {
      throw new RuntimeException("cannot deserialize card template " + id, e);
    }
  }
}
