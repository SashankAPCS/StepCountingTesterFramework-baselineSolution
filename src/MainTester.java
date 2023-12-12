import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainTester {
    private static final String TEST_FILE_FOLDER = "testFiles/blk3";

    public static void main(String[] args) {
        StepCounter counter = new DefaultStepCounter();  /* instantiate your step counter here */

        ArrayList<Path> paths = getPaths(TEST_FILE_FOLDER);

        System.out.println("Filename \t\t\t\t prediction \t\t correct \t\t error");
        double totalError = 0;
        int count = 0;
        for (Path path : paths) {
            FileData data = processPath( path );

            int prediction = counter.countSteps(data.text);
            count++;

            int error = data.correctNumberOfSteps - prediction;
            totalError += (error*error);
            String displayPath = padWidthTo(data.filePath, 80);
            System.out.println(displayPath + "\t" + prediction + "\t\t" + data.correctNumberOfSteps + "\t\t" + error);
        }
        System.out.println();
        System.out.println("Mean squared error: " + (totalError/count));
    }

    public static String padWidthTo(String filePath, int width) {
        if (filePath.length() >= width) return filePath;
        int numSpacesToAdd = width - filePath.length();
        for (int i = 0; i < numSpacesToAdd; i++) {
            filePath += " ";
        }
        return filePath;
    }

    public static FileData processPath(Path path) {
        String filename = path.getFileName().toString();
        int numSteps = extractNumSteps(path);
        String text;

        if (numSteps == -1) {
            System.err.println("Couldn't get correct # of steps for file: " + path);
            return null;
        }

        try {
            text = readFile(path.toString());
        } catch (Exception e) {
            System.err.println("Error reading the file: " + path);
            return null;
        }

        return new FileData(text, path.toString(), numSteps);
    }

    public static int extractNumSteps(Path path) {
        String filename = path.getFileName().toString();
        filename = filename.replaceAll("[^\\d]","");
        int steps;
        try {
            steps = Integer.parseInt(filename.trim());
        } catch (Exception e) {
            System.err.println("Error extracting # of steps from filename: " + filename);
            return -1;
        }

        return steps;
    }

    public static ArrayList<Path> getPaths(String folderPath) {
        ArrayList<Path> paths = new ArrayList<>();
        Path workDir = Paths.get(folderPath);
        if (!Files.notExists(workDir)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(workDir)) {
                for (Path p : directoryStream) {
                    paths.add(p);
                }
                return paths;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}
