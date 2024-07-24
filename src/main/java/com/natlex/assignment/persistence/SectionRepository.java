package com.natlex.assignment.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.natlex.assignment.model.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {

  @Query("SELECT DISTINCT s FROM Section s JOIN s.geologicalClasses g WHERE g.code = :code")
  List<Section> findByGeologicalClassCode(@Param("code") String code);
}
