package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.rule.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    List<Rule> findByActivateTrue();

    Rule findByName(String name);
}
