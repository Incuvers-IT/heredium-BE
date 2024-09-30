package art.heredium.core.config.jpa;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomMySQL8Dialect extends MySQL8Dialect {
    public CustomMySQL8Dialect() {
        registerFunction("GROUP_CONCAT", new StandardSQLFunction("GROUP_CONCAT", StandardBasicTypes.STRING));
        registerFunction("GROUP_CONCAT_SEPARATOR", new SQLFunctionTemplate(StandardBasicTypes.STRING, "GROUP_CONCAT(?1 SEPARATOR ?2)"));
    }
}
