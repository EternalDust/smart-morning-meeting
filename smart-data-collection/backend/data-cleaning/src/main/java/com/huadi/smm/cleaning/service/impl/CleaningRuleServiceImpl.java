package com.huadi.smm.cleaning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.cleaning.dao.CleaningRuleDao;
import com.huadi.smm.cleaning.service.CleaningRuleService;
import com.huadi.smm.common.entity.CleaningRule;
import com.huadi.smm.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CleaningRuleServiceImpl implements CleaningRuleService {

    @Autowired
    private CleaningRuleDao cleaningRuleDao;

    @Override
    public void addRule(CleaningRule rule) {
        cleaningRuleDao.insert(rule);
    }

    @Override
    public List<CleaningRule> listAll() {
        LambdaQueryWrapper<CleaningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CleaningRule::getPriority);
        return cleaningRuleDao.selectList(wrapper);
    }

    @Override
    public void updateRule(CleaningRule rule) {
        if (cleaningRuleDao.selectById(rule.getRuleId()) == null) {
            throw new BusinessException(404, "清洗规则不存在");
        }
        cleaningRuleDao.updateById(rule);
    }

    @Override
    public void deleteRule(Long ruleId) {
        if (cleaningRuleDao.selectById(ruleId) == null) {
            throw new BusinessException(404, "清洗规则不存在");
        }
        cleaningRuleDao.deleteById(ruleId);
    }

    @Override
    public void toggleRule(Long ruleId) {
        CleaningRule rule = cleaningRuleDao.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(404, "清洗规则不存在");
        }
        rule.setEnabled(rule.getEnabled() == 1 ? 0 : 1);
        cleaningRuleDao.updateById(rule);
    }

    @Override
    public List<CleaningRule> getEnabledRules() {
        LambdaQueryWrapper<CleaningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CleaningRule::getEnabled, 1).orderByAsc(CleaningRule::getPriority);
        return cleaningRuleDao.selectList(wrapper);
    }
}
