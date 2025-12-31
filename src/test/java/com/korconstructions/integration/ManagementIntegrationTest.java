package com.korconstructions.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korconstructions.model.Customer;
import com.korconstructions.model.Payment;
import com.korconstructions.model.Receipt;
import com.korconstructions.repository.CustomerRepository;
import com.korconstructions.repository.PaymentRepository;
import com.korconstructions.repository.ReceiptRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Management Page Integration Tests")
public class ManagementIntegrationTest {

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

    @BeforeEach
    public void setUp() {
        // Clean up before each test
        receiptRepository.deleteAll();
        paymentRepository.deleteAll();
        customerRepository.deleteAll();
    }

    // ==================== CUSTOMER TESTS ====================

    @Test
    @DisplayName("Should create and retrieve two customers")
    public void testCustomersCreationAndRetrieval() throws Exception {
        // Create first customer
        Customer customer1 = new Customer();
        customer1.setName("Γιώργος Παπαδόπουλος");
        customer1.setPhone("6912345678");
        customer1.setEmail("gpapadopoulos@example.com");
        customer1.setAddress("Λεωφ. Αθηνών 123, Αθήνα");
        customer1.setAfm("123456789");
        customer1.setNotes("Πελάτης από 2020");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Γιώργος Παπαδόπουλος"))
                .andExpect(jsonPath("$.phone").value("6912345678"))
                .andExpect(jsonPath("$.email").value("gpapadopoulos@example.com"))
                .andExpect(jsonPath("$.afm").value("123456789"));

        // Create second customer
        Customer customer2 = new Customer();
        customer2.setName("Μαρία Κωνσταντίνου");
        customer2.setPhone("6987654321");
        customer2.setEmail("mkonstantinou@example.com");
        customer2.setAddress("Πατησίων 45, Αθήνα");
        customer2.setAfm("987654321");
        customer2.setNotes("VIP πελάτης");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Μαρία Κωνσταντίνου"))
                .andExpect(jsonPath("$.phone").value("6987654321"));

        // Retrieve all customers
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "Γιώργος Παπαδόπουλος", "Μαρία Κωνσταντίνου")))
                .andExpect(jsonPath("$[*].afm", containsInAnyOrder(
                        "123456789", "987654321")));
    }

    @Test
    @DisplayName("Should update and delete customer")
    public void testCustomerUpdateAndDelete() throws Exception {
        // Create customer
        Customer customer = new Customer();
        customer.setName("Νίκος Αλεξίου");
        customer.setPhone("6911111111");
        customer.setEmail("nalexiou@example.com");
        customer.setAddress("Εθνικής Αντιστάσεως 10");
        customer.setAfm("111222333");

        String createResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(createResponse, Customer.class);
        Long customerId = createdCustomer.getId();

        // Update customer
        createdCustomer.setName("Νίκος Αλεξίου - Updated");
        createdCustomer.setPhone("6922222222");

        mockMvc.perform(put("/api/customers/" + customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Νίκος Αλεξίου - Updated"))
                .andExpect(jsonPath("$.phone").value("6922222222"));

        // Delete customer
        mockMvc.perform(delete("/api/customers/" + customerId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== RECEIPT TESTS ====================

    @Test
    @DisplayName("Should create and retrieve two receipts")
    public void testReceiptsCreationAndRetrieval() throws Exception {
        // First create a customer
        Customer customer = new Customer();
        customer.setName("Πελάτης Δοκιμής");
        customer.setPhone("6900000000");
        customer.setEmail("test@example.com");
        customer.setAfm("999888777");

        String customerResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(customerResponse, Customer.class);
        Long customerId = createdCustomer.getId();

        // Create first receipt
        Receipt receipt1 = new Receipt();
        receipt1.setCustomerId(customerId);
        receipt1.setCustomerName(createdCustomer.getName());
        receipt1.setDate(LocalDate.of(2024, 1, 15));
        receipt1.setReceiptNumber("AP001");
        receipt1.setAmount(new BigDecimal("1500.00"));
        receipt1.setReason("Προκαταβολή για έργο");
        receipt1.setSignature1("Γιώργος");
        receipt1.setSignature2("Μαρία");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiptNumber").value("AP001"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.reason").value("Προκαταβολή για έργο"));

        // Create second receipt
        Receipt receipt2 = new Receipt();
        receipt2.setCustomerId(customerId);
        receipt2.setCustomerName(createdCustomer.getName());
        receipt2.setDate(LocalDate.of(2024, 2, 20));
        receipt2.setReceiptNumber("AP002");
        receipt2.setAmount(new BigDecimal("2500.50"));
        receipt2.setReason("Εξόφληση έργου");
        receipt2.setSignature1("Νίκος");
        receipt2.setSignature2("Ελένη");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiptNumber").value("AP002"))
                .andExpect(jsonPath("$.amount").value(2500.50));

        // Retrieve all receipts
        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].receiptNumber", containsInAnyOrder("AP001", "AP002")))
                .andExpect(jsonPath("$[*].amount", containsInAnyOrder(1500.00, 2500.50)));
    }

    @Test
    @DisplayName("Should retrieve receipts by customer ID")
    public void testGetReceiptsByCustomerId() throws Exception {
        // Create customer
        Customer customer = new Customer();
        customer.setName("Πελάτης για Αποδείξεις");
        customer.setPhone("6911122233");
        customer.setAfm("555666777");

        String customerResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(customerResponse, Customer.class);
        Long customerId = createdCustomer.getId();

        // Create receipt for this customer
        Receipt receipt = new Receipt();
        receipt.setCustomerId(customerId);
        receipt.setCustomerName(createdCustomer.getName());
        receipt.setDate(LocalDate.now());
        receipt.setReceiptNumber("TEST001");
        receipt.setAmount(new BigDecimal("500.00"));
        receipt.setReason("Test receipt");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt)))
                .andExpect(status().isCreated());

        // Get receipts by customer ID
        mockMvc.perform(get("/api/receipts/customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(customerId))
                .andExpect(jsonPath("$[0].receiptNumber").value("TEST001"));
    }

    // ==================== PAYMENT TESTS ====================

    @Test
    @DisplayName("Should create and retrieve two payments")
    public void testPaymentsCreationAndRetrieval() throws Exception {
        // Create first payment
        Payment payment1 = new Payment();
        payment1.setPayeeName("Εταιρεία Οικοδομικών Υλικών ΑΕ");
        payment1.setDate(LocalDate.of(2024, 3, 10));
        payment1.setPaymentNumber("PL001");
        payment1.setAmount(new BigDecimal("3500.00"));
        payment1.setReason("Αγορά τσιμέντου");
        payment1.setSignature1("Διευθυντής");
        payment1.setSignature2("Λογιστής");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payeeName").value("Εταιρεία Οικοδομικών Υλικών ΑΕ"))
                .andExpect(jsonPath("$.paymentNumber").value("PL001"))
                .andExpect(jsonPath("$.amount").value(3500.00))
                .andExpect(jsonPath("$.reason").value("Αγορά τσιμέντου"));

        // Create second payment
        Payment payment2 = new Payment();
        payment2.setPayeeName("Ηλεκτρολόγος Ιωάννης");
        payment2.setDate(LocalDate.of(2024, 3, 15));
        payment2.setPaymentNumber("PL002");
        payment2.setAmount(new BigDecimal("1200.75"));
        payment2.setReason("Ηλεκτρολογική εγκατάσταση");
        payment2.setSignature1("Γιάννης");
        payment2.setSignature2("Κώστας");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payeeName").value("Ηλεκτρολόγος Ιωάννης"))
                .andExpect(jsonPath("$.paymentNumber").value("PL002"))
                .andExpect(jsonPath("$.amount").value(1200.75));

        // Retrieve all payments
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].paymentNumber", containsInAnyOrder("PL001", "PL002")))
                .andExpect(jsonPath("$[*].amount", containsInAnyOrder(3500.00, 1200.75)));
    }

    @Test
    @DisplayName("Should update and delete payment")
    public void testPaymentUpdateAndDelete() throws Exception {
        // Create payment
        Payment payment = new Payment();
        payment.setPayeeName("Προμηθευτής Test");
        payment.setDate(LocalDate.now());
        payment.setPaymentNumber("TEST001");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setReason("Test payment");

        String createResponse = mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Payment createdPayment = objectMapper.readValue(createResponse, Payment.class);
        Long paymentId = createdPayment.getId();

        // Update payment
        createdPayment.setAmount(new BigDecimal("1500.00"));
        createdPayment.setReason("Updated test payment");

        mockMvc.perform(put("/api/payments/" + paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdPayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.reason").value("Updated test payment"));

        // Delete payment
        mockMvc.perform(delete("/api/payments/" + paymentId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Should handle full management workflow: Create customer, receipt, and payment")
    public void testFullManagementWorkflow() throws Exception {
        // Step 1: Create a customer
        Customer customer = new Customer();
        customer.setName("Ολοκληρωμένος Πελάτης");
        customer.setPhone("6944556677");
        customer.setEmail("full@test.com");
        customer.setAfm("444555666");

        String customerResponse = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Customer createdCustomer = objectMapper.readValue(customerResponse, Customer.class);

        // Step 2: Create a receipt for the customer
        Receipt receipt = new Receipt();
        receipt.setCustomerId(createdCustomer.getId());
        receipt.setCustomerName(createdCustomer.getName());
        receipt.setDate(LocalDate.now());
        receipt.setReceiptNumber("FULL001");
        receipt.setAmount(new BigDecimal("5000.00"));
        receipt.setReason("Πλήρης δοκιμή ροής");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receipt)))
                .andExpect(status().isCreated());

        // Step 3: Create a payment
        Payment payment = new Payment();
        payment.setPayeeName("Προμηθευτής για το έργο");
        payment.setDate(LocalDate.now());
        payment.setPaymentNumber("FULL002");
        payment.setAmount(new BigDecimal("2000.00"));
        payment.setReason("Υλικά για το έργο του πελάτη");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isCreated());

        // Verify all entities exist
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
