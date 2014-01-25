import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for checking file format and syntax.
 */
public abstract class AbstractFileMatcherFactory {
  /**
   * Check validity and compare the Audio.
   * 
   * @return AudioFileMatcher : Implementation of Check
   */
  public static FileMatcher getAudioFileMatcher() {
    return new AudioFileMatcher();
  }

  /**
   * Base class Implementation of the interface FileMatcher.
   */
  private static abstract class FileMatcherBase
      implements
        FileMatcher {
    /**
     * Returns true if the implementation is valid as per
     * the specification throws Exception : If encountered a
     * invalid command line as per specification.
     * 
     * @param args the args
     * @throws Exception the exception
     */
    public abstract void checkValidity(String[] args)
        throws Exception;

    /**
     * This method will print match between .wav files; if
     * found in given correct command line
     * 
     * @param args : the command line arguments
     */
    public abstract void match(String[] args);
  }
  /**
   * Implementation to match audio files.
   */
  private static class AudioFileMatcher extends FileMatcherBase {
    /**
     * The header's length used for determining the type of
     * the audio file.
     */
    private static final int AUDIO_FILE_HEADER_LENGTH = 12;
    /**
     * True if the first pathspec is a file
     */
    private boolean isFirstFile;
    /**
     * if the second pathspec is a file
     */
    private boolean isSecondFile;
    /** Location of the output file paths converted to wav. */
    private Map<String, String> toConvertToWavFileMap =
        new HashMap<String, String>();
    /**
     * Location of the output files copied with change of
     * extension to mp3.
     */
    private Map<String, String> toRenameToMp3FileMap =
        new HashMap<String, String>();
    /** Instance of the FingerPrintWav class *. */
    private FingerPrintWav fingerPrintWav = new FingerPrintWav();
    /** The left channel fingerprints of all files. */
    private Map<File, int[][]> leftChannelFingerprints =
        new HashMap<File, int[][]>();
    /** The right channel fingerprints of all files. */
    private Map<File, int[][]> rightChannelFingerprints =
        new HashMap<File, int[][]>();

    /**
     * Returns true if the input arguments are file formats
     * as valid as per specification.
     * 
     * @param args : Command line arguments
     * @throws Exception the exception
     */
    @Override
    public void checkValidity(String[] args) throws Exception {
      // throw exception if arguments passed are null
      if (args == null) {
        throw new Exception(Constants.ERROR + "Null arguments passed");
      }
      // throw exception if arguments have length less than
      // or more than 4.
      if (args.length != 4) {
        throw new Exception(Constants.ERROR
            + "Invalid length arguments passed");
      }
      // throw exception if any of argument is null or empty
      for (int i = 0; i < args.length; i++) {
        if (args[i] == null || ((String) args[i]).isEmpty()) {
          throw new Exception(Constants.ERROR
              + "Arguments contain empty values");
        }
      }
      // Call the method to check the validity of command
      // line arguments
      validateArguments(args);
    }

    /**
     * Method to check if both pathspec are valid
     * 
     * @param args: Command Line args
     * @throws Exception the exception
     */
    private void validateArguments(String[] args) throws Exception {
      boolean isFirstPathSpec =
          isPathSpec((String) args[0], (String) args[1]);
      boolean isSecondPathSpec =
          isPathSpec((String) args[2], (String) args[3]);
      if (isFirstPathSpec)
        isFirstFile =
            args[0].equals(Constants.FILE_PREFIX_1)
                || args[0].equals(Constants.FILE_PREFIX_2);
      if (isSecondPathSpec)
        isSecondFile =
            args[2].equals(Constants.FILE_PREFIX_1)
                || args[2].equals(Constants.FILE_PREFIX_2);
    }

    /**
     * Method to check the validity of given arguments.
     * 
     * @param prefix :the command option
     * @param path: the path of the pathspec
     * @return true, if valid
     * @throws Exception : the exception to notify the error
     */
    private boolean isPathSpec(String prefix, String path)
        throws Exception {
      // to handle cases where path contains '~'
      path =
          path.replace(Constants.TILDE,
              System.getProperty(Constants.USER_HOME));
      File file = new File(path);
      // this pathspec is with a -f or --file command option
      if (prefix.equals(Constants.FILE_PREFIX_1)
          || prefix.equals(Constants.FILE_PREFIX_2)) {
        if (!file.exists() || file.isDirectory()) {
          throw new Exception(Constants.ERROR + "File " + path
              + " does not exist or it is a directory");
        } else {
          return isAudioFile(file.getAbsolutePath());
        }
      } else
      // this pathspec is with a -d or --dir command option
      if (prefix.equals(Constants.DIR_PREFIX_1)
          || prefix.equals(Constants.DIR_PREFIX_2)) {
        if (!file.isDirectory()) {
          throw new Exception(Constants.ERROR + path
              + " is not a directory");
        } else {
          // Loop through all the files in the directory
          File[] allFiles = file.listFiles();
          for (File aFile : allFiles) {
            if (aFile.isDirectory()) {
              throw new Exception(Constants.ERROR + path
                  + " contains subdirectory");
            } else
              isAudioFile(aFile.getAbsolutePath());
          }
          // Valid Directory
          return true;
        }
      } else {
        throw new Exception(Constants.ERROR + prefix
            + " - invalid operators passed");
      }
    }

    /**
     * Method checks contents of a given file to determine
     * if its a valid audio file. We support .wav and .mp3
     * files
     * 
     * @param filepath: path of the given file
     * @return true if the given filepath points to a valid
     *         file as per specification
     * @throws Exception the exception
     */
    private boolean isAudioFile(String filepath) throws Exception {
      // buffer to store first 12 bytes of a file
      byte[] header = new byte[AUDIO_FILE_HEADER_LENGTH];
      // read file using FileInputStream
      FileInputStream fileInputStream = new FileInputStream(filepath);
      fileInputStream.read(header);
      fileInputStream.close();
      // check the type of the audio file by the header
      if (AudioFileTypeValidator.isWav(header)) {
        return true;
      } else if (AudioFileTypeValidator.isMp3Signature1(header)
          || AudioFileTypeValidator.isMp3Signature2(header)) {
        // Check if the file has the correct extension
        String extension =
            filepath.substring(filepath.lastIndexOf(".") + 1,
                filepath.length());
        String finalInputFilePath = filepath;
        // if File does not have extension as .mp3, we
        // create the copy
        // file in /tmp/AAX with extension .mp3
        if (!extension.equalsIgnoreCase(Constants.MP3)) {
          File file = new File(filepath);
          finalInputFilePath =
              Constants.OUTPUT_FILE_LOCATION + Constants.SLASH
                  + file.getName().replaceFirst("[.][^.]+$", "")
                  + Constants.DOT + Constants.MP3;
          FileUtils.copyFile(file, new File(finalInputFilePath));
          toRenameToMp3FileMap.put(filepath, finalInputFilePath);
        }
        toConvertToWavFileMap.put(finalInputFilePath,
            FileUtils.getOutputFilePath(filepath));
        return true;
      } else {
        throw new Exception(Constants.ERROR + filepath
            + " is not a valid audio file");
      }
    }

    /**
     * Method to print a match between valid audio files
     * 
     * @param args: Command line args
     */
    @Override
    public void match(String[] args) {
      try {
        // Check if user input non .wave files
        if (!toConvertToWavFileMap.isEmpty())
        // convert to Canonical Form
          FileUtils.convertToWav(toConvertToWavFileMap);
      } catch (Exception e) {}
      args[1] =
          args[1].replace(Constants.TILDE,
              System.getProperty(Constants.USER_HOME));
      args[3] =
          args[3].replace(Constants.TILDE,
              System.getProperty(Constants.USER_HOME));
      try {
        // When -f, --file or --file, -f
        if (isFirstFile && isSecondFile) {
          File fileOne = new File(args[1]);
          File fileTwo = new File(args[3]);
          // Compute FingerPrints of both files
          matchTwoFiles(fileOne, fileTwo);
        } else if (isFirstFile != isSecondFile) {
          // store the file and directory
          String file = isFirstFile ? args[1] : args[3];
          String directory = isFirstFile ? args[3] : args[1];
          File fileOne = new File(file);
          // Get all Files in the shorter directory
          File[] allFilesDir = (new File(directory)).listFiles();
          if (allFilesDir.length == 0) {
            // Empty Directory, hence don't proceed
            return;
          }
          for (File aFile : allFilesDir) {
            // Call the Match method to detect a match
            matchTwoFiles(fileOne, aFile);
          }
        } else {
          // When comparing two directories
          File[] allFilesDir1 = (new File(args[1])).listFiles();
          File[] allFilesDir2 = (new File(args[3])).listFiles();

          // One or both directories are empty, return
          if (allFilesDir1.length == 0 || allFilesDir2.length == 0) return;

          // iterating over two directories
          for (File aFileDir1 : allFilesDir1) {
            for (File aFileDir2 : allFilesDir2) {
              matchTwoFiles(aFileDir1, aFileDir2);
            }
          }
        }
        // delete the temporary file created while
        // converting input files to canonical form
        FileUtils.deleteTempFiles(toConvertToWavFileMap);
        // delete the temporary file created while
        // renaming extensions
        FileUtils.deleteTempFiles(toRenameToMp3FileMap);
      } catch (Exception e) {}
    }

    /**
     * Match two files.
     * 
     * @param file1: audio file1
     * @param file2: audio file2
     * @throws Exception the exception
     */
    private void matchTwoFiles(File file1, File file2)
        throws Exception {

      // Get the byte arrays of the files
      byte[] file1Bytes =
          FileUtils.readFileBytes(getActualFile(file1));
      byte[] file2Bytes =
          FileUtils.readFileBytes(getActualFile(file2));
      // Get the headers
      byte[] headerFile1 =
          fingerPrintWav.getWavBodyHeader(file1Bytes,
              Constants.HEADER);
      byte[] headerFile2 =
          fingerPrintWav.getWavBodyHeader(file2Bytes,
              Constants.HEADER);
      // Get the channels
      int channelFile1 =
          fingerPrintWav.getWavProperty(headerFile1,
              Constants.CHANNELS);
      if (channelFile1 == -1) return;
      int channelFile2 =
          fingerPrintWav.getWavProperty(headerFile2,
              Constants.CHANNELS);
      if (channelFile2 == -1) return;
      
      
      // Declare 2-D arrays for storing left channel
      // fingerprints of both files
      int[][] fingerPrintLeftFile1;
      int[][] fingerPrintLeftFile2;
      // Initialize empty 2-D arrays for storing right
      // channel
      // fingerprints of both files
      int[][] fingerPrintRightFile1 = new int[0][0];
      int[][] fingerPrintRightFile2 = new int[0][0];
      // Caching : Populate left channel fingerprints of
      // both files from
      // the cache, if not present then cache it.
      if (!leftChannelFingerprints.containsKey(file1)) {
        fingerPrintLeftFile1 =
            fingerPrintWav.fingerPrint(file1Bytes, headerFile1,
                channelFile1, true);
        leftChannelFingerprints.put(file1, fingerPrintLeftFile1);
      } else {
        fingerPrintLeftFile1 = leftChannelFingerprints.get(file1);
      }
      if (!leftChannelFingerprints.containsKey(file2)) {
        fingerPrintLeftFile2 =
            fingerPrintWav.fingerPrint(file2Bytes, headerFile2,
                channelFile2, true);
        leftChannelFingerprints.put(file2, fingerPrintLeftFile2);
      } else {
        fingerPrintLeftFile2 = leftChannelFingerprints.get(file2);
      }
      
      // Get filenames
      String file1Name = file1.getName();
      String file2Name = file2.getName();
      // If match is detected between left channel
      // fingerprints of
      // two files, return
      if (fingerPrintWav.matchTwoFingerPrints(fingerPrintLeftFile1,
          fingerPrintLeftFile2, file1Name, file2Name)) {
        return;
      }
      // Otherwise, check if any file is stereo and get
      // right channel
      // fingerprints from the cache if present, else
      // compute and store it.
      if (channelFile1 == 2) {
        if (!rightChannelFingerprints.containsKey(file1)) {
          fingerPrintRightFile1 =
              fingerPrintWav.fingerPrint(file1Bytes, headerFile1,
                  channelFile1, false);
          rightChannelFingerprints.put(file1, fingerPrintRightFile1);
        } else {
          fingerPrintRightFile1 = rightChannelFingerprints.get(file1);
        }
      }
      if (channelFile2 == 2) {
        if (!rightChannelFingerprints.containsKey(file2)) {
          fingerPrintRightFile2 =
              fingerPrintWav.fingerPrint(file2Bytes, headerFile2,
                  channelFile2, false);
          rightChannelFingerprints.put(file2, fingerPrintRightFile2);
        } else {
          fingerPrintRightFile2 = rightChannelFingerprints.get(file2);
        }
      }
      // Try to match two files by
      // (LEFT VS RIGHT, RIGHT VS LEFT, RIGHT VS RIGHT)
      // channel fingerprints.
      // Only proceed with matching when no match is found
      if (!fingerPrintWav.matchTwoFingerPrints(fingerPrintLeftFile1,
          fingerPrintRightFile2, file1Name, file2Name)) {
        if (!fingerPrintWav.matchTwoFingerPrints(
            fingerPrintRightFile1, fingerPrintLeftFile2, file1Name,
            file2Name)) {
          fingerPrintWav.matchTwoFingerPrints(fingerPrintRightFile1,
              fingerPrintRightFile2, file1Name, file2Name);
        }
      }
    }

    /**
     * Gets the file that is already converted to wav or
     * with correct extension.
     * 
     * @param file: the file from the command args
     * @return the actual file that is used for
     *         fingerprinting
     */
    private File getActualFile(File file) {
      String filePath = file.getAbsolutePath();
      if (toRenameToMp3FileMap.containsKey(filePath)) {
        filePath = toRenameToMp3FileMap.get(filePath);
      }
      if (toConvertToWavFileMap.containsKey(filePath)) {
        file = new File(toConvertToWavFileMap.get(filePath));
      }
      return file;
    }
  }
}
