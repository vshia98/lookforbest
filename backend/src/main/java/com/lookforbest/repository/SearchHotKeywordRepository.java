package com.lookforbest.repository;

import com.lookforbest.entity.SearchHotKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHotKeywordRepository extends JpaRepository<SearchHotKeyword, Long> {

    Optional<SearchHotKeyword> findByKeyword(String keyword);

    List<SearchHotKeyword> findTop10ByOrderBySearchCountDesc();

    @Modifying
    @Query(value = """
        INSERT INTO search_hot_keywords (keyword, search_count, last_searched_at, created_at, updated_at)
        VALUES (:keyword, 1, NOW(), NOW(), NOW())
        ON DUPLICATE KEY UPDATE
            search_count = search_count + 1,
            last_searched_at = NOW(),
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertKeyword(@Param("keyword") String keyword);
}
