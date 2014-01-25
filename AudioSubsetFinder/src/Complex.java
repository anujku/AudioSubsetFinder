/**
 * The Class Complex.
 */
public class Complex {

  /** The real_part of the complex number. */
  private final double real_part;

  /** The imaginary_part of the complex number. */
  private final double imaginary_part;

  /**
   * Instantiates a new complex pbject.
   * 
   * @param real the real part
   * @param imag the imaginary part
   */
  public Complex(double real, double imag) {
    this.real_part = real;
    this.imaginary_part = imag;
  }

  /**
   * Method to add two complex numbers.
   * 
   * @param complex, the complex number to be added to this instance
   * @return the Complex, the addition of input complex number with
   *         this.
   */
  public Complex add(Complex complex) {
    double real = this.real_part + complex.real_part;
    double imag = this.imaginary_part + complex.imaginary_part;
    return new Complex(real, imag);
  }

  /**
   * Method to subtract two complex numbers.
   * 
   * @param complex, the complex number to be subtracted from this
   *        instance
   * @return the Complex, the subtraction of input complex number from
   *         this.
   */
  public Complex subtract(Complex complex) {
    double real = this.real_part - complex.real_part;
    double imag = this.imaginary_part - complex.imaginary_part;
    return new Complex(real, imag);
  }

  /**
   * Method to multiply two complex numbers.
   * 
   * @param complex, the complex number to be multiplied to this
   *        instance
   * @return the Complex, the multiplication of input complex number
   *         with this.
   */
  public Complex multiply(Complex complex) {
    double real =
        this.real_part * complex.real_part - this.imaginary_part
            * complex.imaginary_part;
    double imag =
        this.real_part * complex.imaginary_part + this.imaginary_part
            * complex.real_part;
    return new Complex(real, imag);
  }

  /**
   * Method to return the absolute value of a Complex number.
   * 
   * @return the double, the absolute value
   */
  public double absolute() {
    return Math.hypot(real_part, imaginary_part);
  }

  /**
   * Gets the imaginary part of the complex number.
   * 
   * @return the imaginary part
   */
  public double getImaginaryPart() {
    return this.imaginary_part;
  }

  /**
   * Gets the real part of the complex number.
   * 
   * @return the real part
   */
  public double getRealPart() {
    return real_part;
  }
}
