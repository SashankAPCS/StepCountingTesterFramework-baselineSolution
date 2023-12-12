import java.util.ArrayList;

public interface StepCounter {
    public int countSteps(ArrayList<Double> xAcc,
                          ArrayList<Double> yAcc,
                          ArrayList<Double> zAcc,
                          ArrayList<Double> xGyro,
                          ArrayList<Double> yGyro,
                          ArrayList<Double> zGyro);

    public int countSteps(String csvFileText);
}
