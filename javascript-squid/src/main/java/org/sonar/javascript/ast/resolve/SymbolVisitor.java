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
package org.sonar.javascript.ast.resolve;

import java.util.List;

import org.sonar.javascript.ast.visitors.BaseTreeVisitor;
import org.sonar.javascript.model.implementations.declaration.MethodDeclarationTreeImpl;
import org.sonar.javascript.model.implementations.declaration.ParameterListTreeImpl;
import org.sonar.javascript.model.implementations.statement.CatchBlockTreeImpl;
import org.sonar.javascript.model.interfaces.Tree;
import org.sonar.javascript.model.interfaces.declaration.FunctionDeclarationTree;
import org.sonar.javascript.model.interfaces.declaration.MethodDeclarationTree;
import org.sonar.javascript.model.interfaces.declaration.ScriptTree;
import org.sonar.javascript.model.interfaces.expression.ClassTree;
import org.sonar.javascript.model.interfaces.expression.IdentifierTree;
import org.sonar.javascript.model.interfaces.statement.CatchBlockTree;

public class SymbolVisitor extends BaseTreeVisitor {

  private SymbolModel symbolModel;
  private SymbolModel.Scope currentScope;

  public SymbolVisitor(SymbolModel symbolModel) {
    this.symbolModel = symbolModel;
    this.currentScope = null;
  }

  @Override
  public void visitScript(ScriptTree tree) {
    newScope();
    setScopeForTree(tree);
    super.visitScript(tree);
  }

  @Override
  public void visitClassDeclaration(ClassTree tree) {
    addSymbol(tree.name().name(), tree);
    newScope();

    super.visitClassDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    addSymbol(((MethodDeclarationTreeImpl) tree).nameToString(), tree);
    newScope();
    addSymbols(((ParameterListTreeImpl) tree.parameters()).parameterIdentifiers());

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    setScopeForTree(tree);
    newScope();
    addSymbols(((CatchBlockTreeImpl) tree).parameterIdentifiers());

    super.visitCatchBlock(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    if (!tree.is(Tree.Kind.FUNCTION_EXPRESSION, Tree.Kind.GENERATOR_FUNCTION_EXPRESSION)) {
      addSymbol(tree.name().name(), tree);
      newScope();
      addSymbols(((ParameterListTreeImpl) tree.parameters()).parameterIdentifiers());
    }

    super.visitFunctionDeclaration(tree);
  }

  private void setScopeForTree(Tree tree) {
    symbolModel.setScopeFor(tree, currentScope);
  }

  private void addSymbol(String name, Tree tree) {
    currentScope.addSymbolToScope(name, tree);
    setScopeForTree(tree);
  }

  private void addSymbols(List<IdentifierTree> identifiers) {
    for (IdentifierTree identifier : identifiers) {
      currentScope.addSymbolToScope(identifier.name(), identifier);
      setScopeForTree(identifier);
    }
  }

  private void newScope() {
    SymbolModel.Scope newScope = new SymbolModel.Scope(currentScope);
    if (newScope != null) {
      currentScope.setNext(newScope);
    }
    currentScope = newScope;

  }

}
