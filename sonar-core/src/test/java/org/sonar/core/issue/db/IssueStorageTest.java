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
package org.sonar.core.issue.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.issue.internal.DefaultIssueComment;
import org.sonar.api.issue.internal.IssueChangeContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.DateUtils;
import org.sonar.api.utils.Duration;
import org.sonar.core.component.ComponentDto;
import org.sonar.core.persistence.AbstractDaoTestCase;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.persistence.MyBatis;

import java.util.Collection;
import java.util.Date;

public class IssueStorageTest extends AbstractDaoTestCase {

  IssueChangeContext context = IssueChangeContext.createUser(new Date(), "emmerik");

  DbSession session;

  @Before
  public void before() {
    session = getMyBatis().openSession(false);
  }

  @After
  public void after() throws Exception {
    session.close();
  }

  @Test
  public void batch_insert_new_issues() throws Exception {
    FakeBatchSaver saver = new FakeBatchSaver(getMyBatis(), new FakeRuleFinder());

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(true)

      .setRuleKey(RuleKey.of("squid", "AvoidCycle"))
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setReporter("emmerik")
      .setResolution("OPEN")
      .setStatus("OPEN")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

      .setComponentUuid("component-uuid")
      .setProjectUuid("project-uuid")
      .setComponentKey("struts:Action");

    saver.save(issue);

    checkTables("should_insert_new_issues", new String[] {"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void batch_insert_new_issues_with_session() throws Exception {
    FakeBatchSaver saver = new FakeBatchSaver(getMyBatis(), new FakeRuleFinder());

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(true)

      .setRuleKey(RuleKey.of("squid", "AvoidCycle"))
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setReporter("emmerik")
      .setResolution("OPEN")
      .setStatus("OPEN")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

      .setComponentUuid("component-uuid")
      .setProjectUuid("project-uuid")
      .setComponentKey("struts:Action");

    saver.save(session, issue);
    session.commit();

    checkTables("should_insert_new_issues", new String[] {"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void server_insert_new_issues_with_session() throws Exception {
    ComponentDto project = new ComponentDto().setId(10L).setUuid("project-uuid");
    ComponentDto component = new ComponentDto().setId(100L).setUuid("component-uuid");
    FakeServerSaver saver = new FakeServerSaver(getMyBatis(), new FakeRuleFinder(), component, project);

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(true)

      .setRuleKey(RuleKey.of("squid", "AvoidCycle"))
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setReporter("emmerik")
      .setResolution("OPEN")
      .setStatus("OPEN")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

      .setComponentKey("struts:Action")
      .setComponentUuid("component-uuid")
      .setProjectUuid("project-uuid");

    saver.save(session, issue);
    session.commit();

    checkTables("should_insert_new_issues", new String[] {"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void batch_update_issues() throws Exception {
    setupData("should_update_issues");

    FakeBatchSaver saver = new FakeBatchSaver(getMyBatis(), new FakeRuleFinder());

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(false)
      .setChanged(true)

      // updated fields
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setChecksum("FFFFF")
      .setAuthorLogin("simon")
      .setAssignee("loic")
      .setFieldChange(context, "severity", "INFO", "BLOCKER")
      .setReporter("emmerik")
      .setResolution("FIXED")
      .setStatus("RESOLVED")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

      // unmodifiable fields
      .setRuleKey(RuleKey.of("xxx", "unknown"))
      .setComponentKey("not:a:component");

    saver.save(issue);

    checkTables("should_update_issues", new String[] {"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void server_update_issues() throws Exception {
    setupData("should_update_issues");

    ComponentDto project = new ComponentDto().setId(10L);
    ComponentDto component = new ComponentDto().setId(100L);
    FakeServerSaver saver = new FakeServerSaver(getMyBatis(), new FakeRuleFinder(), component, project);

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(false)
      .setChanged(true)

      // updated fields
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setChecksum("FFFFF")
      .setAuthorLogin("simon")
      .setAssignee("loic")
      .setFieldChange(context, "severity", "INFO", "BLOCKER")
      .setReporter("emmerik")
      .setResolution("FIXED")
      .setStatus("RESOLVED")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

      // unmodifiable fields
      .setRuleKey(RuleKey.of("xxx", "unknown"))
      .setComponentKey("not:a:component");

    saver.save(issue);

    checkTables("should_update_issues", new String[] {"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  static class FakeBatchSaver extends IssueStorage {

    protected FakeBatchSaver(MyBatis mybatis, RuleFinder ruleFinder) {
      super(mybatis, ruleFinder);
    }

    @Override
    protected void doInsert(DbSession session, long now, DefaultIssue issue) {
      int ruleId = rule(issue).getId();
      IssueDto dto = IssueDto.toDtoForComputationInsert(issue, 100l, 10l, ruleId, now);

      session.getMapper(IssueMapper.class).insert(dto);
    }

    @Override
    protected void doUpdate(DbSession session, long now, DefaultIssue issue) {
      IssueDto dto = IssueDto.toDtoForUpdate(issue, 10l, now);
      session.getMapper(IssueMapper.class).update(dto);
    }
  }

  static class FakeServerSaver extends IssueStorage {

    private final ComponentDto component;
    private final ComponentDto project;

    protected FakeServerSaver(MyBatis mybatis, RuleFinder ruleFinder, ComponentDto component, ComponentDto project) {
      super(mybatis, ruleFinder);
      this.component = component;
      this.project = project;
    }

    @Override
    protected void doInsert(DbSession session, long now, DefaultIssue issue) {
      int ruleId = rule(issue).getId();
      IssueDto dto = IssueDto.toDtoForServerInsert(issue, component, project, ruleId, now);

      session.getMapper(IssueMapper.class).insert(dto);
    }

    @Override
    protected void doUpdate(DbSession session, long now, DefaultIssue issue) {
      IssueDto dto = IssueDto.toDtoForUpdate(issue, 10l, now);
      session.getMapper(IssueMapper.class).update(dto);
    }
  }

  static class FakeRuleFinder implements RuleFinder {

    @Override
    public Rule findById(int ruleId) {
      return null;
    }

    @Override
    public Rule findByKey(String repositoryKey, String key) {
      return null;
    }

    @Override
    public Rule findByKey(RuleKey key) {
      Rule rule = new Rule().setRepositoryKey(key.repository()).setKey(key.rule());
      rule.setId(200);
      return rule;
    }

    @Override
    public Rule find(RuleQuery query) {
      return null;
    }

    @Override
    public Collection<Rule> findAll(RuleQuery query) {
      return null;
    }
  }
}
