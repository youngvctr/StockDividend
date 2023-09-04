package com.stock.service;

import com.stock.exception.DividendException;
import com.stock.exception.ErrorResponse;
import com.stock.model.Company;
import com.stock.model.ScrapedResult;
import com.stock.persist.CompanyRepository;
import com.stock.persist.DividendRepository;
import com.stock.persist.entity.CompanyEntity;
import com.stock.persist.entity.DividendEntity;
import com.stock.scraper.Scraper;
import com.stock.type.ErrorCode;
import io.netty.util.internal.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.stock.type.ErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {
    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new DividendException(ErrorCode.COMPANY_ALREADY_EXIST, COMPANY_ALREADY_EXIST.getStatus(), COMPANY_ALREADY_EXIST.getDescription());
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // 1. ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new DividendException(FAIL_SCRAP_TICKER, FAIL_SCRAP_TICKER.getStatus(), FAIL_SCRAP_TICKER.getDescription());
        }

        // 2. 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 3. 스크래핑 결과 반환
        if(this.companyRepository.existsByTicker(ticker)){
            throw new DividendException(COMPANY_ALREADY_EXIST, COMPANY_ALREADY_EXIST.getStatus(), COMPANY_ALREADY_EXIST.getDescription());
        }

        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autocompleteKeyword(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet().stream().collect(Collectors.toList()); //.limit(10)
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)           // 1. 배당금 정보 삭제
                .orElseThrow(() -> new DividendException(ErrorCode.INVALID_REQUEST, INVALID_REQUEST.getStatus(), INVALID_REQUEST.getDescription()));
        // 2. 회사 정보 삭제
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);
        this.deleteAutocompleteKeyword(company.getName());
        return company.getName();
    }
}
