package com.vijay.personal.task.objects;

import java.time.Instant;
import java.util.Objects;

public class Task {
  private String id;
  private String text;
  private Instant createdOn;
  private Instant dueBy;
  private Instant completedOn;

  public String getId() {
    return id;
  }

  public Task setId(String id) {
    this.id = id;
    return this;
  }

  public String getText() {
    return text;
  }

  public Task setText(String text) {
    this.text = text;
    return this;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public Task setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
    return this;
  }

  public Instant getDueBy() {
    return dueBy;
  }

  public Task setDueBy(Instant dueBy) {
    this.dueBy = dueBy;
    return this;
  }

  public Instant getCompletedOn() {
    return completedOn;
  }

  public Task setCompletedOn(Instant completedOn) {
    this.completedOn = completedOn;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return Objects.equals(id, task.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
