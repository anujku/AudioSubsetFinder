import java.lang.Math;

/**
 * The Class FFT used to compute FFT of any input signal of Complex[]
 */
public class FFT {

  /**
   * This method calculates FFT for an array of Complex[] representing
   * input signal. Ref :
   * http://en.wikipedia.org/wiki/Fast_Fourier_transform
   * 
   * @param complexArr of Complex[] -> input signal
   * @return the Complex[]-> the FFT transformation of input signal
   */
  public static Complex[] fft(Complex[] complexArr) {
    // N (as mentioned in formula of FFT): Length of the input signal

    int arrLength = complexArr.length;
    // Return if the input signal's length is 1
    if (arrLength == 1) return new Complex[] {complexArr[0]};

    // This case will not happen because we are padding the signal
    if ((Math.log(arrLength) / Math.log(2)) % 1 != 0) {
      return new Complex[] {complexArr[0]};
    }

    // Store even placed frequency points from input signal
    Complex[] even = new Complex[arrLength / 2];
    for (int i = 0; i < arrLength / 2; i++) {
      even[i] = complexArr[2 * i];
    }
    // Compute FFt on even placed frequency points
    Complex[] complexEven = fft(even);

    // Store odd placed frequency points from input signal
    Complex[] odd = even;
    for (int i = 0; i < arrLength / 2; i++) {
      odd[i] = complexArr[2 * i + 1];
    }

    // Compute FFt on odd placed frequency points
    Complex[] complexOdd = fft(odd);
    // Create FFTSignal for given input signal
    double phase = 0;
    Complex[] fftSig = new Complex[arrLength];

    for (int i = 0; i < arrLength / 2; i++) {
      phase = 2 * i * Math.PI / arrLength;
      Complex complex = new Complex(Math.cos(phase), Math.sin(phase));
      fftSig[i] = complexEven[i].add(complex.multiply(complexOdd[i]));
      fftSig[i + arrLength / 2] =
          complexEven[i].subtract(complex.multiply(complexOdd[i]));
    }
    // Return FFT Signal
    return fftSig;
  }

  /**
   * This method adds Padding to the signal array to make its length
   * is a power of 2. Complex(0.0, 0.0) will be the filled number for
   * padding.
   * 
   * @param signal of Complex[], the input signal
   * @return a Complex[] after padding
   */

  public static Complex[] padding(Complex[] signal) {
    int length = signal.length;
    // get the closest(ceiling) power of 2 of Log length of base 2.
    int ceiling = (int) Math.ceil(Math.log(length) / Math.log(2));
    // the length after padding
    int newlength = (int) Math.pow(2, ceiling);

    // initialize a new signal array with new length
    Complex[] paddedSignal = new Complex[newlength];
    // copy every element in input array to new array
    for (int i = 0; i < length; i++) {
      paddedSignal[i] = signal[i];
    }
    Complex cp = new Complex(0.0, 0.0);
    // fill the rest elements of the new array with Complex(0,0)
    for (int i = length; i < newlength; i++) {
      paddedSignal[i] = cp;
    }

    // Return the signal after adding padding
    return paddedSignal;
  }

}
