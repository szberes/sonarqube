/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualityprofile;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.rule.RuleStatus;
import org.sonar.core.qualityprofile.db.ActiveRuleDto;
import org.sonar.core.qualityprofile.db.ActiveRuleParamDto;
import org.sonar.core.qualityprofile.db.QualityProfileDto;
import org.sonar.core.rule.RuleDto;
import org.sonar.core.rule.RuleParamDto;
import org.sonar.server.exceptions.BadRequestException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

class RuleActivatorContext {

  private RuleDto rule;
  private final Map<String, RuleParamDto> ruleParams = Maps.newHashMap();
  private QualityProfileDto profile, parentProfile;
  private ActiveRuleDto activeRule, parentActiveRule;
  private final Map<String, ActiveRuleParamDto> activeRuleParams = Maps.newHashMap(), parentActiveRuleParams = Maps.newHashMap();

  RuleDto rule() {
    return rule;
  }

  RuleActivatorContext setRule(RuleDto rule) {
    this.rule = rule;
    return this;
  }

  Map<String, RuleParamDto> ruleParamsByKeys() {
    return ruleParams;
  }

  Collection<RuleParamDto> ruleParams() {
    return ruleParams.values();
  }

  RuleActivatorContext setRuleParams(Collection<RuleParamDto> ruleParams) {
    this.ruleParams.clear();
    for (RuleParamDto ruleParam : ruleParams) {
      this.ruleParams.put(ruleParam.getName(), ruleParam);
    }
    return this;
  }

  QualityProfileDto profile() {
    return profile;
  }

  RuleActivatorContext setProfile(QualityProfileDto profile) {
    this.profile = profile;
    return this;
  }

  @CheckForNull
  QualityProfileDto parentProfile() {
    return parentProfile;
  }

  RuleActivatorContext setParentProfile(@Nullable QualityProfileDto p) {
    this.parentProfile = p;
    return this;
  }

  @CheckForNull
  ActiveRuleDto activeRule() {
    return activeRule;
  }

  RuleActivatorContext setActiveRule(@Nullable ActiveRuleDto a) {
    this.activeRule = a;
    return this;
  }

  RuleActivatorContext setParentActiveRule(@Nullable ActiveRuleDto a) {
    this.parentActiveRule = a;
    return this;
  }

  @CheckForNull
  Map<String, ActiveRuleParamDto> activeRuleParamsAsMap() {
    return activeRuleParams;
  }

  Map<String, String> activeRuleParamsAsStringMap() {
    Map<String, String> params = Maps.newHashMap();
    for (Map.Entry<String, ActiveRuleParamDto> param : activeRuleParams.entrySet()) {
      params.put(param.getKey(), param.getValue().getValue());
    }
    return params;
  }

  Map<String, String> parentActiveRuleParamsAsStringMap() {
    Map<String, String> params = Maps.newHashMap();
    for (Map.Entry<String, ActiveRuleParamDto> param : parentActiveRuleParams.entrySet()) {
      params.put(param.getKey(), param.getValue().getValue());
    }
    return params;
  }

  @CheckForNull
  Collection<ActiveRuleParamDto> activeRuleParams() {
    return activeRuleParams.values();
  }

  RuleActivatorContext setActiveRuleParams(@Nullable Collection<ActiveRuleParamDto> a) {
    activeRuleParams.clear();
    if (a != null) {
      for (ActiveRuleParamDto ar : a) {
        this.activeRuleParams.put(ar.getKey(), ar);
      }
    }
    return this;
  }

  RuleActivatorContext setParentActiveRuleParams(@Nullable Collection<ActiveRuleParamDto> a) {
    parentActiveRuleParams.clear();
    if (a != null) {
      for (ActiveRuleParamDto ar : a) {
        this.parentActiveRuleParams.put(ar.getKey(), ar);
      }
    }
    return this;
  }

  String defaultSeverity() {
    return parentActiveRule != null ? parentActiveRule.getSeverityString() : rule.getSeverityString();
  }

  @CheckForNull
  String defaultParam(String paramKey) {
    ActiveRuleParamDto parentParam = parentActiveRuleParams.get(paramKey);
    if (parentParam != null) {
      return parentParam.getValue();
    }
    RuleParamDto ruleParam = ruleParams.get(paramKey);
    if (ruleParam == null) {
      throw new BadRequestException(String.format("Rule parameter %s does not exist", paramKey));
    }
    return ruleParam.getDefaultValue();
  }

  /**
   * True if trying to override an inherited rule but with exactly the same values
   */
  boolean isSameAsParent(RuleActivation activation) {
    if (parentActiveRule == null) {
      return false;
    }
    if (activation.isReset()) {
      return true;
    }
    if (StringUtils.equals(activation.getSeverity(), parentActiveRule.getSeverityString())) {
      return Maps.difference(activation.getParameters(), parentActiveRuleParamsAsStringMap()).areEqual();
    }
    return false;
  }

  void verifyForActivation() {
    if (RuleStatus.REMOVED == rule.getStatus()) {
      throw new BadRequestException("Rule was removed: " + rule.getKey());
    }
    if (rule.isTemplate()) {
      throw new BadRequestException("Rule template can't be activated on a Quality profile: " + rule.getKey());
    }
    if (rule.getKey().isManual()) {
      throw new BadRequestException("Manual rule can't be activated on a Quality profile: " + rule.getKey());
    }
    if (!profile.getLanguage().equals(rule.getLanguage())) {
      throw new BadRequestException(String.format("Rule %s and profile %s have different languages", rule.getKey(), profile.getKey()));
    }

  }
}