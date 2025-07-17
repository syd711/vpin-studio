package de.mephisto.vpin.server.vpsdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "VpsEntries")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VpsDbEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  private String vpsTableId;

  private String comment;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getVpsTableId() {
    return vpsTableId;
  }

  public void setVpsTableId(String vpsTableId) {
    this.vpsTableId = vpsTableId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VpsDbEntry entry = (VpsDbEntry) o;
    return Objects.equals(id, entry.id) && Objects.equals(vpsTableId, entry.vpsTableId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, vpsTableId);
  }

  @Override
  public String toString() {
    return "VPS data for table '" + vpsTableId + "'";
  }
}
