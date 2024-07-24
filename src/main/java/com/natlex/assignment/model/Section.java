package com.natlex.assignment.model;

import java.util.List;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "section_sequence")
  @SequenceGenerator(name = "section_sequence", sequenceName = "section_seq", allocationSize = 1)
  private Long id;

  @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GeologicalClass> geologicalClasses;

  private String name;
  private String jobId;
}
