package com.funkdefino.autoback;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <p/>
 * <code>$Id: $</code>
 * @author Differitas (David M. Lang)
 * @version $Revision: $
 */
public final class FileInfo {

  //** --------------------------------------------------------------- Constants

  private final static String EXTENSION = ".ptx";

  //** -------------------------------------------------------------------- Data

  private String base;
  private int    rollover;

  //** ------------------------------------------------------------ Construction

  /**
   * Ctor.
   * @param base directory base.
   */
  public FileInfo(String base, int rollover) {
    this.rollover = rollover;
    this.base = base;
  }

  //** -------------------------------------------------------------- Operations

  /**
   * Caculates the next sequence number.
   * @param src the source file.
   * @return the sequence number (or -1).
   */
  public int getNextSequence(File src) {

    File f = new File(base);
    int sequence = -1;

    if(f.isDirectory()) {

      // List & sort, newest first
      File[] files = f.listFiles();
      Arrays.sort(files, new Comparator<File>() {
          public int compare(File f1, File f2) {
              return Long.valueOf(f2.lastModified()).compareTo(
                      f1.lastModified());
          }
      });

      if(files.length == 0) sequence = 1;
      else {
        if(src.lastModified() > files[0].lastModified()) {
          // Increment the sequence number, with rollover.
          sequence = getSequence(files[0]);
          if(sequence != -1) {
            if(sequence == rollover)
               sequence = 0;
            ++sequence;
          }
        }
      }
    }

    return sequence;

  } // getNextSequence()

  /**
   * Calculates the file's sequence number.
   * @param file the file.
   * @return the sequence number (or -1).
   */
  private int getSequence(File file) {

    int nSequence = -1;

    String name = file.getName();
    if(name.endsWith(EXTENSION)) {
       name = name.substring(0, name.length() - EXTENSION.length());
       int nIndex = name.lastIndexOf('.');
       if(nIndex != -1) {
          String sequence = name.substring(++nIndex, name.length());
          nSequence = Integer.parseInt(sequence);
       }
    }

    return nSequence;

  } // getSequence()

} // class FileInfo
