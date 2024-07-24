package com.natlex.assignment.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobType;

public interface JobRepository extends JpaRepository<Job, String> {

  Optional<Job> findByIdAndJobType(String id, JobType jobType);
}
