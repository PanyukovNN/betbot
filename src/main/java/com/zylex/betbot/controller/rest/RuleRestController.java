package com.zylex.betbot.controller.rest;

import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("rule")
public class RuleRestController {
    //Add rule
    //Edit rule
    //Get rules
    //Create rule

    private final RuleRepository ruleRepository;

    @Autowired
    public RuleRestController(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Rule>> getAll() {
        try {
            return new ResponseEntity<>(ruleRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
