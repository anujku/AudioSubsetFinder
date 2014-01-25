/**
 * The Interface FileMatcher
 */
public interface FileMatcher {

  /**
   * Returns true if the Check is valid as per the specification.
   * 
   * @param args, command line arguments
   * @return true, if is valid
   * @throws Exception the exception
   */
  public void checkValidity(String[] args) throws Exception;

  /**
   * This method will print match between .wav files; if found in
   * given correct command line
   * 
   * @param args: the command line arguments
   */
  public void match(String[] args);

}
