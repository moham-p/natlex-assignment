package com.natlex.assignment.api.response;

import com.natlex.assignment.model.JobState;

import lombok.Builder;

@Builder
public record JobStateResponse(JobState state) {}
