package com.meltmedia.dao.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity implements IEntity {
  private Long id;
  private Date createdDate;
  private Date lastModifiedDate;

  @Id
  @GeneratedValue(generator="increment")
  @GenericGenerator(name="increment", strategy="increment")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public Date getCreatedDate() {
    return createdDate;
  }
  
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }
  
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }
  
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
  
  @PreUpdate
  protected void preUpdate() {
    setLastModifiedDate(new Date());
  }
  
  @PrePersist
  protected void prePersist() {
    setCreatedDate(new Date());
    setLastModifiedDate(getCreatedDate());
  }  
}
