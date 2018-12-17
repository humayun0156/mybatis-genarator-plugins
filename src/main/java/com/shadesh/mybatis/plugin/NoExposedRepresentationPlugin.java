package com.shadesh.mybatis.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * This plugin is used to change the getters and setters of the generated myBatis models,
 * to avoid exposing the stored object when a Date is setted or getted.
 * Instead, a clone of the object will be created each time to avoid misuse.
 * 
 * Setter example:
 * Before: this.date = date;
 * After:  this.date = date == null ? null : new Date(date.getTime());
 * 
 * Getter example:
 * Before: return date;
 * After:  return date == null ? null : new Date(date.getTime());
 * 
 */
public class NoExposedRepresentationPlugin extends PluginAdapter {

    public NoExposedRepresentationPlugin() {
        // no-arg constructor
    }

    @Override
    public boolean validate(List<String> arg0) {
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (FullyQualifiedJavaType.getDateInstance().equals(method.getParameters().get(0).getType())) {
            int i = 0;
            for (String line : method.getBodyLines()) {
                if (line.contains(" = ")) {
                    String newLine = line.replace(" = ", " = " + method.getParameters().get(0).getName()
                            + " == null ? null : new Date(").replace(";", ".getTime());");
                    method.getBodyLines().set(i, newLine);
                    break;
                }
                i++;
            }

        }
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (FullyQualifiedJavaType.getDateInstance().equals(method.getReturnType())) {
            int i = 0;
            for (String line : method.getBodyLines()) {
                if (line.contains("return ")) {
                    String fieldName = line.replace("return ", "").replace(";", "");
                    String newLine = "return " + fieldName
                            + " == null ? null : new Date(" + fieldName + ".getTime());";
                    method.getBodyLines().set(i, newLine);
                    break;
                }
                i++;
            }
        }
        return true;
    }
}