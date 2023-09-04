package com.stock.web;

import com.stock.exception.DividendException;
import com.stock.model.Company;
import com.stock.model.constants.CacheKey;
import com.stock.persist.entity.CompanyEntity;
import com.stock.service.CompanyService;
import com.stock.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import static com.stock.type.ErrorCode.*;

@Slf4j
@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

    /**
     * 자동완성 기능을 위한 prefix 단어 검색 기능
     *
     * @param keyword
     * @return
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(
            @RequestParam String keyword
    ) {
        var resultKeyword = this.companyService.getCompanyNamesByKeyword(keyword);
        var resultAutoCompleteKeyword = this.companyService.autocompleteKeyword(keyword);
        if (resultKeyword.size() == 0 || resultAutoCompleteKeyword.size() == 0) {
            throw new DividendException(INVALID_COMPANY, INVALID_COMPANY.getStatus(), INVALID_COMPANY.getDescription());
        }
        return ResponseEntity.ok(resultAutoCompleteKeyword);//autocomplete(keyword));
    }

    /**
     * 회사 목록 조회
     *
     * @param pageable
     * @return
     */
    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(
            final Pageable pageable
    ) {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        if (companies.getSize() == 0) {
            throw new DividendException(INVALID_COMPANY, INVALID_COMPANY.getStatus(), INVALID_COMPANY.getDescription());
        }
        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     *
     * @param request
     * @return
     */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(
            @RequestBody Company request
    ) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new DividendException(ErrorCode.TICKER_IS_EMPTY, TICKER_IS_EMPTY.getStatus(), TICKER_IS_EMPTY.getDescription());
        }

        Company company = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(company.getName()); //trie 에 company 명이 추가됨
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(
            @PathVariable String ticker
    ) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);
        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
