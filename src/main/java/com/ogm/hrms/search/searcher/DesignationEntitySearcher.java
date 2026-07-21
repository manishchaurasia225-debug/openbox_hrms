package com.ogm.hrms.search.searcher;

import com.ogm.hrms.enums.SearchType;
import com.ogm.hrms.repository.DesignationRepository;
import com.ogm.hrms.search.EntitySearcher;
import com.ogm.hrms.search.SearchHit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Global-search source for designations; requires DESIGNATION:VIEW. */
@Component
public class DesignationEntitySearcher implements EntitySearcher {

    private final DesignationRepository designationRepository;

    public DesignationEntitySearcher(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    @Override
    public SearchType type() {
        return SearchType.DESIGNATION;
    }

    @Override
    public String requiredAuthority() {
        return "DESIGNATION:VIEW";
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHit> search(String query, int limit) {
        return designationRepository.search(query, PageRequest.of(0, limit)).stream()
                .map(d -> new SearchHit(SearchType.DESIGNATION, d.getId(), d.getName(), d.getCode()))
                .toList();
    }
}
