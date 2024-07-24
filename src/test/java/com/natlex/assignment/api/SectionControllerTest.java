package com.natlex.assignment.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.natlex.assignment.api.controller.SectionController;
import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.api.response.SectionResponse;
import com.natlex.assignment.config.SecurityConfig;
import com.natlex.assignment.service.SectionService;

@WebMvcTest(SectionController.class)
@Import(SecurityConfig.class)
public class SectionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SectionService service;

  private final String url = "/api/v1/sections";

  private String createSectionPostBody;

  @BeforeEach
  void setUp() {
    createSectionPostBody =
        "{\"name\": \"sectionName\",\"geologicalClasses\": [{\"name\": \"name1\",\"code\": \"code1\"},{\"name\": \"name2\",\"code\": \"code2\"}]}";
  }

  @Test
  void createSection_shouldReturnGeologicalClassResponseWith201() throws Exception {

    var response =
        SectionResponse.builder()
            .id(123L)
            .name("name")
            .geologicalClasses(
                List.of(
                    GeologicalClassRequest.builder().name("name1").code("code1").build(),
                    GeologicalClassRequest.builder().name("name2").code("code2").build()))
            .build();
    given(service.saveSection(any(SectionRequest.class))).willReturn(response);

    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSectionPostBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(
            jsonPath("$.geologicalClasses[0].name")
                .value(response.geologicalClasses().get(0).name()))
        .andExpect(
            jsonPath("$.geologicalClasses[0].code")
                .value(response.geologicalClasses().get(0).code()))
        .andExpect(
            jsonPath("$.geologicalClasses[1].name")
                .value(response.geologicalClasses().get(1).name()))
        .andExpect(
            jsonPath("$.geologicalClasses[1].code")
                .value(response.geologicalClasses().get(1).code()));
  }

  @Test
  void createSectionWithNoRequestBody_shouldReturn400() throws Exception {
    mockMvc
        .perform(
            post(url).with(httpBasic("user", "password")).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void createSectionWithoutGeologicalClasses_shouldReturn400() throws Exception {

    var sectionWithoutGeologicalClassesPostBody = "{\"name\": \"sectionName\"}]}";
    mockMvc
        .perform(
            post(url)
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(sectionWithoutGeologicalClassesPostBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("One or more fields have validation errors"));
  }

  @Test
  void createSectionWithInvalidRequestBody_shouldReturn400() throws Exception {
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
  void createSectionWithoutAuth_shouldReturn401() throws Exception {
    mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(401));
  }

  @Test
  void createSectionWithInvalidAuth_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            post(url)
                .with(httpBasic("invalidUser", "invalidPassword"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(401));
  }

  @Test
  void getAllSections_shouldReturnListOfSectionResponse() throws Exception {

    var response =
        List.of(
            SectionResponse.builder()
                .id(123L)
                .name("name1")
                .geologicalClasses(
                    List.of(GeologicalClassRequest.builder().name("n1").code("c1").build()))
                .build(),
            SectionResponse.builder()
                .id(456L)
                .name("name2")
                .geologicalClasses(
                    List.of(GeologicalClassRequest.builder().name("n2").code("c2").build()))
                .build());

    given(service.getAllSections()).willReturn(response);

    mockMvc
        .perform(
            get(url).with(httpBasic("user", "password")).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$.[0].name").value("name1"))
        .andExpect(jsonPath("$.[1].name").value("name2"));
  }

  @Test
  void getSectionById_shouldReturnSectionResponse() throws Exception {

    var response = SectionResponse.builder().id(123L).name("name1").build();
    given(service.getSectionById(anyLong())).willReturn(response);

    mockMvc
        .perform(get(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()));
  }

  @Test
  void getSectionByNonExistingId_shouldReturn404() throws Exception {

    var errorMessage = "Section not found";
    given(service.getSectionById(anyLong())).willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(get(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void getSectionByInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(get(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }

  @Test
  void updateSection_shouldReturnUpdatedSectionResponse() throws Exception {
    var response =
        SectionResponse.builder()
            .id(123L)
            .name("name")
            .geologicalClasses(
                List.of(
                    GeologicalClassRequest.builder().name("name1").code("code1").build(),
                    GeologicalClassRequest.builder().name("name2").code("code2").build()))
            .build();
    given(service.updateSection(anyLong(), any(SectionRequest.class))).willReturn(response);

    mockMvc
        .perform(
            put(url + "/123")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSectionPostBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.name").value(response.name()))
        .andExpect(
            jsonPath("$.geologicalClasses[0].name")
                .value(response.geologicalClasses().get(0).name()))
        .andExpect(
            jsonPath("$.geologicalClasses[0].code")
                .value(response.geologicalClasses().get(0).code()))
        .andExpect(
            jsonPath("$.geologicalClasses[1].name")
                .value(response.geologicalClasses().get(1).name()))
        .andExpect(
            jsonPath("$.geologicalClasses[1].code")
                .value(response.geologicalClasses().get(1).code()));
  }

  @Test
  void updateSectionWithNonExistingId_shouldReturn404() throws Exception {

    var errorMessage = "Section not found";
    given(service.updateSection(anyLong(), any(SectionRequest.class)))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(
            put(url + "/123")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSectionPostBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void updateSectionWithInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(put(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }

  @Test
  void updateSectionWithNoRequestBody_shouldReturn400() throws Exception {

    mockMvc
        .perform(put(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required request body is missing"));
  }

  @Test
  void updateSectionWithInvalidRequestBody_shouldReturn400() throws Exception {

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
  void deleteSection_shouldReturn204() throws Exception {

    mockMvc
        .perform(delete(url + "/123").with(httpBasic("user", "password")))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteSectionWithInvalidId_shouldReturn400() throws Exception {

    mockMvc
        .perform(delete(url + "/invalid-id").with(httpBasic("user", "password")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid value for parameter: id"));
  }

  @Test
  void getSectionsByGeologicalClassCode_shouldReturnListOfSectionResponse() throws Exception {

    var response =
        List.of(
            SectionResponse.builder()
                .id(123L)
                .name("name1")
                .geologicalClasses(
                    List.of(GeologicalClassRequest.builder().name("n1").code("c2").build()))
                .build(),
            SectionResponse.builder()
                .id(456L)
                .name("name2")
                .geologicalClasses(
                    List.of(GeologicalClassRequest.builder().name("n2").code("c2").build()))
                .build());

    given(service.getSectionsByGeologicalClassCode(anyString())).willReturn(response);

    mockMvc
        .perform(get(url + "/by-code").with(httpBasic("user", "password")).param("code", "c2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$.[0].name").value("name1"))
        .andExpect(jsonPath("$.[1].name").value("name2"));
  }
}
