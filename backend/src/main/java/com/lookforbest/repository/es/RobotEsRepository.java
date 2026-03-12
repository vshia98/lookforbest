package com.lookforbest.repository.es;

import com.lookforbest.entity.RobotEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotEsRepository extends ElasticsearchRepository<RobotEsDocument, String> {
}
