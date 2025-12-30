package com.korconstructions.controller;

import com.korconstructions.model.CompanyInfo;
import com.korconstructions.service.CompanyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    @Autowired
    public CompanyInfoController(CompanyInfoService companyInfoService) {
        this.companyInfoService = companyInfoService;
    }

    @GetMapping
    public ResponseEntity<CompanyInfo> getCompanyInfo() {
        return ResponseEntity.ok(companyInfoService.getCompanyInfo());
    }

    @PutMapping
    public ResponseEntity<CompanyInfo> updateCompanyInfo(@RequestBody CompanyInfo companyInfo) {
        CompanyInfo updated = companyInfoService.updateCompanyInfo(companyInfo);
        return ResponseEntity.ok(updated);
    }
}
