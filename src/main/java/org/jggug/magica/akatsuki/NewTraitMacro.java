package org.jggug.magica.akatsuki;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import ru.trylogic.groovy.macro.runtime.Macro;
import ru.trylogic.groovy.macro.runtime.MacroContext;

/**
 */
public class NewTraitMacro {

    /**
     * @param macroContext
     * @param classExpression
     */
    @Macro
    public static Expression newTrait(MacroContext macroContext, ClassExpression classExpression) {
        return newTrait(macroContext, new MapExpression(), classExpression);
    }

    /**
     * @param macroContext
     * @param mapExpression
     * @param classExpression
     */
    @Macro
    public static Expression newTrait(MacroContext macroContext, MapExpression mapExpression, ClassExpression classExpression) {
        BlockStatement closureBlockStatement = new BlockStatement();
        ClosureExpression closureBlockExpression = closureX(closureBlockStatement);

        // Object obj = (new Object()).withTraits(<Class>);
        VariableExpression variableExpression = varX("obj", ClassHelper.OBJECT_TYPE);
        ConstructorCallExpression constructorCallExpression = ctorX(ClassHelper.OBJECT_TYPE);
        ArgumentListExpression argumentListExpression = args(classExpression);
        MethodCallExpression initExpression = callX(constructorCallExpression, "withTraits", argumentListExpression);
        ExpressionStatement expressionStatement = (ExpressionStatement) declS(variableExpression, initExpression);
        closureBlockStatement.addStatement(expressionStatement);

        // obj[<prop>] = <value>
        for (MapEntryExpression mapEntryExpression : mapExpression.getMapEntryExpressions()) {
            // obj[<prop>]
            BinaryExpression leftExpression = (BinaryExpression) indexX(variableExpression, mapEntryExpression.getKeyExpression());
            closureBlockStatement.addStatement(assignS(leftExpression, mapEntryExpression.getValueExpression()));
        }

        // return obj;
        ReturnStatement returnStatement = new ReturnStatement(variableExpression);
        closureBlockStatement.addStatement(returnStatement);

        VariableScope variableScope = new VariableScope();
        variableScope.setClassScope(classExpression.getType());
        closureBlockExpression.setVariableScope(variableScope);

        return callX(closureBlockExpression, "call");
    }
}
