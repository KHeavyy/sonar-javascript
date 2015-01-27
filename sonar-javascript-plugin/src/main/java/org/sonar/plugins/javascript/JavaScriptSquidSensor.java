/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.javascript;

import com.google.common.collect.Lists;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.checks.NoSonarFilter;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.javascript.EcmaScriptConfiguration;
import org.sonar.javascript.JavaScriptAstScanner;
import org.sonar.javascript.api.EcmaScriptMetric;
import org.sonar.javascript.ast.visitors.SubscriptionAstTreeVisitor;
import org.sonar.javascript.ast.visitors.VisitorsBridge;
import org.sonar.javascript.checks.CheckList;
import org.sonar.javascript.checks.utils.SubscriptionBaseVisitor;
import org.sonar.javascript.metrics.FileLinesVisitor;
import org.sonar.plugins.javascript.core.JavaScript;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.CodeVisitor;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class JavaScriptSquidSensor implements Sensor {

  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  private final AnnotationCheckFactory annotationCheckFactory;
  private final FileLinesContextFactory fileLinesContextFactory;
  private final ResourcePerspectives resourcePerspectives;
  private final ModuleFileSystem moduleFileSystem;
  private final NoSonarFilter noSonarFilter;

  private Project project;
  private SensorContext context;
  private AstScanner<LexerlessGrammar> scanner;

  public JavaScriptSquidSensor(RulesProfile profile, FileLinesContextFactory fileLinesContextFactory,
                               ResourcePerspectives resourcePerspectives, ModuleFileSystem moduleFileSystem, NoSonarFilter noSonarFilter) {
    this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckList.REPOSITORY_KEY, CheckList.getChecks());
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.resourcePerspectives = resourcePerspectives;
    this.moduleFileSystem = moduleFileSystem;
    this.noSonarFilter = noSonarFilter;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return !moduleFileSystem.files(FileQuery.onSource().onLanguage(JavaScript.KEY)).isEmpty();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    this.project = project;
    this.context = context;

    List<CodeVisitor> astNodeVisitors = Lists.newArrayList();
    List<SubscriptionAstTreeVisitor> treeVisitors = Lists.newArrayList();
    Collection<CodeVisitor> squidChecks = annotationCheckFactory.getChecks();

    for (CodeVisitor visitor : squidChecks) {
      if (treeVisitors instanceof SubscriptionBaseVisitor) {
        System.out.println(" sub ---> " + treeVisitors);
        treeVisitors.add((SubscriptionBaseVisitor) visitor);
      } else if (treeVisitors instanceof SquidCheck){
        astNodeVisitors.add((SquidCheck) visitor);
        System.out.println("check ---> " + treeVisitors);
      } else if (treeVisitors instanceof SquidAstVisitor) {
        System.out.println("ast ---> " + treeVisitors);
        astNodeVisitors.add((SquidAstVisitor) visitor);

      }
    }

    astNodeVisitors.add(new VisitorsBridge(treeVisitors));
    astNodeVisitors.add(new FileLinesVisitor(project, fileLinesContextFactory));

    scanner = JavaScriptAstScanner.create(createConfiguration(project), astNodeVisitors.toArray(new SquidAstVisitor[astNodeVisitors.size()]));
    scanner.scanFiles(moduleFileSystem.files(FileQuery.onSource().onLanguage(JavaScript.KEY)));

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles);
  }

  private EcmaScriptConfiguration createConfiguration(Project project) {
    return new EcmaScriptConfiguration(moduleFileSystem.sourceCharset());
  }

  private void save(Collection<SourceCode> squidSourceFiles) {
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;

      File sonarFile = File.fromIOFile(new java.io.File(squidFile.getKey()), project);

      noSonarFilter.addResource(sonarFile, squidFile.getNoSonarTagLines());
      saveClassComplexity(sonarFile, squidFile);
      saveFilesComplexityDistribution(sonarFile, squidFile);
      saveFunctionsComplexityAndDistribution(sonarFile, squidFile);
      saveMeasures(sonarFile, squidFile);
      saveIssues(sonarFile, squidFile);
    }
  }

  private void saveMeasures(File sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(EcmaScriptMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(EcmaScriptMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, squidFile.getDouble(EcmaScriptMetric.CLASSES));
    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(EcmaScriptMetric.FUNCTIONS));
    context.saveMeasure(sonarFile, CoreMetrics.ACCESSORS, squidFile.getDouble(EcmaScriptMetric.ACCESSORS));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(EcmaScriptMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(EcmaScriptMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(EcmaScriptMetric.COMMENT_LINES));
  }

  private void saveClassComplexity(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    double complexityInClasses = 0;
    Set<SourceCode> children = squidFile.getChildren();

    if (children != null) {
      for (SourceCode sourceCode : squidFile.getChildren()) {
        if (sourceCode.isType(SourceClass.class)) {
          complexityInClasses += sourceCode.getDouble(EcmaScriptMetric.COMPLEXITY);
        }
      }
    }
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY_IN_CLASSES, complexityInClasses);
  }

  private void saveFunctionsComplexityAndDistribution(File sonarFile, SourceFile squidFile) {
    Collection<SourceCode> squidFunctionsInFile = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceFunction.class));
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
    double complexityInFunction = 0;
    for (SourceCode squidFunction : squidFunctionsInFile) {
      double functionComplexity = squidFunction.getDouble(EcmaScriptMetric.COMPLEXITY);
      complexityDistribution.add(functionComplexity);
      complexityInFunction += functionComplexity;
    }
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY_IN_FUNCTIONS, complexityInFunction);
 }

  private void saveFilesComplexityDistribution(File sonarFile, SourceFile squidFile) {
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
    complexityDistribution.add(squidFile.getDouble(EcmaScriptMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  private void saveIssues(File sonarFile, SourceFile squidFile) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    if (messages != null) {

      for (CheckMessage message : messages) {
        ActiveRule rule = annotationCheckFactory.getActiveRule(message.getCheck());
        Issuable issuable = resourcePerspectives.as(Issuable.class, sonarFile);

        if (issuable != null) {
          Issue issue = issuable.newIssueBuilder()
            .ruleKey(RuleKey.of(rule.getRepositoryKey(), rule.getRuleKey()))
            .line(message.getLine())
            .message(message.getText(Locale.ENGLISH))
            .build();
          issuable.addIssue(issue);
        }
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
