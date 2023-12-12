import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DefaultStepCounter implements StepCounter {

    @Override
    public int countSteps(ArrayList<Double> xAcc, ArrayList<Double> yAcc, ArrayList<Double> zAcc, ArrayList<Double> xGyro, ArrayList<Double> yGyro, ArrayList<Double> zGyro) {
        ArrayList<Double> mag = getMagnitudes(xAcc, yAcc, zAcc);

        ArrayList<Integer> peakIndexes = getPeakIndexes(mag);
        ArrayList<Double> peakValues = getPeakValuesFromIndexes(peakIndexes, mag);

        ArrayList<Integer> peakIndexes2 = getPeakIndexes2(vertFilterPeaks(countPeaks(mag), vertThresholdVal(countPeaks(mag))), mag);
        ArrayList<Double> peakValues2 = getPeakValuesFromIndexes(peakIndexes2, mag);

        ArrayList<Integer> peakIndexes3 = horzFilterPeaks(peakIndexes2);
        ArrayList<Double> peakValues3 = getPeakValuesFromIndexes(peakIndexes3, mag);

        ArrayList<Integer> peakIndexes4 = getPeakIndexes2(smoothData(mag), mag);

        return peakIndexes3.size();
    }


    public static ArrayList<Integer> horzFilterPeaks(ArrayList<Integer> peakIndexes) {
        ArrayList<Integer> newPeakIndexes = new ArrayList<>();

        int startIndex = 5;

        for (int i = startIndex; i < peakIndexes.size(); i += 5) {


            int distanceTotal = 0;
            double avgDistance;
            for (int j = startIndex - 5; j < i - 1; j++) {
                distanceTotal += (peakIndexes.get(j + 1) - peakIndexes.get(j));


            }
            avgDistance = (double) distanceTotal / 5.0;


            for (int k = startIndex - 5; k < i; k++) {
                if (peakIndexes.get(k + 1) - peakIndexes.get(k) >= avgDistance) {
                    newPeakIndexes.add(peakIndexes.get(k));
                }
            }


            startIndex = i + 5;
        }


        return newPeakIndexes;
    }


    public static double vertThresholdVal(ArrayList<Double> peaks) {
        double added = 0;


        for (int i = 0; i < peaks.size(); i++) {
            added += peaks.get(i);
        }


        double avg = added / (double) peaks.size();


        return avg;
    }


    public static ArrayList<Double> vertFilterPeaks(ArrayList<Double> peaks, double vertThresholdVal) {
        ArrayList<Double> newPeaks = new ArrayList<>();


        for (int i = 0; i < peaks.size(); i++) {
            if (peaks.get(i) > vertThresholdVal) {
                newPeaks.add(peaks.get(i));
            }
        }
        return newPeaks;
    }

    public ArrayList<Double> smoothData (ArrayList<Double> peaks) {
        ArrayList<Double> smoothPeaks = new ArrayList<>();

        int startIndex = 7;

        for (int i = 0; i < peaks.size(); i+=7) {

            double weightedAvg;

            for (int j = i; j < i; j+= i+1) {

                weightedAvg = (((((double) (1/7)) * peaks.get(j)) + (((double) (1/7)) * peaks.get(j + 1)) + (((double) (1/7)) * peaks.get(j + 2)) + (((double) (1/7)) * peaks.get(j + 3)) + (((double) (1/7)) * peaks.get(j + 4)) + (((double) (1/7)) * peaks.get(j + 5)) + (((double) (1/7)) * peaks.get(j + 6)) + (((double) (1/7)) * peaks.get(j + 7))));
                smoothPeaks.add(weightedAvg);

            }

        }
        return smoothPeaks;
    }


    public static ArrayList<Integer> getPeakIndexes2(ArrayList<Double> newPeaks, ArrayList<Double> mags) {
        ArrayList<Integer> peakLocations = new ArrayList<>();


        for (int j = 0; j < newPeaks.size(); j++) {
            for (int i = 1; i < mags.size() - 1; i++) {
                if (mags.get(i) == newPeaks.get(j)) {
                    peakLocations.add(i);
                }
            }
        }
        return peakLocations;
    }

    public static ArrayList<Double> getPeakValuesFromIndexes(ArrayList<Integer> peakIndexes, ArrayList<Double> mags) {
        ArrayList<Double> peaks = new ArrayList<>();
        for (int i = 0; i < peakIndexes.size(); i++) {
            double val = mags.get(peakIndexes.get(i));
            peaks.add(val);
        }
        return peaks;
    }

    public static ArrayList<Integer> getPeakIndexes(ArrayList<Double> mags) {
        ArrayList<Integer> peakIndexes = new ArrayList<>();

        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i - 1) < mags.get(i) && mags.get(i) > mags.get(i + 1)) {
                peakIndexes.add(i);
            }
        }
        return peakIndexes;
    }

    public static ArrayList<Double> countPeaks(ArrayList<Double> mags) {
        ArrayList<Double> peaks = new ArrayList<>();
        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i) > mags.get(i - 1) && mags.get(i) > mags.get(i + 1)) {
                peaks.add(mags.get(i));
            }
        }
        return peaks;
    }

    public static ArrayList<Double> getMagnitudes(ArrayList<Double> accX, ArrayList<Double> accY, ArrayList<Double> accZ) {
        ArrayList<Double> output = new ArrayList<>();
        if (accX.size() != accY.size() || accX.size() != accZ.size() || accY.size() != accZ.size()) {
            System.out.println("WARNING: x, y, z acceleration lists not the same length");
        }

        for (int i = 0; i < accX.size(); i++) {
            double mag = magnitude(accX.get(i), accY.get(i), accZ.get(i));
            output.add(mag);
        }
        return output;
    }

    private static double magnitude(Double x, Double y, Double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static ArrayList<Double> getColumnAsList(String[] lines, int colNum) {
        ArrayList<Double> output = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {  // start at 1 to skip the header line
            String line = lines[i];

            String[] values = line.split(",");
            String columnValue = values[colNum];
            double valueAsDouble = Double.parseDouble(columnValue.trim());
            output.add(valueAsDouble);
        }

        return output;
    }


    @Override
    public int countSteps(String csvFileText) {
        String[] lines = csvFileText.split("\n");

        ArrayList<Double> accX = getColumnAsList(lines, 0);
        ArrayList<Double> accY = getColumnAsList(lines, 1);
        ArrayList<Double> accZ = getColumnAsList(lines, 2);
        ArrayList<Double> gyroX = getColumnAsList(lines, 3);
        ArrayList<Double> gyroY = getColumnAsList(lines, 4);
        ArrayList<Double> gyroZ = getColumnAsList(lines, 5);

        return countSteps(accX, accY, accZ, gyroX, gyroY, gyroZ);
    }
}


