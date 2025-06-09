package br.ufrj.cos.repository;

import br.ufrj.cos.domain.PaperReference;
import br.ufrj.cos.views.record.ReferenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperReferenceRepository extends JpaRepository<PaperReference, Long> {

    @Query(value = "select q from PaperReference as q")
    List<PaperReference> searchAll();

    @Query("SELECT NEW br.ufrj.cos.views.record.ReferenceRecord(p.publishYear, COUNT(p), COUNT(p)) " +
            "FROM PaperReference p " +
            "WHERE p.publishYear IS NOT NULL " +
            "GROUP BY p.publishYear " +
            "ORDER BY p.publishYear ASC")
    List<ReferenceRecord> findPaperCountsByYear();


    List<PaperReference> findByTitleContainingIgnoreCase(String title);

    @Query(value = "select p from PaperReference as p order by p.title")
    List<PaperReference> findAllOrderByPaperTitleAsc();

    List<PaperReference> findDistinctByPublishYear(int publishYear);
}
