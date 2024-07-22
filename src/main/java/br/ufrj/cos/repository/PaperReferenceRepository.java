package br.ufrj.cos.repository;

import br.ufrj.cos.domain.PaperReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperReferenceRepository extends JpaRepository<PaperReference, Long> {

    @Query(value = "select q from PaperReference as q")
    List<PaperReference> searchAll();

}
