package interpreter;

public class tmp {

    static final double EPSILON = 0.001;

    // An example function whose solution
    // is determined using Bisection Method.
    // The function is x^5 + 5x^3 - 5
    static double func(double x)
    {
//        return x * x * x - x * x + 2;
        return (x * x * x * x * x) + (5 * (x * x * x)) - 5;
    }

    // Derivative of the above function
    // which is 5x^4 + 15x^2

    static double derivFunc(double x)
    {
//        return 3 * x * x - 2 * x;
        return (5 * (x * x * x * x)) + (15 * (x * x));
    }

    // Function to find the root
    static void newtonRaphson(double x)
    {
        double h = func(x) / derivFunc(x);


        while (Math.abs(h) >= EPSILON)
        {
            h = func(x) / derivFunc(x);

            // x(i+1) = x(i) - f(x) / f'(x)
            x = x - h;
        }

//        System.out.print("The value of the"
//                + " root is : "
//                + Math.round(x * 1000.0) / 1000.0);
        System.out.println("value is " + x);
    }

    // Driver code
    public static void main (String[] args)
    {

        // Initial values assumed
        double x0 = 1;
        newtonRaphson(x0);
    }
}