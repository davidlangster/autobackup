package com.funkdefino.autoback;

import java.nio.channels.FileChannel;
import java.util.concurrent.*;
import java.io.*;

/**
 * <p/>
 * <code>$Id: $</code>
 * @author Differitas (David M. Lang)
 * @version $Revision: $
 */
public final class AutoBackup implements Runnable{

  //** --------------------------------------------------------------- Constants

  private final static String NO_SRC_FILE = "No src file : %s";
  private final static String BACKUP_DIR = "Session File Backups";
  private final static String BAK = "bak";
  private final static String EXT = "ptx";

  //** -------------------------------------------------------------------- Data

  private ScheduledExecutorService scheduler;
  private FileInfo fileInfo;
  private String session;
  private File bkupDir;
  private File srcFile;

  //** ------------------------------------------------------------- Application

  /**
   * Application entry point.
   * @param args command-line arguments.
   */
  public static void main(String[] args) {

    if(args.length < 4) {
      System.out.println("Usage : AutoBackup [base][session][backups][period]<directory><ext>");
      return;
    }

    String base    = args[0];
    String session = args[1];
    String backups = args[2];
    String period  = args[3];

    // Default to ProTools for backwards compatibilty
    String directory = args.length >= 5 ? args[4] : BACKUP_DIR;
    String ext = args.length >= 6 ? args[5] : EXT;

    System.out.println(String.format("AutoBackup : %s\\%s.%s (backups=%s;period=%s)",
                       base, session, ext, backups, period));

    try  {new AutoBackup(base, session, ext, directory, backups, period);}
    catch(Exception excp) {
      excp.printStackTrace();
    }

  } // main()

  //** ------------------------------------------------------------ Construction

  /**
   * Ctor.
   * @param base the session base directory.
   * @param session the session name.
   * @param ext the session extension.
   * @param directory the backup directory.
   * @param backups the number of backups before rollover.
   * @param period the backup period.
   * @throws Exception
   */
  public AutoBackup(String base, String session, String ext, String directory,
                    String backups, String period) throws Exception
  {
    initialise(base, session, ext, directory, backups, period);
  }

  //** ----------------------------------------- Operations (Runnable interface)

  public void run() {

    try {
      int sequence = fileInfo.getNextSequence(srcFile);
      if(sequence != -1) {
         String dstFile = String.format("%s\\%s.%s.%03d.%s", bkupDir, session, BAK, sequence, EXT);
         copyFile(srcFile, new File(dstFile));
         System.out.println("Created : " + dstFile);
      }
    }
    catch(Exception excp) {
      excp.printStackTrace();
    }

  } // run()

  /**
   * Performs startup initialisation.
   * @param base the base directory.
   * @param session the session name.
   * @param ext the session extension.
   * @param directory the backup directory.
   * @param backups the number of backups before rollover.
   * @param period the backup period.
   * @throws Exception  on error.
   */
  private void initialise(String base, String session, String ext, String directory,
                          String backups, String period)
                          throws Exception {

    this.session = session;

    srcFile = new File(String.format("%s\\%s.%s", base, session, ext));
    if(!srcFile.exists()) {
      throw new Exception(String.format(NO_SRC_FILE, srcFile.getAbsolutePath()));
    }

    int nBackups = Integer.parseInt(backups);
    int nPeriod  = Integer.parseInt(period );

    bkupDir = new File(String.format("%s\\%s", base, directory));
    if(!bkupDir.exists()) bkupDir.mkdir();
    fileInfo  = new FileInfo(bkupDir.getAbsolutePath(), nBackups);

    scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this, nPeriod, nPeriod, TimeUnit.MINUTES);

  } // initialise()

  /**
   * Copies a file.
   * @param srcFile the source file.
   * @param dstFile the destination file.
   * @throws IOException on error.
   */
  private static void copyFile(File srcFile, File dstFile) throws IOException {

    FileInputStream  fis = null;
    FileOutputStream fos = null;
    FileChannel src = null;
    FileChannel dst = null;

    dstFile.createNewFile();

    try {
      fis = new FileInputStream (srcFile);
      fos = new FileOutputStream(dstFile);
      src = fis.getChannel();
      dst = fos.getChannel();
      dst.transferFrom(src, 0, src.size());
    }
    finally {
      close(fis);
      close(fos);
      close(src);
      close(dst);
    }

  } // copyFile()

  /**
   * This closes a resource.
   * @param c the closeable resource.
   */
  private static void close(Closeable c) {
    if(c != null) {
       try  {c.close();}
       catch(IOException excp) {
         excp.printStackTrace();
       }
    }
  }

} // class AutoBackup
