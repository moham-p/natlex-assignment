package com.natlex.assignment.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.natlex.assignment.api.controller.GeologicalClassController;
import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.response.GeologicalClassResponse;
import com.natlex.assignment.config.SecurityConfig;
import com.natlex.assignment.service.GeologicalClassService;

@WebMvcTest(GeologicalClassController.class)
@Import(SecurityConfig.class)
public class GeologicalClassControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GeologicalClassService service;

  private final String url = "/api/v1/geologicalClasses";

  private String createGeologicalClassPostBody;

  @BeforeEach
  void setUp() {
    createGeologicalClassPostBody = "{\"name\": \"Geo Class\", \"code\": \"GC01\"}";
  }

  @Test
  void createGeologicalClass_shouldReturnGeologicalClassResponseWith201() throws Exception {

    var response =
        GeologicalClassResponse.builder()
            .id(123L)
            .name("name")
            .code("code")
            .sectionId(456L)
            .build();
    given(service.saveGeologicalClass(any(GeologicalClassRequest.class), anyLong()))
        .willReturn(response);

    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody)
                .param("sectionId", "1"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(jsonPath("$.code").value(response.code()))
        .andExpect(jsonPath("$.sectionId").value(response.sectionId()));
  }

  @Test
  void createGeologicalClassWithNoSectionId_shouldReturn400() throws Exception {
    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Missing parameter: sectionId"));
  }

  @Test
  void createGeologicalClassWithInvalidSectionId_shouldReturn400() throws Exception {
    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody)
                .param("sectionId", "invalid-section-id"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: sectionId"));
  }

  @Test
  void createGeologicalClassWithNonExistingSectionId_shouldReturn404() throws Exception {

    var errorMessage = "Section not found";
    given(service.saveGeologicalClass(any(GeologicalClassRequest.class), anyLong()))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody)
                .param("sectionId", "1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void createGeologicalClassWithNoRequestBody_shouldReturn400() throws Exception {
    mockMvc
        .perform(post(url).with(httpBasic("user", "password")).param("sectionId", "1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void createGeologicalClassWithInvalidRequestBody_shouldReturn400() throws Exception {
    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid body"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void createGeologicalClassWithoutAuth_shouldReturn401() throws Exception {
    mockMvc.perform(post(url)).andExpect(status().is(401));
  }

  @Test
  void createGeologicalClassWithInvalidAuth_shouldReturn401() throws Exception {
    mockMvc
        .perform(post(url).with(httpBasic("invalidUser", "invalidPassword")))
        .andExpect(status().is(401));
  }

  @Test
  void getAllGeologicalClasses_shouldReturnListOfGeologicalClassResponse() throws Exception {

    var response =
        List.of(
            GeologicalClassResponse.builder()
                .id(123L)
                .name("name1")
                .code("code1")
                .sectionId(1L)
                .build(),
            GeologicalClassResponse.builder()
                .id(456L)
                .name("name2")
                .code("code2")
                .sectionId(1L)
                .build());

    given(service.getAllGeologicalClasses()).willReturn(response);

    mockMvc
        .perform(get(url).with(httpBasic("user", "password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$.[0].name").value(response.get(0).name()))
        .andExpect(jsonPath("$.[0].code").value(response.get(0).code()))
        .andExpect(jsonPath("$.[1].name").value(response.get(1).name()))
        .andExpect(jsonPath("$.[1].code").value(response.get(1).code()));
  }

  @Test
  void getGeologicalClassById_shouldReturnGeologicalClassResponse() throws Exception {

    var response =
        GeologicalClassResponse.builder()
            .id(123L)
            .name("name1")
            .code("code1")
            .sectionId(1L)
            .build();

    given(service.getGeologicalClassById(anyLong())).willReturn(response);

    mockMvc
        .perform(get(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(jsonPath("$.code").value(response.code()))
        .andExpect(jsonPath("$.sectionId").value(response.sectionId()));
  }

  @Test
  void getGeologicalClassByNonExistingId_shouldReturn404() throws Exception {

    var errorMessage = "GeologicalClass not found";
    given(service.getGeologicalClassById(anyLong()))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(get(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void getGeologicalClassByInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(get(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }

  @Test
  void updateGeologicalClass_shouldReturnUpdatedGeologicalClassResponse() throws Exception {
    var response =
        GeologicalClassResponse.builder()
            .id(123L)
            .name("name")
            .code("code")
            .sectionId(456L)
            .build();
    given(service.updateGeologicalClass(anyLong(), any(GeologicalClassRequest.class)))
        .willReturn(response);

    mockMvc
        .perform(
            put(url + "/123")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(jsonPath("$.code").value(response.code()))
        .andExpect(jsonPath("$.sectionId").value(response.sectionId()));
  }

  @Test
  void updateGeologicalClassWithNonExistingId_shouldReturn404() throws Exception {

    var errorMessage = "GeologicalClass not found";
    given(service.updateGeologicalClass(anyLong(), any(GeologicalClassRequest.class)))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(
            put(url + "/123")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGeologicalClassPostBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void updateGeologicalClassWithInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(put(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }

  @Test
  void updateGeologicalClassWithNoRequestBody_shouldReturn400() throws Exception {

    mockMvc
        .perform(put(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void updateGeologicalClassWithInvalidRequestBody_shouldReturn400() throws Exception {

    mockMvc
        .perform(
            put(url + "/123")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid body"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void deleteGeologicalClass_shouldReturn204() throws Exception {

    mockMvc
        .perform(delete(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteGeologicalClassWithInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(delete(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }
}
