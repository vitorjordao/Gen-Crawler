package br.com.gencrawler.repository;

import br.com.gencrawler.model.ExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExtractedDataRepository extends JpaRepository<ExtractedData, UUID> {
    
    List<ExtractedData> findByJobId(UUID jobId);
}
