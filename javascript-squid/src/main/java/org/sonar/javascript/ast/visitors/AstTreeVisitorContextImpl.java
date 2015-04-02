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
package org.sonar.javascript.ast.visitors;

import java.io.File;

import com.google.common.base.Preconditions;
import org.sonar.javascript.ast.resolve.SymbolModel;
import org.sonar.javascript.model.implementations.JavaScriptTree;
import org.sonar.javascript.model.interfaces.Tree;
import org.sonar.javascript.model.interfaces.declaration.ScriptTree;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.CodeVisitor;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;

public class AstTreeVisitorContextImpl implements AstTreeVisitorContext {
  private final ScriptTree tree;
  private final SourceFile sourceFile;
  private final File file;
  private final SymbolModel symbolModel;

  public AstTreeVisitorContextImpl(ScriptTree tree, SourceFile sourceFile, File file, SymbolModel symbolModel) {
    this.tree = tree;
    this.sourceFile = sourceFile;
    this.file = file;
    this.symbolModel = symbolModel;
  }

  @Override
  public ScriptTree getTree() {
    return tree;
  }

  @Override
  public void addIssue(CodeVisitor check, Tree tree, String message) {
    commonAddIssue(check, getLine(tree), message, -1);
  }

  @Override
  public void addIssue(CodeVisitor check, int line, String message) {
    commonAddIssue(check, line, message, -1);
  }

  @Override
  public void addIssue(CodeVisitor check, Tree tree, String message, double cost){
    commonAddIssue(check, getLine(tree), message, cost);
  }

  @Override
  public void addIssue(CodeVisitor check, int line, String message, double cost){
    commonAddIssue(check, line, message, cost);
  }

  @Override
  public String getFileKey() {
    return sourceFile.getKey();
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public SymbolModel getSymbolModel() {
    return symbolModel;
  }

  /**
   * Cost is set if <code>cost<code/> is more than zero.
   * */
  private void commonAddIssue(CodeVisitor check, int line, String message, double cost){
    Preconditions.checkNotNull(check);
    Preconditions.checkNotNull(message);

    CheckMessage checkMessage = new CheckMessage(check, message);
    if (cost > 0) {
      checkMessage.setCost(cost);
    }

    if (line > 0) {
      checkMessage.setLine(line);
    }

    sourceFile.log(checkMessage);
  }

  private int getLine(Tree tree) {
    return ((JavaScriptTree)tree).getLine();
  }

}
