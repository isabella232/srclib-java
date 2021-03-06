package com.sourcegraph.javagraph;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class generates Sourcefile for OpenJDK projects because they need special treatment, such as
 * - there is no need to run scanners, everything is included into Srcfile's units
 * - source units should contain special markers indicating that we are working with OpenJDK project
 * Usage: JDKSrcFileGenerator PROJECT [DIRECTORY]
 * Expected scenario
 * - clone OpenJDK
 * - build OpenJDK
 * - generate Sourcefile for jdk, langtools, and nashorn
 * - run `srclib make`
 */
public class JDKSrcFileGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: " + JDKSrcFileGenerator.class.getName() + " PROJECT [DIRECTORY]");
            System.err.println();
            System.err.println("PROJECT may be jdk, langtools, nashorns");
            System.exit(1);
        }

        String project = args[0];

        if (args.length > 1) {
            PathUtil.CWD = Paths.get(args[1]);
        }

        JSONUtil.writeJSON(getSrcfile(project));
    }

    /**
     * Generates Srcfile for a given OpenJDK project
     * @param project OpenJDK project name (jdk, langtools, nashorn, ...)
     * @return generated Srcfile
     * @throws IOException
     */
    static Srcfile getSrcfile(String project) throws IOException {
        Collection<SourceUnit> units = new LinkedList<>();

        SourceUnit unit = new SourceUnit();
        unit.Type = "Java";
        unit.Name = project;
        unit.Data.Type = getMarker(project);
        unit.Data.JDKProjectName = project;

        List<String> sourcePaths = JDKProject.getJDKSourcePaths();
        unit.Files = ScanUtil.scanFiles(sourcePaths).
                stream().
                map(PathUtil::relativizeCwd).
                collect(Collectors.toList());

        units.add(unit);

        Srcfile srcfile = new Srcfile();
        srcfile.SourceUnits = units;
        srcfile.Scanners = Collections.emptyList();
        return srcfile;
    }

    private static String getMarker(String project) {
        if ("jdk".equals(project)) {
            return JDKProject.MARKER_JDK;
        }
        return JDKProject.MARKER_JDK_BASED;
    }

}
