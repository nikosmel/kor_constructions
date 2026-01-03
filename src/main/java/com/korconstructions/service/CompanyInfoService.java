package com.korconstructions.service;

import com.korconstructions.dto.FinancialSummaryDTO;
import com.korconstructions.model.CompanyInfo;
import com.korconstructions.model.Payment;
import com.korconstructions.repository.CompanyInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CompanyInfoService {

    private final CompanyInfoRepository companyInfoRepository;
    private final PaymentService paymentService;

    @Autowired
    public CompanyInfoService(CompanyInfoRepository companyInfoRepository, PaymentService paymentService) {
        this.companyInfoRepository = companyInfoRepository;
        this.paymentService = paymentService;
    }

    @Transactional(readOnly = true)
    public CompanyInfo getCompanyInfo() {
        return companyInfoRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    // Return a temporary default without saving
                    CompanyInfo temp = new CompanyInfo();
                    temp.setCompanyName("Kor Constructions");
                    return temp;
                });
    }

    @Transactional
    public CompanyInfo updateCompanyInfo(CompanyInfo companyInfo) {
        // Get existing or create new if none exists
        CompanyInfo existing = companyInfoRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    CompanyInfo newInfo = new CompanyInfo();
                    newInfo.setCompanyName("Kor Constructions");
                    newInfo.setUpdatedAt(LocalDateTime.now());
                    return newInfo;
                });

        // Update fields
        existing.setCompanyName(companyInfo.getCompanyName());
        existing.setTaxId(companyInfo.getTaxId());
        existing.setDoy(companyInfo.getDoy());
        existing.setAddress(companyInfo.getAddress());
        existing.setCity(companyInfo.getCity());
        existing.setPostalCode(companyInfo.getPostalCode());
        existing.setPhone(companyInfo.getPhone());
        existing.setMobile(companyInfo.getMobile());
        existing.setEmail(companyInfo.getEmail());
        existing.setWebsite(companyInfo.getWebsite());
        existing.setDescription(companyInfo.getDescription());
        existing.setStartingCapital(companyInfo.getStartingCapital());
        existing.setSquareMeters(companyInfo.getSquareMeters());
        existing.setUpdatedAt(LocalDateTime.now());

        return companyInfoRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public FinancialSummaryDTO getFinancialSummary() {
        CompanyInfo companyInfo = getCompanyInfo();

        // Calculate total expenses from all payments
        BigDecimal totalExpenses = paymentService.getAllPayments()
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinancialSummaryDTO(
                companyInfo.getStartingCapital(),
                companyInfo.getSquareMeters(),
                totalExpenses
        );
    }
}
