package cz.kopidlno.repository;

import cz.kopidlno.model.CastObce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastObceRepository extends JpaRepository<CastObce, Long> {
}
