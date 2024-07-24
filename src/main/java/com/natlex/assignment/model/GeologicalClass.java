package com.natlex.assignment.model;

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
public class GeologicalClass {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geologicalClass_sequence")
  @SequenceGenerator(
      name = "geologicalClass_sequence",
      sequenceName = "geologicalClass_seq",
      allocationSize = 1)
  private Long id;

  private String name;
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "section_id")
  private Section section;
}
