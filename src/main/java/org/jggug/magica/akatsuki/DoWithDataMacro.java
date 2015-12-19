package org.jggug.magica.akatsuki;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import ru.trylogic.groovy.macro.runtime.Macro;
import ru.trylogic.groovy.macro.runtime.MacroContext;

/**
 */
public class DoWithDataMacro {

    /**
     * @param macroContext
     * @param closureExpression
     */
    @Macro
    public static Expression doWithData(MacroContext macroContext, ClosureExpression closureExpression) {
        BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        List<Statement> noLabelBlock = new ArrayList<Statement>();
        List<Statement> doWithBlock = new ArrayList<Statement>();
        List<Statement> whereBlock = new ArrayList<Statement>();
        List<Statement> currentBlock = noLabelBlock;
        for (Statement statement : closureBlockStatement.getStatements()) {
            String label = statement.getStatementLabel();
            if (label == null) {
                label = "";
            }
            switch (label) {
                case "dowith" :
                    currentBlock = doWithBlock;
                    currentBlock.add(statement);
                    break;
                case "where" :
                    currentBlock = whereBlock;
                    currentBlock.add(statement);
                    break;
                default :
                    currentBlock.add(statement);
                    break;
            }
        }

        // where ブロックを、変数ごとに分解
        List<Expression> variables = parseBinaryExpression((ExpressionStatement) whereBlock.get(0));
        List<List<Expression>> valuesList = new ArrayList<List<Expression>>(whereBlock.size() - 1);
        for (int i = 1; i < whereBlock.size(); i++) {
            valuesList.add(parseBinaryExpression((ExpressionStatement) whereBlock.get(i)));
        }

        BlockStatement resultClosureBlockStatement = new BlockStatement();
        for (List<Expression> values : valuesList) {
            BlockStatement blockStatement = new BlockStatement();
            // no label block
            for (Statement noLabelStatement : noLabelBlock) {
                blockStatement.addStatement(noLabelStatement);
            }
            // where block -> 変数宣言
            for (int i = 0; i < variables.size(); i++) {
                VariableExpression variableExpression = (VariableExpression) variables.get(i);
                Expression initExpression = values.get(i);
                ExpressionStatement expressionStatement = (ExpressionStatement) declS(variableExpression, initExpression);
                blockStatement.addStatement(expressionStatement);
            }
            // dowtih block
            for (Statement doWithStatement : doWithBlock) {
                blockStatement.addStatement(doWithStatement);
            }
            resultClosureBlockStatement.addStatement(blockStatement);
        }

        ClosureExpression resultClosureExpression = closureX(resultClosureBlockStatement);
        resultClosureExpression.setVariableScope(closureExpression.getVariableScope().copy());
        return callX(resultClosureExpression, "call");
    }

    private static List<Expression> parseBinaryExpression(ExpressionStatement expressionStatement) {
        List<Expression> list = new ArrayList<Expression>();
        Expression expression = expressionStatement.getExpression();
        do {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            list.add(0, binaryExpression.getRightExpression());
            expression = binaryExpression.getLeftExpression();
            if (!(expression instanceof BinaryExpression)) {
                list.add(0, expression);
            }
        } while (expression instanceof BinaryExpression);
        return list;
    }
}
