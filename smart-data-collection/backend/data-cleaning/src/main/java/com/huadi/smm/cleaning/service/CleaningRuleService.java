package com.huadi.smm.cleaning.service;

import com.huadi.smm.common.entity.CleaningRule;

import java.util.List;

public interface CleaningRuleService {

    void addRule(CleaningRule rule);

    List<CleaningRule> listAll();

    void updateRule(CleaningRule rule);

    void deleteRule(Long ruleId);

    void toggleRule(Long ruleId);

    List<CleaningRule> getEnabledRules();
}
