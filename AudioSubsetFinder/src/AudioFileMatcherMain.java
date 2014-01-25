/**
 * The Class AudioFileMatcherMain.
 */
public class AudioFileMatcherMain {
  /**
   * The main method.
   * 
   * @param args: Command line arguments passed to be
   *          validated if the path specification are
   *          correct then program exits with status zero,
   *          in case of an error, it exits by printing
   *          'ERROR' to the standard output and exit status
   *          any other than zero (10 here).
   */
  public static void main(String[] args) {
    FileMatcher audioFileMatcher =
        AbstractFileMatcherFactory.getAudioFileMatcher();
    try {
      // Check if command Line is correct
      audioFileMatcher.checkValidity(args);
      // Print match if any according to specification
      audioFileMatcher.match(args);
      System.exit(0);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(10);
    }
  }
}
