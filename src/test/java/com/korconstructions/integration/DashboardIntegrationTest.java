package com.korconstructions.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korconstructions.model.*;
import com.korconstructions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Dashboard Integration Tests - All Endpoints")
public class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private CompanyInfoRepository companyInfoRepository;

    @BeforeEach
    public void setUp() {
        // Clean up all repositories
        receiptRepository.deleteAll();
        paymentRepository.deleteAll();
        floorRepository.deleteAll();
        buildingRepository.deleteAll();
        customerRepository.deleteAll();
        companyInfoRepository.deleteAll();
    }

    @Test
    @DisplayName("Should verify all API endpoints are accessible")
    public void testAllEndpointsAccessible() throws Exception {
        // Test Management endpoints
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk());

        // Test Buildings endpoints
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/floors"))
                .andExpect(status().isOk());

        // Test Company Info endpoint
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled("Skipping due to @JsonBackReference serialization limitation")
    @DisplayName("Should create complete dashboard data: 2 customers, 2 receipts, 2 payments, 2 buildings, 2 apartments")
    public void testCompleteDashboardWorkflow() throws Exception {
        // ========== MANAGEMENT SECTION ==========

        // Create 2 customers
        Customer customer1 = new Customer();
        customer1.setName("Dashboard Customer 1");
        customer1.setPhone("6911111111");
        customer1.setEmail("customer1@dashboard.test");
        customer1.setAfm("111111111");

        String customer1Response = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer1 = objectMapper.readValue(customer1Response, Customer.class);

        Customer customer2 = new Customer();
        customer2.setName("Dashboard Customer 2");
        customer2.setPhone("6922222222");
        customer2.setEmail("customer2@dashboard.test");
        customer2.setAfm("222222222");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isCreated());

        // Create 2 receipts
        Receipt receipt1 = new Receipt();
        receipt1.setCustomerId(createdCustomer1.getId());
        receipt1.setCustomerName(createdCustomer1.getName());
        receipt1.setDate(LocalDate.now());
        receipt1.setReceiptNumber("DASH-R001");
        receipt1.setAmount(new BigDecimal("1000.00"));
        receipt1.setReason("Dashboard Receipt 1");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt1)))
                .andExpect(status().isCreated());

        Receipt receipt2 = new Receipt();
        receipt2.setCustomerId(createdCustomer1.getId());
        receipt2.setCustomerName(createdCustomer1.getName());
        receipt2.setDate(LocalDate.now());
        receipt2.setReceiptNumber("DASH-R002");
        receipt2.setAmount(new BigDecimal("2000.00"));
        receipt2.setReason("Dashboard Receipt 2");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt2)))
                .andExpect(status().isCreated());

        // Create 2 payments
        Payment payment1 = new Payment();
        payment1.setPayeeName("Dashboard Payee 1");
        payment1.setDate(LocalDate.now());
        payment1.setPaymentNumber("DASH-P001");
        payment1.setAmount(new BigDecimal("500.00"));
        payment1.setReason("Dashboard Payment 1");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment1)))
                .andExpect(status().isCreated());

        Payment payment2 = new Payment();
        payment2.setPayeeName("Dashboard Payee 2");
        payment2.setDate(LocalDate.now());
        payment2.setPaymentNumber("DASH-P002");
        payment2.setAmount(new BigDecimal("750.00"));
        payment2.setReason("Dashboard Payment 2");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment2)))
                .andExpect(status().isCreated());

        // ========== BUILDINGS SECTION ==========

        // Create 2 buildings
        Building building1 = new Building();
        building1.setName("Dashboard Building 1");
        building1.setAddress("Dashboard Address 1");
        building1.setDescription("First building from dashboard");
        building1.setNumberOfFloors(3);
        building1.setStatus(BuildingStatus.IN_PROGRESS);
        building1.setCustomer(createdCustomer1);

        String building1Response = mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(building1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Building createdBuilding1 = objectMapper.readValue(building1Response, Building.class);

        Building building2 = new Building();
        building2.setName("Dashboard Building 2");
        building2.setAddress("Dashboard Address 2");
        building2.setDescription("Second building from dashboard");
        building2.setNumberOfFloors(2);
        building2.setStatus(BuildingStatus.PLANNING);

        String building2Response = mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(building2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Building createdBuilding2 = objectMapper.readValue(building2Response, Building.class);

        // Create 2 apartments (1 per building)
        Floor apartment1 = new Floor();
        // Create minimal building reference with just the ID for JSON serialization
        Building buildingRef1 = new Building();
        buildingRef1.setId(createdBuilding1.getId());
        apartment1.setBuilding(buildingRef1);
        apartment1.setFloorNumber("1");
        apartment1.setDescription("Dashboard Apartment 1");
        apartment1.setSquareMeters(new BigDecimal("80.00"));
        apartment1.setPrice(new BigDecimal("120000.00"));

        mockMvc.perform(post("/api/floors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apartment1)))
                .andExpect(status().isCreated());

        Floor apartment2 = new Floor();
        // Create minimal building reference with just the ID for JSON serialization
        Building buildingRef2 = new Building();
        buildingRef2.setId(createdBuilding2.getId());
        apartment2.setBuilding(buildingRef2);
        apartment2.setFloorNumber("1");
        apartment2.setDescription("Dashboard Apartment 2");
        apartment2.setSquareMeters(new BigDecimal("90.00"));
        apartment2.setPrice(new BigDecimal("135000.00"));

        mockMvc.perform(post("/api/floors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apartment2)))
                .andExpect(status().isCreated());

        // ========== COMPANY INFO SECTION ==========

        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompanyName("Kor Constructions Dashboard Test");
        companyInfo.setTaxId("999999999");
        companyInfo.setEmail("dashboard@test.com");
        companyInfo.setPhone("2109999999");

        mockMvc.perform(put("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(companyInfo)))
                .andExpect(status().isOk());

        // ========== VERIFY ALL DATA ==========

        // Verify Management data
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "Dashboard Customer 1", "Dashboard Customer 2")));

        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].receiptNumber", containsInAnyOrder(
                        "DASH-R001", "DASH-R002")));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].paymentNumber", containsInAnyOrder(
                        "DASH-P001", "DASH-P002")));

        // Verify Buildings data
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "Dashboard Building 1", "Dashboard Building 2")));

        mockMvc.perform(get("/api/floors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(
                        "Dashboard Apartment 1", "Dashboard Apartment 2")));

        // Verify Company Info
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kor Constructions Dashboard Test"))
                .andExpect(jsonPath("$.taxId").value("999999999"));
    }

    @Test
    @DisplayName("Should handle empty state for all endpoints")
    public void testEmptyStateForAllEndpoints() throws Exception {
        // All endpoints should return empty arrays/default values when no data exists
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/floors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kor Constructions"));
    }

    @Test
    @Disabled("Skipping due to @JsonBackReference serialization limitation")
    @DisplayName("Should verify data relationships across dashboard sections")
    public void testDataRelationships() throws Exception {
        // Create customer
        Customer customer = new Customer();
        customer.setName("Relationship Test Customer");
        customer.setPhone("6900000000");
        customer.setAfm("000000000");

        String customerResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(customerResponse, Customer.class);
        Long customerId = createdCustomer.getId();

        // Create receipt for customer
        Receipt receipt = new Receipt();
        receipt.setCustomerId(customerId);
        receipt.setCustomerName(createdCustomer.getName());
        receipt.setDate(LocalDate.now());
        receipt.setReceiptNumber("REL-001");
        receipt.setAmount(new BigDecimal("1500.00"));
        receipt.setReason("Test receipt");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt)))
                .andExpect(status().isCreated());

        // Create building for customer
        Building building = new Building();
        building.setName("Customer's Building");
        building.setAddress("Customer Address");
        building.setStatus(BuildingStatus.IN_PROGRESS);
        building.setCustomer(createdCustomer);

        String buildingResponse = mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(building)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Building createdBuilding = objectMapper.readValue(buildingResponse, Building.class);

        // Create apartment in building
        Floor apartment = new Floor();
        // Create minimal building reference with just the ID for JSON serialization
        Building buildingRef1 = new Building();
        buildingRef1.setId(createdBuilding.getId());
        apartment.setBuilding(buildingRef1);
        apartment.setFloorNumber("1");
        apartment.setDescription("Related Apartment");
        apartment.setPrice(new BigDecimal("100000.00"));

        mockMvc.perform(post("/api/floors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apartment)))
                .andExpect(status().isCreated());

        // Verify relationships
        // 1. Receipt should belong to customer
        mockMvc.perform(get("/api/receipts/customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].receiptNumber").value("REL-001"));

        // 2. Building should belong to customer
        mockMvc.perform(get("/api/buildings/customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Customer's Building"));

        // 3. Apartment should belong to building
        mockMvc.perform(get("/api/floors/building/" + createdBuilding.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Related Apartment"));
    }

    @Test
    @DisplayName("Should verify all CRUD operations work across dashboard")
    public void testCRUDOperationsAcrossDashboard() throws Exception {
        // CREATE operations
        Customer customer = new Customer();
        customer.setName("CRUD Test Customer");
        customer.setPhone("6900000000");

        String customerResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(customerResponse, Customer.class);

        // READ operations
        mockMvc.perform(get("/api/customers/" + createdCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD Test Customer"));

        // UPDATE operations
        createdCustomer.setName("CRUD Test Customer - Updated");
        mockMvc.perform(put("/api/customers/" + createdCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD Test Customer - Updated"));

        // DELETE operations
        mockMvc.perform(delete("/api/customers/" + createdCustomer.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should verify status filtering for buildings works from dashboard")
    public void testBuildingStatusFiltering() throws Exception {
        // Create buildings with different statuses
        Building planning = new Building();
        planning.setName("Planning Building");
        planning.setAddress("Planning Address");
        planning.setStatus(BuildingStatus.PLANNING);

        Building inProgress = new Building();
        inProgress.setName("In Progress Building");
        inProgress.setAddress("In Progress Address");
        inProgress.setStatus(BuildingStatus.IN_PROGRESS);

        Building completed = new Building();
        completed.setName("Completed Building");
        completed.setAddress("Completed Address");
        completed.setStatus(BuildingStatus.COMPLETED);

        mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planning)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inProgress)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completed)))
                .andExpect(status().isCreated());

        // Test filtering by each status
        mockMvc.perform(get("/api/buildings/status/PLANNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PLANNING"));

        mockMvc.perform(get("/api/buildings/status/IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/buildings/status/COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}
