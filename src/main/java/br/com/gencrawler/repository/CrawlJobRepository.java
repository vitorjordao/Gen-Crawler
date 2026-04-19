package br.com.gencrawler.repository;

import br.com.gencrawler.model.CrawlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CrawlJobRepository extends JpaRepository<CrawlJob, UUID> {
}
