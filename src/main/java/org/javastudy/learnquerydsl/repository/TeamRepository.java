package org.javastudy.learnquerydsl.repository;

import org.javastudy.learnquerydsl.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
