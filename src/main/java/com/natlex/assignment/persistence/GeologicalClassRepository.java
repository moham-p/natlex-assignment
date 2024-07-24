package com.natlex.assignment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natlex.assignment.model.GeologicalClass;

public interface GeologicalClassRepository extends JpaRepository<GeologicalClass, Long> {}
