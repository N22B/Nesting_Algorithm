package nest.util;


public class Config implements Cloneable{
    public  static int CLIIPER_SCALE = 10000;
    public  static double CURVE_TOLERANCE  = 0.02;
    public  double SPACING ;
    public  int POPULATION_SIZE;
    public  int MUTATION_RATE ; //变异概率，0.01*MUTATION_RATE
    private  boolean CONCAVE ;
    public   boolean USE_HOLE ;

    @Override
    public Config clone() throws CloneNotSupportedException {
        return (Config) super.clone();
    }

    public Config() {
        CLIIPER_SCALE = 10000;
        CURVE_TOLERANCE = 0.3;
        SPACING = 10;
        POPULATION_SIZE = 10;
        MUTATION_RATE = 10;
        CONCAVE = false;
        USE_HOLE = false;
    }

    public boolean isCONCAVE() {
        return CONCAVE;
    }

    public boolean isUSE_HOLE() {
        return USE_HOLE;
    }
}
