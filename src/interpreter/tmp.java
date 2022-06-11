package interpreter;

public class tmp {

    static final double EPSILON = 0.001;

    // An example function whose solution
    // is determined using Bisection Method.

    static double func(double x) {
        // The function F1 is x^5 + 5x^3 - 5
//        return (x * x * x * x * x) + (5 * (x * x * x)) - 5;
        // The function F2 is 8x^6 + 3x^2 - 3
        return (8 * (x * x * x * x * x * x)) + (3 * (x * x)) - 3;
    }

    // Derivative of the above function

    static double derivFunc(double x) {
        // f1' is 5x^4 + 15x^2
//        return (5 * (x * x * x * x)) + (15 * (x * x));
        // f2' is 48x^5 + 6x
        return (48 * (x * x * x * x * x)) + (6 * x);
    }

    // Function to find the root
    static void newtonRaphson(double x) {
        double h = func(x) / derivFunc(x);


        while (Math.abs(h) >= EPSILON) {
            System.out.println("f = " + func(x));
            System.out.println("f' = " + derivFunc(x));
            h = func(x) / derivFunc(x);
            System.out.println("value of h is: " + h);

            // x(i+1) = x(i) - f(x) / f'(x)
            x = x - h;
        }

        System.out.println("value is " + x);
    }

    // Driver code
    public static void main(String[] args) {

        // Initial values assumed
        double x0 = 3;
        newtonRaphson(x0);
    }
}