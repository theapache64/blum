package com.theah64.blum;

import com.sun.istack.internal.NotNull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String FLAG_PROJECT_DIRECTORY = "pd";
    private static final String FLAG_COMMAND = "c";

    private static final Options OPTIONS = new Options()
            .addOption(FLAG_PROJECT_DIRECTORY, true, "Project directory")
            .addOption(FLAG_COMMAND, true, "Command");

    private static File COMMAND_DICTIONARY_FILE;

    public static void main(String[] args) throws IOException, InterruptedException {


        try {

            final String pgmDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
            COMMAND_DICTIONARY_FILE = new File(pgmDir + File.separator + "commands.txt");

            //Parsing command
            final CommandLine cmd = new DefaultParser().parse(OPTIONS, args);

            //Getting arguments
            final String projectFolder = cmd.getOptionValue(FLAG_PROJECT_DIRECTORY, null);
            if (projectFolder != null) {

                final String command = cmd.getOptionValue(FLAG_COMMAND, null);

                if (command != null) {

                    //Getting android project package name
                    final String projectPackageName = getProjectPackageName(projectFolder);
                    final String[] fullCommands = getFullCommand(command, projectPackageName);

                    for (final String fullCommand : fullCommands) {

                        System.out.println(fullCommand);

                        //executing command
                        final Process process = Runtime.getRuntime().exec(fullCommand);
                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(process.getInputStream()));

                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(process.getErrorStream()));

                        String s = null;
                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                        }

                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                        }


                        stdInput.close();
                        stdError.close();
                    }

                } else {
                    throw new CustomException("Command is null");
                }

            } else {
                throw new CustomException("Project folder is null");
            }

        } catch (ParseException | CustomException | IOException e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static String[] getFullCommand(String command, String projectPackageName) throws IOException, CustomException {
        final String getCommandFormat = getCommandFormat(command);
        return getCommandFormat.replaceAll("%s", projectPackageName).split("&&");
    }

    private static String getCommandFormat(String command) throws IOException, CustomException {
        final Pattern commandDicPattern = Pattern.compile(String.format(COMMAND_PARSE_REGEX_FORMAT, command));
        final BufferedReader br = new BufferedReader(new FileReader(COMMAND_DICTIONARY_FILE));
        String line = null;
        String fullCommandFormat = null;
        while ((line = br.readLine()) != null) {
            final Matcher matcher = commandDicPattern.matcher(line);
            if (matcher.find()) {
                fullCommandFormat = matcher.group(2);
                break;
            }
        }
        br.close();
        if (fullCommandFormat == null) {
            throw new CustomException("Invalid command : " + command);
        }
        return fullCommandFormat;
    }

    private static final Pattern PACKAGE_REGEX = Pattern.compile("^applicationId \"(.+)\"$");

    private static final String COMMAND_PARSE_REGEX_FORMAT = "alias %s = (\"|')(.+)(\"|')";

    //To get package name from gradle file
    @NotNull
    private static String getProjectPackageName(String projectFolder) throws CustomException, IOException {
        File gradleFile = new File(projectFolder + "/app/build.gradle");

        if (!gradleFile.exists()) {
            //React native support
            gradleFile = new File(projectFolder + "/android/app/build.gradle");
        }

        try {
            String packageName = null;

            //Reading gradle file to find package name
            final BufferedReader br = new BufferedReader(new FileReader(gradleFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("applicationId")) {
                    final Matcher matcher = PACKAGE_REGEX.matcher(line.trim());
                    if (matcher.find()) {
                        packageName = matcher.group(1);
                        break;
                    }
                }
            }
            br.close();

            if (packageName == null) {
                throw new CustomException("Unable to find package name from " + gradleFile.getAbsolutePath());
            }

            return packageName;
        } catch (FileNotFoundException e) {
            throw new CustomException(String.format("%s or %s/android is not an android project directory", projectFolder, projectFolder));
        }
    }

    private static class CustomException extends Exception {
        public CustomException(String message) {
            super(message);
        }
    }
}
