package com.github.bpazy.eql;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.IconLoader;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public class Configs {
    public final static String eqlFileExtension = ".eql";
    public final static String eqlerConfigAntName = EqlerConfig.class.getName();
    public final static Set<String> eqlSqlAntNames;
    public final static List<String> eqlSqlAntIdentifiers = Lists.newArrayList("Sql", "UseSqlFile");
    public final static Icon eqlIcon = IconLoader.getIcon("eql.png");

    public static boolean isEqlSql(String name) {
        return eqlSqlAntIdentifiers.contains(name);
    }

    static {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages("org.n3r.eql.eqler.annotations")
                        .setScanners(new SubTypesScanner(false))
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("org.n3r.eql.eqler.annotations"))));
        eqlSqlAntNames = reflections.getAllTypes();
    }
}
