package org.jggug.magica.akatsuki;

import java.util.List;
import java.util.Stack;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import ru.trylogic.groovy.macro.runtime.Macro;
import ru.trylogic.groovy.macro.runtime.MacroContext;

public class MatchMacro {
	
    /**
     * @param macroContext
     * @param variableExpression
     * @param closureExpression
     */
    @Macro
    public static Expression match(MacroContext macroContext, Expression variableExpression, ClosureExpression closureExpression) {
        BlockStatement matchClosureBlockStatement = new BlockStatement();
        Parameter itParameter = param(ClassHelper.OBJECT_TYPE, "it");
        ClosureExpression matchClosureExpression = closureX(new Parameter[] { itParameter }, matchClosureBlockStatement);
        matchClosureExpression.setVariableScope(closureExpression.getVariableScope().copy());

        BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        List<Statement> statements = closureBlockStatement.getStatements();
        for (Statement statement : statements) {
            ExpressionStatement expressionStatement = (ExpressionStatement) statement;
            MethodCallExpression methodCallExpression = (MethodCallExpression) expressionStatement.getExpression();
            ConstantExpression method = (ConstantExpression) methodCallExpression.getMethod();
            String methodName = method.getText();
            Statement transformedStatement = null;
            switch (methodName) {
                case "then" :
                    transformedStatement = makeWhenThen(macroContext, methodCallExpression, itParameter);
                    break;
                case "orElse" :
                    transformedStatement = makeOrElse(macroContext, methodCallExpression, itParameter);
                    break;
            }
            matchClosureBlockStatement.addStatement(transformedStatement);
        }

        return callX(matchClosureExpression, "call", variableExpression);
    }

    private static Statement makeWhenThen(MacroContext macroContext, MethodCallExpression thenMethodCallExpression, Parameter itParameter) {
        VariableExpression itVariableExpression = varX(itParameter);

        // then
        ArgumentListExpression thenArgumets = (ArgumentListExpression) thenMethodCallExpression.getArguments();
        Expression thenArgumet = thenArgumets.getExpression(0);
        BlockStatement ifBlockStatement = block(new ReturnStatement(thenArgumet));

        // when
        MethodCallExpression whenMethodCallExpression = (MethodCallExpression) thenMethodCallExpression.getObjectExpression();
        ArgumentListExpression whenArguments = (ArgumentListExpression) whenMethodCallExpression.getArguments();
        Expression whenArgument = whenArguments.getExpression(0);
        Expression ifConditionExpression = null;
        if (whenArgument instanceof ClassExpression) {
            // when <Class> then ...
            // -> it instanceof <Class>
            ifConditionExpression = isInstanceOfX(itVariableExpression, whenArgument.getType());
        } else if (whenArgument instanceof BinaryExpression) {
            // when ( a | b | c ) then ...
            // -> it == a || it == b || it == c
            Stack<ConstantExpression> stack = new Stack<ConstantExpression>();
            BinaryExpression binaryExpression = (BinaryExpression) whenArgument;
            binaryExpression = pushConstantExpression(binaryExpression, stack);
            while (binaryExpression != null) {
                binaryExpression = pushConstantExpression(binaryExpression, stack);
            }

            ifConditionExpression = makeBinaryCondition(ifConditionExpression, itVariableExpression, stack);
            while (!stack.empty()) {
                ifConditionExpression = makeBinaryCondition(ifConditionExpression, itVariableExpression, stack);
            }
        } else if (whenArgument instanceof RangeExpression) {
            // when <Range> then ...
            // -> <Range>.contains(it)
            ifConditionExpression = callX(whenArgument, "contains", itVariableExpression);
        } else if (whenArgument instanceof ConstantExpression) {
            // when <Constant> then ...
            ifConditionExpression = makeCondition(itVariableExpression, whenArgument);
        }

        return ifS(ifConditionExpression, ifBlockStatement);
    }

    private static BinaryExpression pushConstantExpression(BinaryExpression binaryExpression, Stack<ConstantExpression> stack) {
        stack.push((ConstantExpression) binaryExpression.getRightExpression());
        if (binaryExpression.getLeftExpression() instanceof ConstantExpression) {
            stack.push((ConstantExpression) binaryExpression.getLeftExpression());
            return null;
        } else {
            return (BinaryExpression) binaryExpression.getLeftExpression();
        }
    }

    private static Expression makeBinaryCondition(Expression binaryExpression, VariableExpression itVariableExpression, Stack<ConstantExpression> stack) {
        if (binaryExpression == null) {
            ConstantExpression constantExpression = stack.pop();
            return makeCondition(itVariableExpression, constantExpression);
        } else {
            ConstantExpression constantExpression = stack.pop();
            return orX(binaryExpression, makeCondition(itVariableExpression, constantExpression));
        }
    }

    private static Expression makeCondition(VariableExpression itVariableExpression, Expression argument) {
        Expression conditionExpression = null;
        if (ClassHelper.isPrimitiveType(argument.getType())) {
            // -> it == <Constant>
            conditionExpression = eqX(itVariableExpression, argument);
        } else {
            // -> it.equals(<Constant>)
            conditionExpression = callX(itVariableExpression, "equals", argument);
        }
        return conditionExpression;
    }

    private static Statement makeOrElse(MacroContext macroContext, MethodCallExpression orElseMethodCallExpression, Parameter itParameter) {
        ArgumentListExpression arguments = (ArgumentListExpression) orElseMethodCallExpression.getArguments();
        Expression argument = arguments.getExpression(0);
        return new ReturnStatement(argument);
    }
}