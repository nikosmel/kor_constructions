package com.korconstructions.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korconstructions.model.CompanyInfo;
import com.korconstructions.repository.CompanyInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Company Info Page Integration Tests")
public class CompanyInfoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyInfoRepository companyInfoRepository;

    @BeforeEach
    public void setUp() {
        // Clean up before each test
        companyInfoRepository.deleteAll();
    }

    @Test
    @DisplayName("Should get default company info when none exists")
    public void testGetDefaultCompanyInfo() throws Exception {
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kor Constructions"))
                .andExpect(jsonPath("$.id").doesNotExist()); // Default should not have ID yet
    }

    @Test
    @DisplayName("Should create company info on first save")
    public void testCreateCompanyInfo() throws Exception {
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompanyName("Kor Constructions ΑΕ");
        companyInfo.setTaxId("123456789");
        companyInfo.setDoy("Α' Αθηνών");
        companyInfo.setAddress("Λεωφόρος Κηφισίας 100");
        companyInfo.setCity("Αθήνα");
        companyInfo.setPostalCode("15124");
        companyInfo.setPhone("2101234567");
        companyInfo.setMobile("6912345678");
        companyInfo.setEmail("info@korconstructions.gr");
        companyInfo.setWebsite("www.korconstructions.gr");
        companyInfo.setDescription("Εταιρεία κατασκευών με 20 χρόνια εμπειρίας");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(companyInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.companyName").value("Kor Constructions ΑΕ"))
                .andExpect(jsonPath("$.taxId").value("123456789"))
                .andExpect(jsonPath("$.doy").value("Α' Αθηνών"))
                .andExpect(jsonPath("$.address").value("Λεωφόρος Κηφισίας 100"))
                .andExpect(jsonPath("$.city").value("Αθήνα"))
                .andExpect(jsonPath("$.postalCode").value("15124"))
                .andExpect(jsonPath("$.phone").value("2101234567"))
                .andExpect(jsonPath("$.mobile").value("6912345678"))
                .andExpect(jsonPath("$.email").value("info@korconstructions.gr"))
                .andExpect(jsonPath("$.website").value("www.korconstructions.gr"))
                .andExpect(jsonPath("$.description").value("Εταιρεία κατασκευών με 20 χρόνια εμπειρίας"));

        // Verify it was saved in database
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kor Constructions ΑΕ"))
                .andExpect(jsonPath("$.taxId").value("123456789"));
    }

    @Test
    @DisplayName("Should update existing company info without creating duplicates")
    public void testUpdateCompanyInfoNoDuplicates() throws Exception {
        // Create initial company info
        CompanyInfo initialInfo = new CompanyInfo();
        initialInfo.setCompanyName("Initial Company");
        initialInfo.setTaxId("111111111");
        initialInfo.setEmail("initial@test.com");
        initialInfo.setPhone("2101111111");

        String createResponse = mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialInfo)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CompanyInfo createdInfo = objectMapper.readValue(createResponse, CompanyInfo.class);
        Long companyId = createdInfo.getId();

        // Update company info
        CompanyInfo updatedInfo = new CompanyInfo();
        updatedInfo.setCompanyName("Updated Company Name");
        updatedInfo.setTaxId("222222222");
        updatedInfo.setDoy("Β' Αθηνών");
        updatedInfo.setEmail("updated@test.com");
        updatedInfo.setPhone("2102222222");
        updatedInfo.setMobile("6922222222");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId)) // Same ID - no duplicate
                .andExpect(jsonPath("$.companyName").value("Updated Company Name"))
                .andExpect(jsonPath("$.taxId").value("222222222"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // Verify only one record exists in database
        long count = companyInfoRepository.count();
        assert count == 1 : "Expected 1 company info record, but found " + count;
    }

    @Test
    @DisplayName("Should handle multiple updates without creating duplicates")
    public void testMultipleUpdatesNoDuplicates() throws Exception {
        // First save
        CompanyInfo info1 = new CompanyInfo();
        info1.setCompanyName("Company V1");
        info1.setTaxId("111111111");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info1)))
                .andExpect(status().isOk());

        // Second save
        CompanyInfo info2 = new CompanyInfo();
        info2.setCompanyName("Company V2");
        info2.setTaxId("222222222");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info2)))
                .andExpect(status().isOk());

        // Third save
        CompanyInfo info3 = new CompanyInfo();
        info3.setCompanyName("Company V3");
        info3.setTaxId("333333333");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info3)))
                .andExpect(status().isOk());

        // Fourth save
        CompanyInfo info4 = new CompanyInfo();
        info4.setCompanyName("Company V4");
        info4.setTaxId("444444444");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info4)))
                .andExpect(status().isOk());

        // Fifth save
        CompanyInfo info5 = new CompanyInfo();
        info5.setCompanyName("Company V5");
        info5.setTaxId("555555555");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Company V5"))
                .andExpect(jsonPath("$.taxId").value("555555555"));

        // Critical test: Verify only 1 record exists after 5 saves
        long count = companyInfoRepository.count();
        assert count == 1 : "Expected 1 company info record after 5 updates, but found " + count;

        // Verify the latest data is correct
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Company V5"))
                .andExpect(jsonPath("$.taxId").value("555555555"));
    }

    @Test
    @DisplayName("Should update partial company info fields")
    public void testPartialUpdate() throws Exception {
        // Create with all fields
        CompanyInfo fullInfo = new CompanyInfo();
        fullInfo.setCompanyName("Full Company");
        fullInfo.setTaxId("999999999");
        fullInfo.setDoy("Γ' Αθηνών");
        fullInfo.setAddress("Original Address");
        fullInfo.setCity("Αθήνα");
        fullInfo.setPostalCode("12345");
        fullInfo.setPhone("2109999999");
        fullInfo.setMobile("6999999999");
        fullInfo.setEmail("full@test.com");
        fullInfo.setWebsite("www.full.com");
        fullInfo.setDescription("Full description");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullInfo)))
                .andExpect(status().isOk());

        // Update only some fields
        CompanyInfo partialInfo = new CompanyInfo();
        partialInfo.setCompanyName("Full Company"); // Keep same
        partialInfo.setTaxId("999999999"); // Keep same
        partialInfo.setDoy("Δ' Αθηνών"); // CHANGED
        partialInfo.setAddress("New Address 123"); // CHANGED
        partialInfo.setCity("Αθήνα"); // Keep same
        partialInfo.setPostalCode("54321"); // CHANGED
        partialInfo.setPhone("2101111111"); // CHANGED
        partialInfo.setMobile("6911111111"); // CHANGED
        partialInfo.setEmail("full@test.com"); // Keep same
        partialInfo.setWebsite("www.full.com"); // Keep same
        partialInfo.setDescription("Updated description"); // CHANGED

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doy").value("Δ' Αθηνών"))
                .andExpect(jsonPath("$.address").value("New Address 123"))
                .andExpect(jsonPath("$.postalCode").value("54321"))
                .andExpect(jsonPath("$.phone").value("2101111111"))
                .andExpect(jsonPath("$.mobile").value("6911111111"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        // Verify unchanged fields remain
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Full Company"))
                .andExpect(jsonPath("$.taxId").value("999999999"))
                .andExpect(jsonPath("$.email").value("full@test.com"))
                .andExpect(jsonPath("$.website").value("www.full.com"));
    }

    @Test
    @DisplayName("Should handle null/empty values in update")
    public void testUpdateWithNullValues() throws Exception {
        // Create with values
        CompanyInfo initialInfo = new CompanyInfo();
        initialInfo.setCompanyName("Test Company");
        initialInfo.setTaxId("123456789");
        initialInfo.setWebsite("www.test.com");
        initialInfo.setDescription("Some description");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialInfo)))
                .andExpect(status().isOk());

        // Update with null values for some fields
        CompanyInfo updatedInfo = new CompanyInfo();
        updatedInfo.setCompanyName("Test Company");
        updatedInfo.setTaxId("123456789");
        updatedInfo.setWebsite(null); // Set to null
        updatedInfo.setDescription(""); // Set to empty

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.taxId").value("123456789"));

        // Verify only one record
        long count = companyInfoRepository.count();
        assert count == 1 : "Expected 1 company info record, but found " + count;
    }

    @Test
    @DisplayName("Should maintain updatedAt timestamp on update")
    public void testUpdatedAtTimestamp() throws Exception {
        // Create company info
        CompanyInfo initialInfo = new CompanyInfo();
        initialInfo.setCompanyName("Timestamp Test Company");
        initialInfo.setTaxId("777777777");

        String createResponse = mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CompanyInfo createdInfo = objectMapper.readValue(createResponse, CompanyInfo.class);
        String firstUpdatedAt = createdInfo.getUpdatedAt().toString();

        // Wait a bit to ensure timestamp changes
        Thread.sleep(1000);

        // Update company info
        CompanyInfo updatedInfo = new CompanyInfo();
        updatedInfo.setCompanyName("Timestamp Test Company - Updated");
        updatedInfo.setTaxId("888888888");

        String updateResponse = mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CompanyInfo finalInfo = objectMapper.readValue(updateResponse, CompanyInfo.class);
        String secondUpdatedAt = finalInfo.getUpdatedAt().toString();

        // Verify updatedAt changed
        assert !firstUpdatedAt.equals(secondUpdatedAt) :
            "updatedAt should change on update. First: " + firstUpdatedAt + ", Second: " + secondUpdatedAt;
    }
}
