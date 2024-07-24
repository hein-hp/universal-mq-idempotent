package cn.hein.idempotent.toolkits;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * SpEL 表达式解析工具
 */
public class SpELUtil {

    public static Object parseKey(String spel, Method method, Object[] contextObj) {
        List<String> flags = List.of("#", "T(");
        Optional<String> optional = flags.stream().filter(spel::contains).findFirst();
        if (optional.isPresent()) {
            return parse(spel, method, contextObj);
        }
        return spel;
    }

    private static Object parse(String spel, Method method, Object[] contextObj) {
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(spel);
        String[] params = discoverer.getParameterNames(method);
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], contextObj[i]);
            }
        }
        return exp.getValue(context);
    }
}