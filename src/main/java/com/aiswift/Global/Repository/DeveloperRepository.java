package com.aiswift.Global.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Global.Entity.Developer;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
	
	boolean existsByEmail(String email);

	Optional<Developer> findByResetToken(String token);

	Optional<Developer> findByEmail(String email);

}
