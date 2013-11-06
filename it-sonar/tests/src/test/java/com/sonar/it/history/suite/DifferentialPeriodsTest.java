/*
 * Copyright (C) 2009-2012 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.history.suite;

import com.sonar.it.ItUtils;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.selenium.Selenese;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class DifferentialPeriodsTest {

  @ClassRule
  public static Orchestrator orchestrator = HistoryTestSuite.ORCHESTRATOR;

  @Before
  public void cleanUpAnalysisData() {
    orchestrator.getDatabase().truncateInspectionTables();
  }

  /**
   * SONAR-4700
   */
  @Test
  public void not_display_periods_selection_dropdown_on_first_analysis() {
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample")).withoutDynamicAnalysis());

    orchestrator.executeSelenese(Selenese.builder().setHtmlTestsInClasspath("not-display-periods-selection-dropdown-on-first-analysis",
      "/selenium/history/differential-periods/not-display-periods-selection-dropdown-on-dashboard.html",
      "/selenium/history/differential-periods/not-display-periods-selection-dropdown-on-issues-drilldown.html"
    ).build());
  }

  /**
   * SONAR-4700
   */
  @Test
  public void display_periods_selection_dropdown_after_first_analysis() {
    SonarRunner scan = SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample")).withoutDynamicAnalysis();
    orchestrator.executeBuilds(scan, scan);

    orchestrator.executeSelenese(Selenese.builder().setHtmlTestsInClasspath("display-periods-selection-dropdown-after-first-analysis",
      "/selenium/history/differential-periods/display-periods-selection-dropdown-on-dashboard.html",
      "/selenium/history/differential-periods/display-periods-selection-dropdown-on-issues-drilldown.html"
    ).build());
  }

  /**
   * SONAR-4564
   */
  @Test
  public void new_issues_measures() throws Exception {
    // This test assumes that period 1 is "since previous analysis" and 2 is "over x days"

    // Execute an analysis in the past to have a past snapshot
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample"))
      .setProperty("sonar.projectDate", "2013-01-01"));

    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/one-issue-per-line-profile.xml"));
    SonarRunner scan = SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample")).setProfile("one-issue-per-line");
    orchestrator.executeBuild(scan);

    assertThat(orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create()).list()).isNotEmpty();
    Resource file = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("sample:sample/Sample.xoo", "new_violations").setIncludeTrends(true));
    List<Measure> measures = file.getMeasures();
    assertThat(measures.get(0).getVariation1().intValue()).isEqualTo(13);
    assertThat(measures.get(0).getVariation2().intValue()).isEqualTo(13);

    // second analysis, with exactly the same profile -> no new issues
    orchestrator.executeBuild(scan);

    assertThat(orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create()).list()).isNotEmpty();
    file = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("sample:sample/Sample.xoo", "new_violations").setIncludeTrends(true));
    // No variation => measure is purged
    assertThat(file).isNull();
  }

  @Test
  public void new_issues_measures_should_be_zero_on_project_when_no_new_issues_since_30_days() throws Exception {
    // This test assumes that period 1 is "since previous analysis" and 2 is "over 30 days"

    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/one-issue-per-line-profile.xml"));
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample"))
      .setProfile("one-issue-per-line")
        // Analyse a project in the past, with a date older than 30 last days (second period)
      .setProperty("sonar.projectDate", "2013-01-01"));
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-sample"))
      .setProfile("one-issue-per-line"));

    // new issues measures should be to 0 on project on 2 periods as new issues has been created
    Resource file = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("sample", "new_violations").setIncludeTrends(true));
    List<Measure> measures = file.getMeasures();
    Measure newIssues = find(measures, "new_violations");
    assertThat(newIssues.getVariation1().intValue()).isEqualTo(0);
    assertThat(newIssues.getVariation2().intValue()).isEqualTo(0);
  }

  @Test
  public void new_issues_measures_on_since_30_days_period() throws Exception {
    // This test assumes that period 1 is "since previous analysis" and 2 is "over 30 days"

    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/one-issue-per-line-profile.xml"));

    // Execute a analysis in the past, without some modules
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-multi-modules-sample"))
      .setProfile("one-issue-per-line")
        // date older than 30 last days (previous second period)
      .setProperty("sonar.projectDate", "2013-01-01")
      .setProperty("sonar.skippedModules", "multi-modules-sample:module_b,multi-modules-sample:module_a:module_a2")
    );

    // Execute a analysis in the past, with one more module than previous analysis
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-multi-modules-sample"))
      .setProfile("one-issue-per-line")
        // date older than 20 last days (just after second period)
      .setProperty("sonar.projectDate", new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -20)))
      .setProperty("sonar.skippedModules", "multi-modules-sample:module_b")
    );

    // Execute a analysis in the present with all modules
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-multi-modules-sample"))
      .setProfile("one-issue-per-line")
    );

    // new issues measures should be to 0 on project on 2 periods as new issues has been created
    Resource file = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("com.sonarsource.it.samples:multi-modules-sample", "violations", "new_violations").setIncludeTrends(true));
    List<Measure> measures = file.getMeasures();

    Measure issues = find(measures, "violations");
    assertThat(issues.getValue().intValue()).isEqualTo(52);
    assertThat(issues.getVariation1().intValue()).isEqualTo(24);
    assertThat(issues.getVariation2().intValue()).isEqualTo(24);

    Measure newIssues = find(measures, "new_violations");
    assertThat(newIssues.getVariation1().intValue()).isEqualTo(24);
    assertThat(newIssues.getVariation2().intValue()).isEqualTo(24);
  }

  /**
   * SONAR-3647
   */
  @Test
  public void new_issues_measures_consistent_with_variations() throws Exception {
    // This test assumes that period 1 is "since previous analysis" and 2 is "over x days"
    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/DifferentialPeriodsTest/issue-on-tag-foobar.xml"));

    // Execute an analysis in the past to have a past snapshot
    // version 1
    SonarRunner firstScan = SonarRunner.create(ItUtils.locateProjectDir("history/xoo-tracking-v1"))
      .setProfile("issue-on-tag-foobar");
    orchestrator.executeBuilds(firstScan);

    // version 2 with 2 new violations and 3 more ncloc
    SonarRunner secondScan = SonarRunner.create(ItUtils.locateProjectDir("history/xoo-tracking-v2"))
      .setProfile("issue-on-tag-foobar");
    orchestrator.executeBuilds(secondScan);

    assertThat(orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create()).list()).isNotEmpty();
    Resource file = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("sample", "new_violations", "violations", "ncloc").setIncludeTrends(true));
    List<Measure> measures = file.getMeasures();
    Measure newIssues = find(measures, "new_violations");
    assertThat(newIssues.getVariation1().intValue()).isEqualTo(2);
    assertThat(newIssues.getVariation2().intValue()).isEqualTo(2);

    Measure violations = find(measures, "violations");
    assertThat(violations.getValue().intValue()).isEqualTo(3);
    assertThat(violations.getVariation1().intValue()).isEqualTo(2);
    assertThat(violations.getVariation2().intValue()).isEqualTo(2);

    Measure ncloc = find(measures, "ncloc");
    assertThat(ncloc.getValue().intValue()).isEqualTo(16);
    assertThat(ncloc.getVariation1().intValue()).isEqualTo(3);
    assertThat(ncloc.getVariation2().intValue()).isEqualTo(3);
  }

  @Test
  public void new_issues_measures_should_be_correctly_calculated_when_adding_a_new_module() throws Exception {
    // This test assumes that period 1 is "since previous analysis"

    // First analysis without module b
    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/DifferentialPeriodsTest/profile1.xml"));
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-multi-modules-sample"))
      .setProfile("profile1")
      .setProperties("sonar.skippedModules", "multi-modules-sample:module_b"));

    // Second analysis with module b and with a new rule activated to have new issues on module a since last analysis
    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/history/DifferentialPeriodsTest/profile2.xml"));
    orchestrator.executeBuild(SonarRunner.create(ItUtils.locateProjectDir("shared/xoo-multi-modules-sample"))
      .setProfile("profile2"));

    Resource project = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("com.sonarsource.it.samples:multi-modules-sample", "new_violations", "violations").setIncludeTrends(true));
    List<Measure> measures = project.getMeasures();
    Measure newIssues = find(measures, "new_violations");
    assertThat(newIssues.getVariation1().intValue()).isEqualTo(57);
  }

  private Measure find(List<Measure> measures, String metricKey) {
    for (Measure measure : measures) {
      if (measure.getMetricKey().equals(metricKey)) {
        return measure;
      }
    }
    return null;
  }
}
