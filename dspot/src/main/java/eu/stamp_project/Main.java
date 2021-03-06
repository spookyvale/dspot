package eu.stamp_project;

import eu.stamp_project.diff.SelectorOnDiff;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.TestDataMutator;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.options.JSAPOptions;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.RandomHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		try {
			FileUtils.forceDelete(new File("target/dspot/"));
		} catch (Exception ignored) {

		}
		final InputConfiguration configuration = JSAPOptions.parse(args);
		if (configuration == null) {
			Main.runExample();
		} else {
			run(configuration);
		}
	}

	public static void run(InputConfiguration configuration) throws Exception {
		DSpot dspot = new DSpot(
				configuration,
				configuration.getNbIteration(),
				configuration.getAmplifiers(),
				configuration.getSelector()
		);
		RandomHelper.setSeedRandom(configuration.getSeed());
		createOutputDirectories(configuration);
		final long startTime = System.currentTimeMillis();
		final List<CtType> amplifiedTestClasses;
		if (configuration.getTestClasses().isEmpty() || "all".equals(configuration.getTestClasses().get(0))) {
			amplifiedTestClasses = dspot.amplifyAllTests();
		} else if ("diff".equals(configuration.getTestClasses().get(0))) {
			final Map<String, List<String>> testMethodsAccordingToADiff = SelectorOnDiff
					.findTestMethodsAccordingToADiff(configuration);
			amplifiedTestClasses = testMethodsAccordingToADiff.keySet().stream()
					.flatMap(ctType ->
							dspot.amplifyTestClassesTestMethods(Collections.singletonList(ctType), testMethodsAccordingToADiff.get(ctType)).stream()
					).filter(Objects::nonNull)
					.collect(Collectors.toList());
		} else {
			amplifiedTestClasses = dspot.amplifyTestClassesTestMethods(configuration.getTestClasses(), configuration.getTestCases());
		}
		LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
		final long elapsedTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Elapsed time {} ms", elapsedTime);
	}

	public static void createOutputDirectories(InputConfiguration inputConfiguration) {
		final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
		try {
			if (inputConfiguration.shouldClean() && outputDirectory.exists()) {
				FileUtils.forceDelete(outputDirectory);
			}
			if (!outputDirectory.exists()) {
				FileUtils.forceMkdir(outputDirectory);
			}
		} catch (IOException ignored) {
			// ignored
		}
	}

	static void runExample() {
		try {
			InputConfiguration configuration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
			configuration.setAmplifiers(Collections.singletonList(new TestDataMutator()));
			DSpot dSpot = new DSpot(configuration, 1, configuration.getAmplifiers(),
					new JacocoCoverageSelector());
			dSpot.amplifyTestClassesTestMethods(Collections.singletonList("example.TestSuiteExample"), Collections.emptyList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}