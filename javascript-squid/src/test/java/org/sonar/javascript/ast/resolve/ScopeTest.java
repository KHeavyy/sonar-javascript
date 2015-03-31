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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.javascript.model.JavaScriptTreeModelTest;
import org.sonar.javascript.model.implementations.JavaScriptTree;
import org.sonar.javascript.model.interfaces.Tree;
import org.sonar.javascript.model.interfaces.declaration.FunctionDeclarationTree;
import org.sonar.javascript.model.interfaces.declaration.ScriptTree;
import org.sonar.javascript.model.interfaces.expression.FunctionExpressionTree;

import com.sonar.sslr.api.AstNode;

public class ScopeTest extends JavaScriptTreeModelTest {

  private AstNode ROOT_NODE;
  private SymbolModel SYMBOL_MODEL;

  @Before
  public void setUp() throws Exception {
    ROOT_NODE = p.parse(new File("src/test/resources/ast/resolve/scopes.js"));
    SYMBOL_MODEL = SymbolModel.createFor((ScriptTree) ROOT_NODE);
  }

  @Test
  public void global_scope() throws Exception {
    SymbolModel.Scope globalScope = SYMBOL_MODEL.getScopeFor((ScriptTree) ROOT_NODE);

    assertNotNull(globalScope.lookupSymbol("a"));
    assertNotNull(globalScope.lookupSymbol("f"));

//    assertNotNull(globalScope.lookupSymbol("b")); FIXME - implicit declaration
//    assertNotNull(globalScope.lookupSymbol("b")); FIXME - implicit declaration
  }

  @Test
  public void function_scope() throws Exception {
    SymbolModel.Scope functionScope = SYMBOL_MODEL.getScopeFor((FunctionDeclarationTree) ROOT_NODE.getFirstDescendant(Tree.Kind.FUNCTION_DECLARATION));

    assertNotNull(functionScope.lookupSymbol("p"));
    assertNotNull(functionScope.lookupSymbol("a"));
    assertNotNull(functionScope.lookupSymbol("b"));
  }

  @Test
  public void function_expression_scope() throws Exception {
    SymbolModel.Scope functionExprScope = SYMBOL_MODEL.getScopeFor((FunctionExpressionTree) ROOT_NODE.getFirstDescendant(Tree.Kind.FUNCTION_EXPRESSION));

    assertNotNull(functionExprScope.lookupSymbol("a"));

    // redeclared variable
    assertNotNull(functionExprScope.lookupSymbol("x"));
    assertThat(functionExprScope.lookupSymbol("x").declarations()).hasSize(2);
    assertThat(((JavaScriptTree) functionExprScope.lookupSymbol("x").declarations().get(0)).getTokenLine()).isEqualTo(18);
    assertThat(((JavaScriptTree) functionExprScope.lookupSymbol("x").declarations().get(1)).getTokenLine()).isEqualTo(20);
  }

}
