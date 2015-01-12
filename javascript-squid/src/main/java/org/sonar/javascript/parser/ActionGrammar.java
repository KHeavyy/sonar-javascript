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
package org.sonar.javascript.parser;

import com.sonar.sslr.api.AstNode;
import org.sonar.javascript.api.EcmaScriptKeyword;
import org.sonar.javascript.api.EcmaScriptPunctuator;
import org.sonar.javascript.api.EcmaScriptTokenType;
import org.sonar.javascript.ast.parser.TreeFactory;
import org.sonar.javascript.model.implementations.declaration.ParameterListTreeImpl;
import org.sonar.javascript.model.implementations.expression.ArrayLiteralTreeImpl;
import org.sonar.javascript.model.implementations.expression.ArrowFunctionTreeImpl;
import org.sonar.javascript.model.implementations.expression.BracketMemberExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.ClassExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.ComputedPropertyNameTreeImpl;
import org.sonar.javascript.model.implementations.expression.DotMemberExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.FunctionExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.IdentifierTreeImpl;
import org.sonar.javascript.model.implementations.expression.LiteralTreeImpl;
import org.sonar.javascript.model.implementations.expression.ObjectLiteralTreeImpl;
import org.sonar.javascript.model.implementations.expression.PairPropertyTreeImpl;
import org.sonar.javascript.model.implementations.expression.ParenthesisedExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.RestElementTreeImpl;
import org.sonar.javascript.model.implementations.expression.SuperTreeImpl;
import org.sonar.javascript.model.implementations.expression.TaggedTemplateTreeImpl;
import org.sonar.javascript.model.implementations.expression.TemplateCharactersTreeImpl;
import org.sonar.javascript.model.implementations.expression.TemplateExpressionTreeImpl;
import org.sonar.javascript.model.implementations.expression.TemplateLiteralTreeImpl;
import org.sonar.javascript.model.implementations.expression.ThisTreeImpl;
import org.sonar.javascript.model.implementations.expression.YieldExpressionTreeImpl;
import org.sonar.javascript.model.implementations.statement.BlockTreeImpl;
import org.sonar.javascript.model.implementations.statement.BreakStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.CaseClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.CatchBlockTreeImpl;
import org.sonar.javascript.model.implementations.statement.ContinueStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.DebuggerStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.DefaultClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.DoWhileStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ElseClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.EmptyStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ExpressionStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForInStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForOfStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.IfStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.LabelledStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ReturnStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.SwitchStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ThrowStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.TryStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.VariableDeclarationTreeImpl;
import org.sonar.javascript.model.implementations.statement.VariableStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.WhileStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.WithStatementTreeImpl;
import org.sonar.javascript.model.interfaces.Tree.Kind;
import org.sonar.javascript.model.interfaces.expression.CallExpressionTree;
import org.sonar.javascript.model.interfaces.expression.ExpressionTree;
import org.sonar.javascript.model.interfaces.expression.MemberExpressionTree;
import org.sonar.javascript.model.interfaces.statement.DebuggerStatementTree;
import org.sonar.javascript.model.interfaces.statement.StatementTree;
import org.sonar.javascript.parser.sslr.GrammarBuilder;

public class ActionGrammar {

  private final GrammarBuilder b;
  private final TreeFactory f;

  public ActionGrammar(GrammarBuilder b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  /**
   * A.4 Statement
   */

  public EmptyStatementTreeImpl EMPTY_STATEMENT() {
    return b.<EmptyStatementTreeImpl>nonterminal(Kind.EMPTY_STATEMENT)
      .is(f.emptyStatement(b.invokeRule(EcmaScriptPunctuator.SEMI)));
  }

  public DebuggerStatementTree DEBUGGER_STATEMENT() {
    return b.<DebuggerStatementTreeImpl>nonterminal(Kind.DEBUGGER_STATEMENT)
      .is(f.debuggerStatement(b.invokeRule(EcmaScriptKeyword.DEBUGGER), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public VariableStatementTreeImpl VARIABLE_STATEMENT() {
    return b.<VariableStatementTreeImpl>nonterminal(Kind.VARIABLE_STATEMENT)
      .is(f.completeVariableStatement(b.invokeRule(EcmaScriptKeyword.VAR), VARIABLE_DECLARATION_LIST(), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public VariableStatementTreeImpl VARIABLE_DECLARATION_LIST() {
    return b.<VariableStatementTreeImpl>nonterminal(EcmaScriptGrammar.VARIABLE_DECLARATION_LIST)
      .is(f.newVariableStatement(VARIABLE_DECLARATION(), b.zeroOrMore(f.newTuple1(b.invokeRule(EcmaScriptPunctuator.COMMA), VARIABLE_DECLARATION()))));
  }

  public VariableDeclarationTreeImpl VARIABLE_DECLARATION() {
    return b.<VariableDeclarationTreeImpl>nonterminal(Kind.VARIABLE_DECLARATION)
      .is(f.variableDeclaration(b.firstOf(b.invokeRule(EcmaScriptGrammar.BINDING_IDENTIFIER_INITIALISER), b.invokeRule(EcmaScriptGrammar.BINDING_PATTERN_INITIALISER))));
  }

  public LabelledStatementTreeImpl LABELLED_STATEMENT() {
    return b.<LabelledStatementTreeImpl>nonterminal(Kind.LABELLED_STATEMENT)
      .is(f.labelledStatement(LABEL_IDENTIFIER(), b.invokeRule(EcmaScriptPunctuator.COLON), STATEMENT()));
  }

  public ContinueStatementTreeImpl CONTINUE_STATEMENT() {
    return b.<ContinueStatementTreeImpl>nonterminal(Kind.CONTINUE_STATEMENT)
      .is(f.completeContinueStatement(
        b.invokeRule(EcmaScriptKeyword.CONTINUE),
        b.firstOf(
          CONTINUE_WITH_LABEL(),
          CONTINUE_WITHOUT_LABEL())
      ));
  }

  public ContinueStatementTreeImpl CONTINUE_WITH_LABEL() {
    return b.<ContinueStatementTreeImpl>nonterminal()
      .is(f.newContinueWithLabel(
        b.invokeRule(EcmaScriptGrammar.IDENTIFIER_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ContinueStatementTreeImpl CONTINUE_WITHOUT_LABEL() {
    return b.<ContinueStatementTreeImpl>nonterminal()
      .is(f.newContinueWithoutLabel(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public BreakStatementTreeImpl BREAK_STATEMENT() {
    return b.<BreakStatementTreeImpl>nonterminal(Kind.BREAK_STATEMENT)
      .is(f.completeBreakStatement(
        b.invokeRule(EcmaScriptKeyword.BREAK),
        b.firstOf(
          BREAK_WITH_LABEL(),
          BREAK_WITHOUT_LABEL())
      ));
  }

  public BreakStatementTreeImpl BREAK_WITH_LABEL() {
    return b.<BreakStatementTreeImpl>nonterminal()
      .is(f.newBreakWithLabel(
        b.invokeRule(EcmaScriptGrammar.IDENTIFIER_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public BreakStatementTreeImpl BREAK_WITHOUT_LABEL() {
    return b.<BreakStatementTreeImpl>nonterminal()
      .is(f.newBreakWithoutLabel(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public ReturnStatementTreeImpl RETURN_STATEMENT() {
    return b.<ReturnStatementTreeImpl>nonterminal(Kind.RETURN_STATEMENT)
      .is(f.completeReturnStatement(
        b.invokeRule(EcmaScriptKeyword.RETURN),
        b.firstOf(
          RETURN_WITH_EXPRESSION(),
          RETURN_WITHOUT_EXPRESSION())
      ));
  }

  public ReturnStatementTreeImpl RETURN_WITH_EXPRESSION() {
    return b.<ReturnStatementTreeImpl>nonterminal()
      .is(f.newReturnWithExpression(
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ReturnStatementTreeImpl RETURN_WITHOUT_EXPRESSION() {
    return b.<ReturnStatementTreeImpl>nonterminal()
      .is(f.newReturnWithoutExpression(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public ThrowStatementTreeImpl THROW_STATEMENT() {
    return b.<ThrowStatementTreeImpl>nonterminal(Kind.THROW_STATEMENT)
      .is(f.newThrowStatement(
        b.invokeRule(EcmaScriptKeyword.THROW),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public WithStatementTreeImpl WITH_STATEMENT() {
    return b.<WithStatementTreeImpl>nonterminal(Kind.WITH_STATEMENT)
      .is(f.newWithStatement(
        b.invokeRule(EcmaScriptKeyword.WITH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public BlockTreeImpl BLOCK() {
    return b.<BlockTreeImpl>nonterminal(Kind.BLOCK)
      .is(f.newBlock(b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE), b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST)), b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public TryStatementTreeImpl TRY_STATEMENT() {
    return b.<TryStatementTreeImpl>nonterminal(Kind.TRY_STATEMENT)
      .is(f.completeTryStatement(
        b.invokeRule(EcmaScriptKeyword.TRY),
        BLOCK(),
        b.firstOf(
          f.newTryStatementWithCatch(CATCH_CLAUSE(), b.optional(FINALLY_CLAUSE())),
          FINALLY_CLAUSE())
      ));
  }

  public TryStatementTreeImpl FINALLY_CLAUSE() {
    return b.<TryStatementTreeImpl>nonterminal(EcmaScriptGrammar.FINALLY)
      .is(f.newTryStatementWithFinally(b.invokeRule(EcmaScriptKeyword.FINALLY), BLOCK()));
  }

  public CatchBlockTreeImpl CATCH_CLAUSE() {
    return b.<CatchBlockTreeImpl>nonterminal(Kind.CATCH_BLOCK)
      .is(f.newCatchBlock(
        b.invokeRule(EcmaScriptKeyword.CATCH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CATCH_PARAMETER),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        BLOCK()));
  }

  public SwitchStatementTreeImpl SWITCH_STATEMENT() {
    return b.<SwitchStatementTreeImpl>nonterminal(Kind.SWITCH_STATEMENT)
      .is(f.completeSwitchStatement(
        b.invokeRule(EcmaScriptKeyword.SWITCH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        CASE_BLOCK()));
  }

  public SwitchStatementTreeImpl CASE_BLOCK() {
    return b.<SwitchStatementTreeImpl>nonterminal()
      .is(f.newSwitchStatement(
        b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
        b.zeroOrMore(CASE_CLAUSE()),
        b.optional(f.newTuple2(DEFAULT_CLAUSE(), b.zeroOrMore(CASE_CLAUSE()))),
        b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public CaseClauseTreeImpl CASE_CLAUSE() {
    return b.<CaseClauseTreeImpl>nonterminal(Kind.CASE_CLAUSE)
      .is(f.caseClause(
        b.invokeRule(EcmaScriptKeyword.CASE),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.COLON),
        b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST))));
  }

  public DefaultClauseTreeImpl DEFAULT_CLAUSE() {
    return b.<DefaultClauseTreeImpl>nonterminal(Kind.DEFAULT_CLAUSE)
      .is(f.defaultClause(
        b.invokeRule(EcmaScriptKeyword.DEFAULT),
        b.invokeRule(EcmaScriptPunctuator.COLON),
        b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST))));
  }

  public IfStatementTreeImpl IF_STATEMENT() {
    return b.<IfStatementTreeImpl>nonterminal(Kind.IF_STATEMENT)
      .is(f.ifStatement(
        b.invokeRule(EcmaScriptKeyword.IF),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT(),
        b.optional(ELSE_CLAUSE())));
  }

  public ElseClauseTreeImpl ELSE_CLAUSE() {
    return b.<ElseClauseTreeImpl>nonterminal(Kind.ELSE_CLAUSE)
      .is(f.elseClause(
        b.invokeRule(EcmaScriptKeyword.ELSE),
        STATEMENT()));
  }

  public WhileStatementTreeImpl WHILE_STATEMENT() {
    return b.<WhileStatementTreeImpl>nonterminal(Kind.WHILE_STATEMENT)
      .is(f.whileStatement(
        b.invokeRule(EcmaScriptKeyword.WHILE),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public DoWhileStatementTreeImpl DO_WHILE_STATEMENT() {
    return b.<DoWhileStatementTreeImpl>nonterminal(Kind.DO_WHILE_STATEMENT)
      .is(f.doWhileStatement(
        b.invokeRule(EcmaScriptKeyword.DO),
        STATEMENT(),
        b.invokeRule(EcmaScriptKeyword.WHILE),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ExpressionStatementTreeImpl EXPRESSION_STATEMENT() {
    return b.<ExpressionStatementTreeImpl>nonterminal(Kind.EXPRESSION_STATEMENT)
      .is(f.expressionStatement(
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LCURLY_AND_FUNCTION), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  /**
   * ECMAScript 6
   */
  public ForOfStatementTreeImpl FOR_OF_STATEMENT() {
    return b.<ForOfStatementTreeImpl>nonterminal(Kind.FOR_OF_STATEMENT)
      .is(f.forOfStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.firstOf(b.invokeRule(EcmaScriptGrammar.FOR_DECLARATION), b.invokeRule(EcmaScriptGrammar.LEFT_HAND_SIDE_EXPRESSION_NO_LET)),
        b.invokeRule(EcmaScriptGrammar.OF),
        b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public ForInStatementTreeImpl FOR_IN_STATEMENT() {
    return b.<ForInStatementTreeImpl>nonterminal(Kind.FOR_IN_STATEMENT)
      .is(f.forInStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.firstOf(b.invokeRule(EcmaScriptGrammar.FOR_DECLARATION), b.invokeRule(EcmaScriptGrammar.LEFT_HAND_SIDE_EXPRESSION_NO_LET_AND_LBRACKET)),
        b.invokeRule(EcmaScriptKeyword.IN),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public ForStatementTreeImpl FOR_STATEMENT() {
    return b.<ForStatementTreeImpl>nonterminal(Kind.FOR_STATEMENT)
      .is(f.forStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),

        b.optional(b.firstOf(
          b.invokeRule(EcmaScriptGrammar.FOR_VAR_DECLARATION),
          b.invokeRule(ES6(EcmaScriptGrammar.LEXICAL_DECLARATION_NO_IN)),
          b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_IN_NO_LET_AND_BRACKET))), b.invokeRule(EcmaScriptPunctuator.SEMI),
        b.optional(b.invokeRule(EcmaScriptGrammar.CONDITION)), b.invokeRule(EcmaScriptPunctuator.SEMI),
        b.optional(b.invokeRule(EcmaScriptGrammar.EXPRESSION)),

        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()
      ));
  }

  public StatementTree ITERATION_STATEMENT() {
    return b.<StatementTree>nonterminal(EcmaScriptGrammar.ITERATION_STATEMENT)
      .is(b.firstOf(
        DO_WHILE_STATEMENT(),
        WHILE_STATEMENT(),
        FOR_IN_STATEMENT(),
        ES6(FOR_OF_STATEMENT()),
        FOR_STATEMENT()));
  }

  public StatementTree STATEMENT() {
    return b.<StatementTree>nonterminal(EcmaScriptGrammar.STATEMENT)
      .is(b.firstOf(
        BLOCK(),
        VARIABLE_STATEMENT(),
        EMPTY_STATEMENT(),
        LABELLED_STATEMENT(),
        EXPRESSION_STATEMENT(),
        IF_STATEMENT(),
        ITERATION_STATEMENT(),
        CONTINUE_STATEMENT(),
        BREAK_STATEMENT(),
        RETURN_STATEMENT(),
        WITH_STATEMENT(),
        SWITCH_STATEMENT(),
        THROW_STATEMENT(),
        TRY_STATEMENT(),
        DEBUGGER_STATEMENT()));
  }

  /**
   * A.4 [END] Statement
   */

  /**
   * A.3 Expressions
   */

  public LiteralTreeImpl LITERAL() {
    return b.<LiteralTreeImpl>nonterminal(EcmaScriptGrammar.LITERAL)
      .is(b.firstOf(
        f.nullLiteral(b.invokeRule(EcmaScriptKeyword.NULL)),
        f.booleanLiteral(b.firstOf(b.invokeRule(EcmaScriptKeyword.TRUE), b.invokeRule(EcmaScriptKeyword.FALSE))),
        NUMERIC_LITERAL(),
        STRING_LITERAL(),
        f.regexpLiteral(b.invokeRule(EcmaScriptTokenType.REGULAR_EXPRESSION_LITERAL))));
  }

  public LiteralTreeImpl NUMERIC_LITERAL() {
    return b.<LiteralTreeImpl>nonterminal(Kind.NUMERIC_LITERAL)
      .is(f.numericLiteral(b.invokeRule(EcmaScriptTokenType.NUMERIC_LITERAL)));
  }

  public LiteralTreeImpl STRING_LITERAL() {
    return b.<LiteralTreeImpl>nonterminal(Kind.STRING_LITERAL)
      .is(f.stringLiteral(b.invokeRule(EcmaScriptGrammar.STRING_LITERAL)));
  }

  public AstNode ARRAY_LITERAL_ELEMENT() {
    return b.<AstNode>nonterminal(EcmaScriptGrammar.ARRAY_LITERAL_ELEMENT)
      .is(f.arrayInitialiserElement(b.optional(b.invokeRule(EcmaScriptPunctuator.ELLIPSIS)), b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)));
  }

  public ArrayLiteralTreeImpl ARRAY_ELEMENT_LIST() {
    return b.<ArrayLiteralTreeImpl>nonterminal(EcmaScriptGrammar.ELEMENT_LIST)
      .is(f.newArrayLiteralWithElements(
        b.zeroOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA)),
        ARRAY_LITERAL_ELEMENT(),
        b.zeroOrMore(
          f.newTuple3(f.newWrapperAstNode(b.oneOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))), ARRAY_LITERAL_ELEMENT())),
        b.zeroOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))));
  }

  public ParameterListTreeImpl FORMAL_PARAMETER_LIST() {
    return b.<ParameterListTreeImpl>nonterminal(Kind.FORMAL_PARAMETER_LIST)
      .is(f.completeFormalParameterList(
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.optional(b.firstOf(
          f.newFormalParameterList(
            b.invokeRule(EcmaScriptGrammar.BINDING_ELEMENT),
            b.zeroOrMore(f.newTuple4(b.invokeRule(EcmaScriptPunctuator.COMMA), b.invokeRule(EcmaScriptGrammar.BINDING_ELEMENT))),
            b.optional(ES6(f.newTuple5(b.invokeRule(EcmaScriptPunctuator.COMMA), BINDING_REST_ELEMENT())))),
          ES6(f.newFormalRestParameterList(BINDING_REST_ELEMENT()))
        )),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS)
      ));
  }

  /**
   * ECMAScript 6
   */
  public RestElementTreeImpl BINDING_REST_ELEMENT() {
    return b.<RestElementTreeImpl>nonterminal(EcmaScriptGrammar.BINDING_REST_ELEMENT)
      .is(f.bindingRestElement(b.invokeRule(EcmaScriptPunctuator.ELLIPSIS), BINDING_IDENTIFIER()));
  }

  public ArrayLiteralTreeImpl ARRAY_LITERAL() {
    return b.<ArrayLiteralTreeImpl>nonterminal(Kind.ARRAY_LITERAL)
      .is(f.completeArrayLiteral(
        b.invokeRule(EcmaScriptPunctuator.LBRACKET),
        b.optional(b.firstOf(
          ARRAY_ELEMENT_LIST(),
          f.newArrayLiteralWithElidedElements(b.oneOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))))),
        b.invokeRule(EcmaScriptPunctuator.RBRACKET)
      ));
  }

  /**
   * ECMAScript 6
   */
  public FunctionExpressionTreeImpl GENERATOR_EXPRESSION() {
    return b.<FunctionExpressionTreeImpl>nonterminal(Kind.GENERATOR_FUNCTION_EXPRESSION)
      .is(f.generatorExpression(
        b.invokeRule(EcmaScriptKeyword.FUNCTION),
        b.invokeRule(EcmaScriptPunctuator.STAR),
        b.optional(BINDING_IDENTIFIER()),
        FORMAL_PARAMETER_LIST(),
        b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
        b.invokeRule(EcmaScriptGrammar.FUNCTION_BODY),
        b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public FunctionExpressionTreeImpl FUNCTION_EXPRESSION() {
    return b.<FunctionExpressionTreeImpl>nonterminal(Kind.FUNCTION_EXPRESSION)
      .is(f.functionExpression(
        b.invokeRule(EcmaScriptKeyword.FUNCTION),
        b.optional(b.invokeRule(EcmaScriptTokenType.IDENTIFIER)),
        FORMAL_PARAMETER_LIST(),
        b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
        b.invokeRule(EcmaScriptGrammar.FUNCTION_BODY),
        b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public ExpressionTree CONDITIONAL_EXPRESSION() {
   return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_EXPRESSION)
     .is(f.completeConditionalExpression(
       CONDITIONAL_OR_EXPRESSION(),
       b.optional(f.newConditionalExpression(
         b.invokeRule(EcmaScriptPunctuator.QUERY),
         b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION),
         b.invokeRule(EcmaScriptPunctuator.COLON),
         b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)))
     ));
  }

  public ExpressionTree CONDITIONAL_OR_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_OR)
      .is(f.newConditionalOr(
        CONDITIONAL_AND_EXPRESSION(),
        b.zeroOrMore(f.newTuple6(
          b.invokeRule(EcmaScriptPunctuator.OROR),
          CONDITIONAL_AND_EXPRESSION()
        ))
      ));
  }

  public ExpressionTree CONDITIONAL_AND_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_AND)
      .is(f.newConditionalAnd(
        BITWISE_OR_EXPRESSION(),
        b.zeroOrMore(f.newTuple7(
          b.invokeRule(EcmaScriptPunctuator.ANDAND),
          BITWISE_OR_EXPRESSION()
        ))
      ));
  }

  public ExpressionTree BITWISE_OR_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_OR)
      .is(f.newBitwiseOr(
        BITWISE_XOR_EXPRESSION(),
        b.zeroOrMore(f.newTuple8(
          b.invokeRule(EcmaScriptPunctuator.OR),
          BITWISE_XOR_EXPRESSION()
        ))
      ));
  }

  public ExpressionTree BITWISE_XOR_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_XOR)
      .is(f.newBitwiseXor(
        BITWISE_AND_EXPRESSION(),
        b.zeroOrMore(f.newTuple9(
          b.invokeRule(EcmaScriptPunctuator.XOR),
          BITWISE_AND_EXPRESSION()
        ))
      ));
  }

  public ExpressionTree BITWISE_AND_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_AND)
      .is(f.newBitwiseAnd(
        EQUALITY_EXPRESSION(),
        b.zeroOrMore(f.newTuple10(
          b.invokeRule(EcmaScriptPunctuator.AND),
          EQUALITY_EXPRESSION()
        ))
      ));
  }

  public ExpressionTree EQUALITY_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.EQUALITY_EXPRESSION)
      .is(f.newEquality(
          RELATIONAL_EXPRESSION(),
          b.zeroOrMore(f.newTuple11(
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.EQUAL),
              b.invokeRule(EcmaScriptPunctuator.NOTEQUAL),
              b.invokeRule(EcmaScriptPunctuator.EQUAL2),
              b.invokeRule(EcmaScriptPunctuator.NOTEQUAL2)),
            RELATIONAL_EXPRESSION()
          ))
        )
      );
  }

  public ExpressionTree RELATIONAL_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.RELATIONAL_EXPRESSION)
      .is(f.newRelational(
          SHIFT_EXPRESSION(),
          b.zeroOrMore(f.newTuple12(
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.LT),
              b.invokeRule(EcmaScriptPunctuator.GT),
              b.invokeRule(EcmaScriptPunctuator.LE),
              b.invokeRule(EcmaScriptPunctuator.GE),
              b.invokeRule(EcmaScriptKeyword.INSTANCEOF),
              b.invokeRule(EcmaScriptKeyword.IN)),
            SHIFT_EXPRESSION()
          ))
        )
      );
  }

  public ExpressionTree SHIFT_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.SHIFT_EXPRESSION)
      .is(f.newShift(
          ADDITIVE_EXPRESSION(),
          b.zeroOrMore(f.newTuple13(
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.SL),
              b.invokeRule(EcmaScriptPunctuator.SR),
              b.invokeRule(EcmaScriptPunctuator.SR2)),
            ADDITIVE_EXPRESSION()
          ))
        )
      );
  }

  public ExpressionTree ADDITIVE_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.ADDITIVE_EXPRESSION)
      .is(f.newAdditive(
          MULTIPLICATIVE_EXPRESSION(),
          b.zeroOrMore(f.newTuple14(
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.PLUS),
              b.invokeRule(EcmaScriptPunctuator.MINUS)),
            MULTIPLICATIVE_EXPRESSION()
          ))
        )
      );
  }

  public ExpressionTree MULTIPLICATIVE_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.MULTIPLICATIVE_EXPRESSION)
      .is(f.newMultiplicative(
          UNARY_EXPRESSION(),
          b.zeroOrMore(f.newTuple15(
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.STAR),
              b.invokeRule(EcmaScriptPunctuator.DIV),
              b.invokeRule(EcmaScriptPunctuator.MOD)),
            UNARY_EXPRESSION()
          ))
        )
      );
  }

  public ExpressionTree UNARY_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.UNARY_EXPRESSION)
      .is(b.firstOf(
        POSTFIX_EXPRESSION(),
        f.prefixExpression(
          b.firstOf(
            b.invokeRule(EcmaScriptKeyword.DELETE),
            b.invokeRule(EcmaScriptKeyword.VOID),
            b.invokeRule(EcmaScriptKeyword.TYPEOF),
            b.invokeRule(EcmaScriptPunctuator.INC),
            b.invokeRule(EcmaScriptPunctuator.DEC),
            b.invokeRule(EcmaScriptPunctuator.PLUS),
            b.invokeRule(EcmaScriptPunctuator.MINUS),
            b.invokeRule(EcmaScriptPunctuator.TILDA),
            b.invokeRule(EcmaScriptPunctuator.BANG)),
          UNARY_EXPRESSION()
        )
      ));
  }

  public ExpressionTree POSTFIX_EXPRESSION() {
   return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.POSTFIX_EXPRESSION)
     .is(f.postfixExpression(
       LEFT_HAND_SIDE_EXPRESSION(),
       b.optional(b.firstOf(
         b.invokeRule(EcmaScriptGrammar.INC_NO_LB),
         b.invokeRule(EcmaScriptGrammar.DEC_NO_LB)
       ))
     ));
  }

  public ExpressionTree LEFT_HAND_SIDE_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.LEFT_HAND_SIDE_EXPRESSION)
      .is(b.firstOf(
        CALL_EXPRESSION(),
        NEW_EXPRESSION()
      ));
  }

  public YieldExpressionTreeImpl YIELD_EXPRESSION() {
    return b.<YieldExpressionTreeImpl>nonterminal(Kind.YIELD_EXPRESSION)
      .is(f.completeYieldExpression(
        b.invokeRule(EcmaScriptKeyword.YIELD),
        b.optional(f.newYieldExpression(
            b.optional(b.invokeRule(EcmaScriptGrammar.STAR_NO_LB)),
            b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION_NO_LB))
        )
      ));
  }

  public IdentifierTreeImpl IDENTIFIER_REFERENCE() {
    return b.<IdentifierTreeImpl>nonterminal(EcmaScriptGrammar.IDENTIFIER_REFERENCE)
      .is(f.identifierReference(b.firstOf(
          b.invokeRule(EcmaScriptKeyword.YIELD),
          b.invokeRule(EcmaScriptTokenType.IDENTIFIER)))
      );
  }

  public IdentifierTreeImpl BINDING_IDENTIFIER() {
    return b.<IdentifierTreeImpl>nonterminal(EcmaScriptGrammar.BINDING_IDENTIFIER)
      .is(f.bindingIdentifier(b.firstOf(
          b.invokeRule(EcmaScriptKeyword.YIELD),
          b.invokeRule(EcmaScriptTokenType.IDENTIFIER)))
      );
  }

  public IdentifierTreeImpl LABEL_IDENTIFIER() {
    return b.<IdentifierTreeImpl>nonterminal(Kind.LABEL_IDENTIFIER)
      .is(f.labelIdentifier(b.invokeRule(EcmaScriptTokenType.IDENTIFIER)));
  }

  public IdentifierTreeImpl IDENTIFIER_NAME() {
    return b.<IdentifierTreeImpl>nonterminal()
      .is(f.identifierName(b.invokeRule(EcmaScriptGrammar.IDENTIFIER_NAME)));
  }

  public ParameterListTreeImpl ARROW_PARAMETER_LIST() {
    return b.<ParameterListTreeImpl>nonterminal(Kind.ARROW_PARAMETER_LIST)
      .is(f.completeArrowParameterList(
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.optional(
          b.firstOf(
            f.newArrowRestParameterList(BINDING_REST_ELEMENT()),
            f.newArrowParameterList(
              // TODO martin: assignment_expression (comma, assignment_expression)* instead
              b.invokeRule(EcmaScriptGrammar.EXPRESSION),
              b.optional(f.newTuple16(b.invokeRule(EcmaScriptPunctuator.COMMA), BINDING_REST_ELEMENT())))
          )),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS)
      ));
  }

  public ArrowFunctionTreeImpl ARROW_FUNCTION() {
    return b.<ArrowFunctionTreeImpl>nonterminal(Kind.ARROW_FUNCTION)
      .is(f.arrowFunction(
        b.firstOf(
          BINDING_IDENTIFIER(),
          ARROW_PARAMETER_LIST()),
        b.invokeRule(EcmaScriptGrammar.DOUBLEARROW_NO_LB),
        b.firstOf(
          BLOCK(),
          b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION_NO_LCURLY))
      ));
  }

  public ExpressionTree MEMBER_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.MEMBER_EXPRESSION)
      .is(f.completeMemberExpression(
        b.firstOf(
          ES6(SUPER_PROPERTY()),
          f.newExpressionWithArgument(b.invokeRule(EcmaScriptKeyword.NEW), b.firstOf(ES6(SUPER()), MEMBER_EXPRESSION()), ARGUMENTS()),
          PRIMARY_EXPRESSION()),
        b.zeroOrMore(
          b.firstOf(
            BRACKET_EXPRESSION(),
            OBJECT_PROPERTY_ACCESS(),
            ES6(TAGGED_TEMPLATE())))
      ));
  }

  public MemberExpressionTree SUPER_PROPERTY() {
    return b.<MemberExpressionTree>nonterminal()
      .is(f.completeSuperMemberExpression(
        SUPER(),
        b.firstOf(
          OBJECT_PROPERTY_ACCESS(),
          BRACKET_EXPRESSION())
      ));
  }

  public SuperTreeImpl SUPER() {
    return b.<SuperTreeImpl>nonterminal(Kind.SUPER)
      .is(f.superExpression(b.invokeRule(EcmaScriptKeyword.SUPER)));
  }

  public MemberExpressionTree OBJECT_PROPERTY_ACCESS() {
    return b.<DotMemberExpressionTreeImpl>nonterminal(Kind.DOT_MEMBER_EXPRESSION)
      .is(f.newDotMemberExpression(
        b.invokeRule(EcmaScriptPunctuator.DOT),
        IDENTIFIER_NAME()));
  }

  public MemberExpressionTree BRACKET_EXPRESSION() {
    return b.<BracketMemberExpressionTreeImpl>nonterminal(Kind.BRACKET_MEMBER_EXPRESSION)
      .is(f.newBracketMemberExpression(
        b.invokeRule(EcmaScriptPunctuator.LBRACKET),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RBRACKET)));
  }

  public ExpressionTree TAGGED_TEMPLATE() {
    return b.<TaggedTemplateTreeImpl>nonterminal(Kind.TAGGED_TEMPLATE)
      .is(f.newTaggedTemplate(TEMPLATE_LITERAL()));

  }

  public ParameterListTreeImpl ARGUMENTS() {
    return b.<ParameterListTreeImpl>nonterminal(Kind.ARGUMENTS)
      .is(f.completeArguments(
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.optional(ARGUMENT_LIST()),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS)

      ));
  }

  public ParameterListTreeImpl ARGUMENT_LIST() {
    return b.<ParameterListTreeImpl>nonterminal(EcmaScriptGrammar.ARGUMENTS_LIST)
      .is(f.newArgumentList(
          ARGUMENT(),
          b.zeroOrMore(f.newTuple17(b.invokeRule(EcmaScriptPunctuator.COMMA), ARGUMENT())))
      );
  }

  // FIXME get rid of AstNode
  public AstNode ARGUMENT() {
    return b.<AstNode>nonterminal()
      .is(f.argument(
        b.optional(b.invokeRule(EcmaScriptPunctuator.ELLIPSIS)),
        b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)));
  }

  public ExpressionTree CALL_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.CALL_EXPRESSION)
      .is(f.callExpression(
        f.simpleCallExpression(b.firstOf(MEMBER_EXPRESSION(), SUPER()), ARGUMENTS()),
        b.zeroOrMore(b.firstOf(
          ARGUMENTS(),
          BRACKET_EXPRESSION(),
          OBJECT_PROPERTY_ACCESS(),
          ES6(TAGGED_TEMPLATE())
        ))));
  }

  public ParenthesisedExpressionTreeImpl PARENTHESISED_EXPRESSION() {
    return b.<ParenthesisedExpressionTreeImpl>nonterminal(Kind.PARENTHESISED_EXPRESSION)
      .is(f.parenthesisedExpression(
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS)));
  }

  public ClassExpressionTreeImpl CLASS_EXPRESSION() {
    return b.<ClassExpressionTreeImpl>nonterminal(Kind.CLASS_EXPRESSION)
      .is(f.classExpression(
        b.invokeRule(EcmaScriptKeyword.CLASS),
        b.optional(BINDING_IDENTIFIER()),
        b.invokeRule(EcmaScriptGrammar.CLASS_TAIL)
      ));
  }

  public ComputedPropertyNameTreeImpl COMPUTED_PROPERTY_NAME() {
    return b.<ComputedPropertyNameTreeImpl>nonterminal(Kind.COMPUTED_PROPERTY_NAME)
      .is(f.computedPropertyName(
        b.invokeRule(EcmaScriptPunctuator.LBRACKET),
        b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RBRACKET)
      ));
  }

  public ExpressionTree LITERAL_PROPERTY_NAME() {
    return b.<ExpressionTree>nonterminal()
      .is(b.firstOf(
        IDENTIFIER_NAME(),
        STRING_LITERAL(),
        NUMERIC_LITERAL()
      ));
  }

  public ExpressionTree PROPERTY_NAME() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.PROPERTY_NAME)
      .is(b.firstOf(
        LITERAL_PROPERTY_NAME(),
        ES6(COMPUTED_PROPERTY_NAME())
      ));
  }

  public PairPropertyTreeImpl PAIR_PROPERTY() {
    return b.<PairPropertyTreeImpl>nonterminal(Kind.PAIR_PROPERTY)
      .is(f.pairProperty(
        PROPERTY_NAME(),
        b.invokeRule(EcmaScriptPunctuator.COLON),
        b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)
      ));
  }

  // FIXME: get rid of use of AstNode
  public AstNode PROPERTY_DEFINITION() {
    return b.<AstNode>nonterminal(EcmaScriptGrammar.PROPERTY_DEFINITION)
      .is(b.firstOf(
        PAIR_PROPERTY(),
        b.invokeRule(EcmaScriptGrammar.METHOD_DEFINITION),
        ES6(b.invokeRule(EcmaScriptGrammar.COVER_INITIALIZED_NAME))
      ));
  }

  public ObjectLiteralTreeImpl OBJECT_LITERAL() {
    return b.<ObjectLiteralTreeImpl>nonterminal(Kind.OBJECT_LITERAL)
      .is(f.completeObjectLiteral(
        b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
        b.optional(f.newObjectLiteral(
          PROPERTY_DEFINITION(),
          b.zeroOrMore(f.newTuple18(b.invokeRule(EcmaScriptPunctuator.COMMA), PROPERTY_DEFINITION())),
          b.optional(b.invokeRule(EcmaScriptPunctuator.COMMA))
        )),
        b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)
      ));
  }

  public ExpressionTree NEW_EXPRESSION() {
    return b.<ExpressionTree>nonterminal()
      .is(b.firstOf(
        MEMBER_EXPRESSION(),
        f.newExpression(b.invokeRule(EcmaScriptKeyword.NEW), b.firstOf(ES6(SUPER()), NEW_EXPRESSION()))
      ));
  }

  public TemplateLiteralTreeImpl TEMPLATE_LITERAL() {
    return b.<TemplateLiteralTreeImpl>nonterminal(Kind.TEMPLATE_LITERAL)
      .is(b.firstOf(
        f.noSubstitutionTemplate(b.invokeRule(EcmaScriptGrammar.BACKTICK), b.optional(TEMPLATE_CHARACTERS()), b.invokeRule(EcmaScriptGrammar.BACKTICK)),
        f.substitutionTemplate(
          //HEAD
          b.invokeRule(EcmaScriptGrammar.BACKTICK),
          b.optional(TEMPLATE_CHARACTERS()),
          TEMPLATE_EXPRESSION_HEAD(),
          // MIDDLE
          b.zeroOrMore(f.newWrapperAstNode2(
            b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE),
            b.optional(TEMPLATE_CHARACTERS()),
            TEMPLATE_EXPRESSION_HEAD()
          )),
          //TAIL
          b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE),
          b.optional(TEMPLATE_CHARACTERS()),
          b.invokeRule(EcmaScriptGrammar.BACKTICK)
        )
      ));
  }

  public TemplateExpressionTreeImpl TEMPLATE_EXPRESSION_HEAD() {
    return b.<TemplateExpressionTreeImpl>nonterminal()
      .is(f.newTemplateExpressionHead(
          b.invokeRule(EcmaScriptGrammar.DOLLAR_SIGN),
          b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
          b.invokeRule(EcmaScriptGrammar.EXPRESSION)
        ));
  }

  public TemplateCharactersTreeImpl TEMPLATE_CHARACTERS() {
    return b.<TemplateCharactersTreeImpl>nonterminal()
      .is(f.templateCharacters(b.oneOrMore(b.invokeRule(EcmaScriptGrammar.TEMPLATE_CHARACTER))));
  }

  public ThisTreeImpl THIS() {
    return b.<ThisTreeImpl>nonterminal(Kind.THIS)
      .is(f.thisExpression(b.invokeRule(EcmaScriptKeyword.THIS)));

  }

  public ExpressionTree PRIMARY_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.PRIMARY_EXPRESSION)
      .is(
        b.firstOf(
          THIS(),
          // Not IDENTIFIER_REFERENCE, to avoid conflicts with YIELD_EXPRESSION from ASSIGNMENT_EXPRESSION
          f.identifierReferenceWithoutYield(b.invokeRule(EcmaScriptTokenType.IDENTIFIER)),
          LITERAL(),
          ARRAY_LITERAL(),
          OBJECT_LITERAL(),
          FUNCTION_EXPRESSION(),
          PARENTHESISED_EXPRESSION(),
          CLASS_EXPRESSION(),
          GENERATOR_EXPRESSION(),
          TEMPLATE_LITERAL()
      ));
  }

  public ExpressionTree ES6_ASSIGNMENT_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.ES6_ASSIGNMENT_EXPRESSION)
      .is(b.firstOf(YIELD_EXPRESSION(), ARROW_FUNCTION()));
  }

  public ExpressionTree ASSIGNMENT_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)
      .is(
        b.firstOf(
          f.assignmentExpression(
            LEFT_HAND_SIDE_EXPRESSION(),
            b.firstOf(
              b.invokeRule(EcmaScriptPunctuator.EQU),
              b.invokeRule(EcmaScriptPunctuator.STAR_EQU),
              b.invokeRule(EcmaScriptPunctuator.DIV_EQU),
              b.invokeRule(EcmaScriptPunctuator.MOD_EQU),
              b.invokeRule(EcmaScriptPunctuator.PLUS_EQU),
              b.invokeRule(EcmaScriptPunctuator.MINUS_EQU),
              b.invokeRule(EcmaScriptPunctuator.SL_EQU),
              b.invokeRule(EcmaScriptPunctuator.SR_EQU),
              b.invokeRule(EcmaScriptPunctuator.SR_EQU2),
              b.invokeRule(EcmaScriptPunctuator.AND_EQU),
              b.invokeRule(EcmaScriptPunctuator.XOR_EQU),
              b.invokeRule(EcmaScriptPunctuator.OR_EQU)),
            ASSIGNMENT_EXPRESSION()),
          (ExpressionTree) b.invokeRule(EcmaScriptGrammar.CONDITIONAL_EXPRESSION_LOOKAHEAD),
          ES6_ASSIGNMENT_EXPRESSION()
      ));
  }

  /**
   * A.3 [END] Expressions
   */

  private <T> T ES6(T object) {
    return object;
  }

}
