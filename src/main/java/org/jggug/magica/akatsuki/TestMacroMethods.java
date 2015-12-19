package org.jggug.magica.akatsuki;

import org.codehaus.groovy.ast.expr.*;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import ru.trylogic.groovy.macro.runtime.*;

public class TestMacroMethods {
    @Macro
    public static Expression safe(MacroContext macroContext,
                                  MethodCallExpression callExpression) {
        return ternaryX(
                notNullX(callExpression.getObjectExpression()),
                callExpression,
                constX(null)
        );
    }
}
