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
import java.util.Map;

import org.sonar.javascript.model.interfaces.Tree;
import org.sonar.javascript.model.interfaces.declaration.ScriptTree;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class SymbolModel {

  private final Map<Tree, Scope> scopes = Maps.newHashMap();

  public static SymbolModel createFor(ScriptTree script) {
    SymbolModel symbolModel = new SymbolModel();

    new SymbolVisitor(symbolModel).visitScript(script);

    return symbolModel;
  }

  public void setScopeFor(Tree tree, Scope scope) {
    scopes.put(tree, scope);
  }

  public Scope getScopeFor(Tree tree) {
    return scopes.get(tree);
  }

  public static class Scope {

    private Scope outer;
    protected ArrayListMultimap<String, Symbol> symbols = ArrayListMultimap.create();
    private Scope next;

    public Scope(Scope outer) {
      this.outer = outer;
    }

    public Scope outer() {
      return outer;
    }

    public Scope next() {
      return next;
    }

    public void setNext(Scope next) {
      this.next = next;
    }

    public void addSymbolToScope(String name, Tree tree) {
      symbols.put(name, new Symbol(name, tree));
    }

    public List<Symbol> lookupSymbol(String name) {
      Scope scope = this;
      while (scope != null && !scope.symbols.containsKey(name)) {
        scope = scope.outer;
      }
      return scope == null ? ImmutableList.<Symbol>of() : scope.symbols.get(name);
    }

  }

  public static class Symbol {
    private final String name;
    private final Tree tree;
    private int usage;

    public Symbol(String name, Tree tree) {
      this.name = name;
      this.tree = tree;
      this.usage = 0;
    }

    public String name() {
      return name;
    }

    public Tree tree() {
      return tree;
    }

    public int usage() {
      return usage;
    }

    public void increaseUsage() {
      usage++;
    }
  }

}
