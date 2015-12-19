package org.jggug.magica.akatsuki;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import ru.trylogic.groovy.macro.runtime.Macro;
import ru.trylogic.groovy.macro.runtime.MacroContext;

/**
 */
public class DoWhileMacro {

    /**
     * @param macroContext
     * @param closureExpression
     * @param booleanExpression
     */
    @Macro
    public static Expression dowhile(MacroContext macroContext, ClosureExpression closureExpression, Expression booleanExpression) {
        return dowhile(macroContext, booleanExpression, closureExpression);
    }

    /**
     * @param macroContext
     * @param booleanExpression
     * @param closureExpression
     */
    @Macro
    public static Expression dowhile(MacroContext macroContext, Expression booleanExpression, ClosureExpression closureExpression) {
        MethodCallExpression methodCallExpressionOnce = callX(closureExpression, "call");
        BlockStatement whileBlockStatement = new BlockStatement();
        MethodCallExpression methodCallExpressionTrue = callX(closureExpression, "call");
        whileBlockStatement.addStatement(new ExpressionStatement(methodCallExpressionTrue));
        WhileStatement whileStatement = new WhileStatement(new BooleanExpression(booleanExpression), whileBlockStatement);

        BlockStatement closureBlockStatement = new BlockStatement();
        closureBlockStatement.addStatement(new ExpressionStatement(methodCallExpressionOnce));
        closureBlockStatement.addStatement(whileStatement);
        ClosureExpression doWhileClosureExpression = closureX(closureBlockStatement);
        doWhileClosureExpression.setVariableScope(closureExpression.getVariableScope().copy());
        return callX(doWhileClosureExpression, "call");

/*
        BlockStatement doWhileBlockStatement = new BlockStatement();
        MethodCallExpression methodCallExpression = callX(closureExpression, "call");
        doWhileBlockStatement.addStatement(new ExpressionStatement(methodCallExpression));
        DoWhileStatement doWhileStatement = new DoWhileStatement(new BooleanExpression(booleanExpression), doWhileBlockStatement);

        BlockStatement closureBlockStatement = new BlockStatement();
        closureBlockStatement.addStatement(doWhileStatement);
        ClosureExpression doWhileClosureExpression = closureX(closureBlockStatement);
        doWhileClosureExpression.setVariableScope(closureExpression.getVariableScope().copy());
        return callX(doWhileClosureExpression, "call");
*/
    }
}
