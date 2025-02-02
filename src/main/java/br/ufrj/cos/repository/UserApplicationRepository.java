package br.ufrj.cos.repository;

import br.ufrj.cos.domain.UserApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {
    List<UserApplication> findAll();

    // Custom query for filtering by name
    List<UserApplication> findByNameContainingIgnoreCase(String name);

    @Query("SELECT u FROM UserApplication u WHERE u.userName = :userName")
    UserApplication findByUserName(@Param("userName") String userName);

}
