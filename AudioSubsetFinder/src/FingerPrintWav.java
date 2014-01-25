import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Class for fingerprinting .wav files.
 * 
 */
public class FingerPrintWav {
  /** The Constant DATA_CHUNK_BYTE. */
  private final static byte[] DATA_CHUNK_BYTE = {0x64, 0x61, 0x74,
      0x61};
  /** The Constant FMT_CHUNK_BYTE. */
  private final static byte[] FMT_CHUNK_BYTE = {0x66, 0x6d, 0x74,
      0x20};
  /** The Constant CHANNEL_OFFSET. */
  private final static int CHANNEL_OFFSET = 10;
  /** The Constant BYTES_PER_SEC_OFFSET. */
  private final static int BYTES_PER_SEC_OFFSET = 16;
  /** The Constant BITS_PER_SAMP_OFFSET. */
  private final static int BITS_PER_SAMP_OFFSET = 22;
  /** The Constant START_FREQ. */
  private final static int START_FREQ = 30;
  /**
   * The Constant THRESHHOLD_LOW used to determine the match
   * between fingerprints of short file and the fingerprints
   * of subset of long file.
   */
  private static final double THRESHHOLD_LOW = 0.32;
  /**
   * The Constant THRESHHOLD_HIGH used to determine the
   * match between the 1st second fingerprints of short file
   * and any one second fingerprints long file.
   */
  private static final double THRESHHOLD_HIGH = 0.4;
  /** The Constant FREQRANGE. */
  private static final int[] FREQRANGE = new int[] {2000, 2700, 2800,
      2900, 3000, 3100, 3200, 3300, 3400, 3500, 3600, 3700, 3800,
      3900, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5600, 6000,
      7000, 8000, 9000, 10000};

  /**
   * Gets the wav file's body or header.
   * 
   * @param file: the file
   * @param choice: String represents HEADER or BODY
   * @return the byte[] of body or header
   */
  public byte[] getWavBodyHeader(byte[] file, String choice) {
    // Find the start index of data chunk in the file
    int datastart = indexOfPattern(file, DATA_CHUNK_BYTE);
    // If no index found, return empty byte[]
    if (datastart == -1) {
      return (new byte[0]);
    }
    // get the body and header information
    byte[] body = new byte[file.length - (datastart + 8)];
    byte[] header = new byte[datastart];
    if (choice.equalsIgnoreCase(Constants.BODY)) {
      System.arraycopy(file, datastart + 8, body, 0, file.length
          - (datastart + 8));
      return body;
    } else if (choice.equalsIgnoreCase(Constants.HEADER)) {
      System.arraycopy(file, 0, header, 0, datastart);
      return header;
    } else
      return (new byte[0]);
  }

  /**
   * Gets the property from wav header.
   * 
   * @param header the header
   * @param property: the property required
   * @return the value of the required property
   */
  public int getWavProperty(byte[] header, String property) {
    int fmtstart = indexOfPattern(header, FMT_CHUNK_BYTE);
    if (fmtstart == -1) {
      return -1;
    }
    // use ByteBuffer to handle endianness of the bytes
    ByteBuffer bb;
    if (property.equalsIgnoreCase(Constants.CHANNELS)) {
      byte[] channelsBytes =
          {header[fmtstart + CHANNEL_OFFSET],
              header[fmtstart + CHANNEL_OFFSET + 1]};
      bb = ByteBuffer.wrap(channelsBytes);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      return bb.getShort();
    } else if (property.equalsIgnoreCase(Constants.BYTES_PER_SEC)) {
      // set bytes per second from the header
      byte[] bytesPerSecBytes =
          {header[fmtstart + BYTES_PER_SEC_OFFSET],
              header[fmtstart + BYTES_PER_SEC_OFFSET + 1],
              header[fmtstart + BYTES_PER_SEC_OFFSET + 2],
              header[fmtstart + BYTES_PER_SEC_OFFSET + 3]};
      bb = ByteBuffer.wrap(bytesPerSecBytes);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      return bb.getInt();
    } else if (property.equalsIgnoreCase(Constants.BITS_PER_SAMPLE)) {
      // set bits per sample from the header
      byte[] bitsPerSampBytes =
          {header[fmtstart + BITS_PER_SAMP_OFFSET],
              header[fmtstart + BITS_PER_SAMP_OFFSET + 1]};
      bb = ByteBuffer.wrap(bitsPerSampBytes);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      return bb.getShort();
    } else
      return -1;
  }

  /**
   * This method calculates the finger print for a give wav
   * file represented by byte[].
   * 
   * @param file: the file
   * @param header: the header info
   * @param channels the channels
   * @param isLeft: true if fingerprinting on left channel,
   *          false if fingerprinting on right channel
   * @return the int[][], calculated fingerprint of a given
   *         file
   */
  public int[][] fingerPrint(byte[] file, byte[] header,
      int channels, boolean isLeft) {
    byte[] body = getWavBodyHeader(file, Constants.BODY);
    int bytesPerSec = getWavProperty(header, Constants.BYTES_PER_SEC);
    if (bytesPerSec == -1) return new int[0][0];
    int bitsPerSamp =
        getWavProperty(header, Constants.BITS_PER_SAMPLE);
    if (bitsPerSamp == -1) return new int[0][0];
    // get the spectrum of the data of the input file
    Complex[][] spectrum =
        calculateSpectrum(body, bytesPerSec, bitsPerSamp, channels,
            isLeft);
    // get the fingerprint of the calculated spectrum
    return getFingerPrint(spectrum);
  }

  /**
   * Calculate spectrum for the given data part of the
   * signal.
   * 
   * @param data the data
   * @param bytesPerSec the bytes per sec
   * @param bitsPerSamp the bits per samp
   * @param channels the channels
   * @param isLeft: true if fingerprinting on left channel,
   *          false if fingerprinting on right channel
   * @return the complex[][], the spectrum
   */
  private Complex[][] calculateSpectrum(byte[] data, int bytesPerSec,
      int bitsPerSamp, int channels, boolean isLeft) {
    // If mono file, isLeft should always be true
    if (channels == 1 && !isLeft) isLeft = true;
    // input data only contains the body, not include
    // header info
    int signalLength = data.length;
    int totalSeconds = signalLength / bytesPerSec;
    Complex[][] spectrum = new Complex[totalSeconds][];
    // for every second, get the sample chunk into a complex
    // array
    Complex[] sampleChunk;
    for (int second = 0; second < totalSeconds; second++) {
      int bytesPerSample = bitsPerSamp / 8;
      // compute the step as channels * bytesPerSample
      // we'll take one sample in every "step" bytes
      int step = channels * bytesPerSample;
      int channelStep = ((isLeft) ? 0 : 1) * bytesPerSample;
      // initialize the sample chunk array
      sampleChunk = new Complex[bytesPerSec / step];
      // create a corresponding complex number for each
      // sample,
      // put the real part as the sample (byte to double),
      // put the imaginary part as 0,
      for (int i = 0; i < bytesPerSec; i = i + step) {
        // compute the realPart
        double realPart;
        
        if (bitsPerSamp == 16)
          realPart =
              bytesToDouble(data[(second * bytesPerSec) + i
                  + channelStep], data[(second * bytesPerSec) + i + 1
                  + channelStep]);
        else
          realPart =
              (double) data[(second * bytesPerSec) + i + channelStep];
        // each sample has been converted to a complex
        // number
        sampleChunk[i / step] = new Complex(realPart, 0);
      }
      // Perform FFT analysis on the chunk:
      spectrum[second] = FFT.fft(FFT.padding(sampleChunk));
    }
    return spectrum;
  }

  /**
   * Gets the finger print using complex signal spectrum.
   * 
   * @param spectrum the spectrum
   * @return the computed finger print
   */
  private int[][] getFingerPrint(Complex[][] spectrum) {
    // Check if spectrum is populated correctly, else return
    if (spectrum.length == 0) return new int[0][0];
    if (spectrum[0].length == 0) return new int[0][0];
    int bytesPerSecond = spectrum[0].length;
    int totalSeconds = spectrum.length;
    // array that saves highest magnitude in each freq range
    // in each second
    double[][] maxMag = new double[totalSeconds][FREQRANGE.length];
    // array saves freqs of highest magnitude in each range
    // in each second
    int[][] maxFreq = new int[totalSeconds][FREQRANGE.length];
    // For every second
    for (int second = 0; second < totalSeconds; second++) {
      // for every freq
      for (int freq = START_FREQ; freq < bytesPerSecond / 2; freq++) {
        // Get the magnitude in log:
        double mag = Math.log(spectrum[second][freq].absolute() + 1);
        // Find out which range we are in:
        int index = getRangeIndex(freq);
        // Save the highest magnitude and corresponding
        // frequency:
        if (index < FREQRANGE.length) {
          if (mag > maxMag[second][index]) {
            maxMag[second][index] = mag;
            maxFreq[second][index] = freq;
          }
        }
      }
    }
    return maxFreq;
  }

  /**
   * Find out in which frequency range the input frequency
   * occurs.
   * 
   * @param freq the freq
   * @return int, the range index of the frequency
   */
  private int getRangeIndex(int freq) {
    try {
      int i = 0;
      while (i < FREQRANGE.length) {
        if (FREQRANGE[i] < freq)
          i++;
        else
          break;
      }
      return i;
    } catch (Exception e) {
      return FREQRANGE.length + 1;
    }
  }

  /**
   * Method to find Index in the data bytes where the
   * pattern starts. We use KMP string search algorithm. REF
   * : http://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%
   * 80%93Pratt_algorithm
   * 
   * @param data the data
   * @param pattern the pattern
   * @return the int, the index where pattern starts
   */
  private int indexOfPattern(byte[] data, byte[] pattern) {
    // Compute failure function
    int[] failureFunction = computeFailureFunction(pattern);
    int patternIndex = 0;
    if (data.length == 0) return -1;
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      while (patternIndex > 0
          && pattern[patternIndex] != data[dataIndex]) {
        patternIndex = failureFunction[patternIndex - 1];
      }
      // when there is a match increase the pattern index by
      // one
      if (pattern[patternIndex] == data[dataIndex]) {
        patternIndex++;
      }
      // in case pattern completely searched return the
      // pattern matching index
      if (patternIndex == pattern.length) {
        return dataIndex - pattern.length + 1;
      }
    }
    return -1;
  }

  /**
   * Computes the failure function using a boot-strapping
   * process, where the pattern is matched against itself.
   * 
   * @param pattern the pattern
   * @return the int[] : failureFunction
   */
  private int[] computeFailureFunction(byte[] pattern) {
    int[] failureFunction = new int[pattern.length];
    int currentIndex = 0;
    for (int nextIndex = 1; nextIndex < pattern.length; nextIndex++) {
      while (currentIndex > 0
          && pattern[currentIndex] != pattern[nextIndex]) {
        currentIndex = failureFunction[currentIndex - 1];
      }
      if (pattern[currentIndex] == pattern[nextIndex]) {
        currentIndex++;
      }
      failureFunction[nextIndex] = currentIndex;
    }
    return failureFunction;
  }

  /**
   * This is a helper method that converts the given 2
   * bytes(in little-endian) to a double.
   * 
   * @param byte1 the byte1
   * @param byte2 the byte2
   * @return the converted output
   */
  private double bytesToDouble(byte byte1, byte byte2) {
    byte[] bytes = {byte1, byte2};
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.getShort();
  }

  /**
   * Method to match two given finger prints.
   * 
   * @param fingerPrintOne : FFT values of file one
   * @param fingerPrintTwo : FFT values of file two
   * @param file1Name the file1 name
   * @param file2Name the file2 name
   * @return true, if match found, otherwise false
   */
  public boolean matchTwoFingerPrints(int[][] fingerPrintOne,
      int[][] fingerPrintTwo, String file1Name, String file2Name) {
    if (fingerPrintOne.length == 0 || fingerPrintTwo.length == 0)
      return false;
    if (fingerPrintOne[0].length == 0
        || fingerPrintTwo[0].length == 0) return false;
    // get the filenames for printing, longer file name
    // followed by shorter file name
    String fileNames =
        (fingerPrintOne.length >= fingerPrintTwo.length) ? (file1Name
            + Constants.SPACE + file2Name) : (file2Name
            + Constants.SPACE + file1Name);
    int vectorSize = fingerPrintOne[0].length;
    // array to hold the longer fingerprint
    int[][] longer;
    // array to hold the shorter fingerprint
    int[][] shorter;
    if (fingerPrintOne.length >= fingerPrintTwo.length) {
      shorter = fingerPrintTwo;
      longer = fingerPrintOne;
    } else {
      shorter = fingerPrintOne;
      longer = fingerPrintTwo;
    }
    int[] first_vector_short = shorter[0];
    double distance = 0.0;
    double bestMatch = 1.0;
    double time = 0.0;
    int[] vector_long = new int[vectorSize];
    int[] subset_long = new int[shorter.length * vectorSize];
    int[] flattened_short = flattenMatrix(shorter);
    int count = 0;
    // FAST ALGO : Check the similarity between first second
    // fingerprints of short file with every 1 second finger
    // prints of the long file.
    for (int i = 0; i <= longer.length - shorter.length; i++) {
      vector_long = longer[i];
      distance =
          Math.abs(getDistance(vector_long, first_vector_short));
      // If distance between one second fingerprints is
      // smaller than
      // the THRESHHOLD_HIGH: switch to SLOW ALGO to confirm
      // the match
      if (distance <= THRESHHOLD_HIGH) {
        count = 0;
        for (int row = i; row < (i + shorter.length); row++) {
          for (int col = 0; col < vectorSize; col++) {
            subset_long[count++] = longer[row][col];
          }
        }
        // SLOW ALGO : Check if the pattern of shorter
        // fingerprints exists in the subset of longer
        // fingerprint starting at this second.
        distance =
            Math.abs(getDistance(subset_long, flattened_short));
        // if distance smaller than THRESHHOLD_LOW, replace
        // the bestMatch
        if (distance <= THRESHHOLD_LOW && distance < bestMatch) {
          bestMatch = distance;
          time = i;
        }
      }
    }
    if (bestMatch != 1.0) {
      // Match Found
      System.out.println("MATCH: " + time + " " + fileNames);
      return true;
    }
    // No match found
    return false;
  }

  /**
   * Method to convert the 2D array of type int[][] into
   * double[].
   * 
   * @param matrix : int[][] representing computed FFT
   *          values of a audio file (.wav for prototype)
   * @return : double[] containing all values of matrix
   *         linearly
   */
  private int[] flattenMatrix(int[][] matrix) {
    // create a result array of length N*5
    int rowSize = matrix.length;
    // Check if matrix has data else return
    if (rowSize == 0) return (new int[0]);
    int colSize = matrix[0].length;
    if (colSize == 0) return (new int[0]);
    int[] results = new int[rowSize * colSize];
    int count = 0;
    // Loop through the matrix to flatten it
    for (int row = 0; row < rowSize; row++) {
      for (int col = 0; col < colSize; col++) {
        results[count++] = matrix[row][col];
      }
    }
    // return the flattened array
    return results;
  }

  /**
   * Gets the distance.
   * 
   * @param fingerPrintOne: the finger print of one file
   * @param fingerPrintTwo the finger print of two file
   * @return the distance
   */
  private double getDistance(int[] fingerPrintOne,
      int[] fingerPrintTwo) {
    double d = 0.0;
    double error = 0.0;
    double sum = 0.0;
    int freqLength = FREQRANGE.length;
    int column = 0;
    double delta = 0.0;
    for (int i = 0; i < fingerPrintOne.length; i++) {
      delta =
          Math.abs(fingerPrintOne[i]) - Math.abs(fingerPrintTwo[i]);
      column = i % freqLength;
      error =
          (column == 0)
              ? (delta / FREQRANGE[0])
              : (delta / (FREQRANGE[column] - FREQRANGE[column - 1]));
      sum += Math.pow(error, 2);
    }
    d = Math.sqrt(sum / fingerPrintTwo.length);
    return d;
  }
}
