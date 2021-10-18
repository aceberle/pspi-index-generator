package com.eberlecreative.pspiindexgenerator.cli;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.eberlecreative.pspiindexgenerator.gui.PspiIndexGeneratorGUI;
import com.eberlecreative.pspiindexgenerator.imagemodifier.CropAnchors;
import com.eberlecreative.pspiindexgenerator.pspi.generator.PspiIndexGenerator;
import com.eberlecreative.pspiindexgenerator.pspi.util.PspiImageSize;

public class PspiIndexGeneratorCLI {

	public static void main(String[] args) throws Exception {
		final CommandLineParser parser = new DefaultParser();
		final Options options = makeOptions();
		try {
			final CommandLine cmd = parser.parse(options, args);
			final List<String> unknownArgs = cmd.getArgList();
			if (!unknownArgs.isEmpty()) {
				throw new ParseException("Unknown arguments: " + unknownArgs);
			}
			if (cmd.hasOption("help")) {
				printUsage(options);
			} else if (cmd.hasOption("i")) {
				generateIndex(cmd);
			} else {
				PspiIndexGeneratorGUI.main(args);
			}
		} catch (ParseException e) {
			System.err.println("Error: " + e.getMessage());
			System.err.println();
			printUsage(options);
		}
	}

	private static void generateIndex(final CommandLine cmd) throws Exception {
		final PspiIndexGenerator.Builder builder = new PspiIndexGenerator.Builder().verboseLogging(cmd.hasOption("v"))
				.strict(cmd.hasOption("s")).forceOutput(cmd.hasOption("f")).appendOutput(cmd.hasOption("a"));
		if (cmd.hasOption("r")) {
			builder.resizeImages(PspiImageSize.fromString(cmd.getOptionValue("r")));
			if (cmd.hasOption("c")) {
				builder.cropImages(CropAnchors.parseCropAnchor(cmd.getOptionValue("c")));
			}
		}
		if (cmd.hasOption("q")) {
			builder.compressionQuality(Float.parseFloat(cmd.getOptionValue("q")));
		}
		if (cmd.hasOption("d")) {
			builder.dataFile(new File(cmd.getOptionValue("d")));
		}
		if (cmd.hasOption("p")) {
			builder.outputFilePattern(cmd.getOptionValue("p"));
		}
		final PspiIndexGenerator generator = builder.build();
		final File inputDirectory = new File(cmd.getOptionValue("i"));
		final File outputDirectory = new File(cmd.getOptionValue("o", cmd.getOptionValue("i") + "_generated"));
		generator.generate(inputDirectory, outputDirectory);
	}

	private static void printUsage(Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		final String cmdLineSyntax = "java -jar pspi-index-generator.jar";
		final StringWriter buffer = new StringWriter();
		try (PrintWriter writer = new PrintWriter(buffer)) {
			writer.println("PSPI Index Generator");
			writer.println();
			writer.println("Example: ");
			writer.println(
					"    java -jar pspi-index-generator.jar -i inputDir -o outputDir -r SMALL -c center-middle -q 0.9");
			writer.println();
		}
		final String header = buffer.toString();
		final String footer = "Please report issues to aceberle@gmail.com";
		formatter.printHelp(125, cmdLineSyntax, header, options, footer, true);
	}

	private static Options makeOptions() {
		final Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("Prints this message").build());
		options.addOption(Option.builder("i").longOpt("input-dir").hasArg().argName("inputDir")
				.desc("Location of input directory").build());
		options.addOption(Option.builder("o").longOpt("output-dir").hasArg().argName("outputDir")
				.desc("Location of output directory").build());
		options.addOption(Option.builder("d").longOpt("data-file").hasArg().argName("dataFile")
				.desc("Location of input data file").build());
		options.addOption(Option.builder("p").longOpt("output-pattern").hasArg().argName("outputPattern")
				.desc("Change output file names").build());
		options.addOption(Option.builder("v").longOpt("verbose").desc("Enables verbose logging").build());
		options.addOption(
				Option.builder("f").longOpt("force").desc("Force overwrite of existing output directory").build());
		options.addOption(
				Option.builder("a").longOpt("append").desc("Append data to existing PSPI output directory").build());
		options.addOption(Option.builder("s").longOpt("strict").desc("Fails on unexpected files").build());
		options.addOption(Option.builder("r").longOpt("resize-images").hasArg().argName("SMALL|LARGE").desc(
				"Automatically resizes images to standard PSPI size specifications.  Acceptable values are \"SMALL\" for 320x400px or \"LARGE\" for 640x800px.")
				.build());
		options.addOption(
				Option.builder("c").longOpt("crop-anchor").hasArg().argName("(top|center|bottom)-(left|middle|right)")
						.desc("When -r is specified, this value is used to override the default crop anchor, which is: "
								+ PspiIndexGenerator.DEFAULT_CROP_ANCHOR)
						.build());
		options.addOption(Option.builder("q").longOpt("quality").hasArg().argName("0.0-1.0").desc(
				"Compression quality level to use when images are manipulated (crop or resize).  Values range is 0.0-1.0, where 0.0 is least quality and 1.0 is highest quality.  Default value is 0.7")
				.build());
		return options;
	}
}
