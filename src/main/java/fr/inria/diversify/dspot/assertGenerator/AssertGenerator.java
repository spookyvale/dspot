package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/16
 * Time: 10:31
 */
public class AssertGenerator {

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    public AssertGenerator(InputConfiguration configuration, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.compiler = compiler;
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, new ArrayList<>(testClass.getMethods()));
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        MethodsAssertGenerator ags = new MethodsAssertGenerator(testClass, this.configuration, compiler);
        final List<CtMethod<?>> amplifiedTestWithAssertion = ags.generateAsserts(testClass, tests);
        Log.debug("{} new tests with assertions generated", amplifiedTestWithAssertion.size());
        return amplifiedTestWithAssertion;
    }
}
