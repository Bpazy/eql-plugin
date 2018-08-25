package com.github.bpazy.eql;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.IconLoader;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlId;
import org.n3r.eql.eqler.annotations.UseSqlFile;

import javax.swing.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public class Configs {
    public final static String eqlFileExtension = ".eql";
    public final static String eqlerConfigAntName = EqlerConfig.class.getName();
    public final static Set<String> eqlSqlAntNames;
    public final static Set<String> eqlSqlAntSimpleNames;
    public final static Icon eqlIcon = IconLoader.getIcon("eql.png");
    public final static String dqlName = Dql.class.getName();

    public static boolean isEqlSql(String name) {
        return eqlSqlAntSimpleNames.contains(name);
    }

    static {
        List<Class> clazzs = Lists.newArrayList(Sql.class, SqlId.class, UseSqlFile.class);
        eqlSqlAntNames = clazzs.stream().map(Class::getName).collect(Collectors.toSet());
        eqlSqlAntSimpleNames = clazzs.stream().map(Class::getSimpleName).collect(Collectors.toSet());
    }
}
