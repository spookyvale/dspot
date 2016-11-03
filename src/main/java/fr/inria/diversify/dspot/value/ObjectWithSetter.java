package fr.inria.diversify.dspot.value;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:29
 */
public class ObjectWithSetter extends Value {
    protected Map<String, Value> valuesToSet;
    protected String dynamicType;
    protected Factory factory;

    public ObjectWithSetter(String dynamicType, Factory factory, ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.valuesToSet = new HashMap<>();
        this.factory = factory;
        this.dynamicType = dynamicType;
    }

    public void addValue(String fieldOrMethod, PrimitiveValue value) {
        valuesToSet.put(fieldOrMethod, value);
    }

    @Override
    public String getDynamicType() {
        return dynamicType;
    }

    @Override
    public CtTypeReference getType() {
        return null;
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public CtExpression getValue() {
        return null;
    }

    @Override
    public void initLocalVar(CtLocalVariable localVar) throws Exception {
        CtBlock body = localVar.getParent(CtBlock.class);
        CtExpression constructorCall = valueFactory.findConstructorCall(factory.Class().get(dynamicType), true);
        if(constructorCall != null) {
            localVar.setAssignment(constructorCall);
            int count = 1;
            for (Map.Entry<String, Value> entry : valuesToSet.entrySet()) {
                String primitive = ((PrimitiveValue) entry.getValue()).simplePrintValue();
                CtCodeSnippetStatement stmt;
                if (entry.getKey().endsWith("(")) {
                    stmt = factory.Code().createCodeSnippetStatement(localVar.getSimpleName() + "." + entry.getKey() + primitive + ")");
                } else {
                    stmt = factory.Code().createCodeSnippetStatement(localVar.getSimpleName() + "." + entry.getKey() + " = " + primitive);
                }
                body.getStatements().add(count, stmt);
                count++;
            }
        } else {
            throw new Exception();
        }
    }
}