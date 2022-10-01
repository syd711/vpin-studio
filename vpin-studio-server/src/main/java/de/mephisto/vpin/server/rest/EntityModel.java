package de.mephisto.vpin.server.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 *
 */
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"})
abstract public class EntityModel<T> implements Serializable {
  private final static Logger LOG = LoggerFactory.getLogger(EntityModel.class);

  private String id;
  private Date createdAt;
  private Date updatedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof EntityModel) {
      return ((EntityModel) obj).getId().equals(this.getId());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + getId() + "]";
  }
}
