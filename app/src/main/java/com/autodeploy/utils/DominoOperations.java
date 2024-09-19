package com.autodeploy.utils;

import lotus.domino.*;
import java.util.UUID;
public class DominoOperations {

  private Session session;
  private String sourceNtfFileName;
  private String targetUpdateNsfFilePath;
  private String targetUpdateServerName;
  private String randomString;
  public DominoOperations(
    Session session,
    String sourceNtfFileName,
    String targetUpdateNsfFilePath,
    String targetServer
  ) {
    this.session = session;
    this.sourceNtfFileName = sourceNtfFileName;
    this.targetUpdateNsfFilePath = targetUpdateNsfFilePath;
    this.targetUpdateServerName = targetServer;
  }

  public int performOperations() throws NotesException {
    Database sourceDbOnServer = null;
    Database sourceDb = session.getDatabase(null, this.sourceNtfFileName);
    if(sourceDb.isOpen()){
      System.out.println("Success: Open source database on local server successfully."+sourceDb.getTitle());
    }
    Database targetDb = session.getDatabase(
      this.targetUpdateServerName,
      this.targetUpdateNsfFilePath
    );
    try {
      if (sourceDb == null || !sourceDb.isOpen()) {
        System.out.println(
          "Error: Failed to open the source database with the name: " +
          sourceNtfFileName 
        );
      }
      String sourceDbTitle = sourceDb.getTitle();
      
     sourceDbOnServer= session.getDatabase(
        this.targetUpdateServerName,
        sourceDb.getFileName()
      );
      System.out.println("sourceDbOnServer: "+this.sourceNtfFileName);
      if (sourceDbOnServer.isOpen()) {
        //remove the source database on the target server
        sourceDbOnServer.remove();
        System.out.println("Success: Remove source database on target server successfully.");
      }
        
      //copy sourceDb to target server
      sourceDbOnServer = sourceDb.createCopy(this.targetUpdateServerName, sourceDb.getFileName());
      System.out.println("Success create source template copy on target server:"+this.targetUpdateServerName);
      if (sourceDbOnServer.isOpen()) {
        System.out.println(
          "Success: Create lasted source Database on target server successfully."
        );
      }

      if (!targetDb.isOpen()) {
        System.out.println(
          "Can't find the target database: " +
          sourceNtfFileName +
          " on the server: " +
          targetUpdateServerName
        );
        System.out.println(
          "Creating a new copy from source Db on the target server: " +
          targetUpdateServerName
        );
        //create a new database on the target server with DbDirectory
        DbDirectory dir = session.getDbDirectory(this.targetUpdateServerName);
        targetDb=dir.createDatabase( this.targetUpdateNsfFilePath);
        // targetDb = sourceDbOnServer.createFromTemplate (this.targetUpdateServerName, sourceDbOnServer.getTitle(), true);
        
      }

      //now starting update the target database with the source database
      sourceDb.recycle(); //close the source database
      targetDb.recycle(); //close the target database
      
      String command =
        "load convert -d " +
        this.targetUpdateNsfFilePath +
        " * " +
        sourceDbOnServer.getFileName();
      sourceDbOnServer.recycle(); //close the source database on the target server
      //start update and refresh the design
      System.out.println("execute command:" + command);
      String log = session.sendConsoleCommand(this.targetUpdateServerName, command);
      System.out.println("Deploy log: " + log);
      
      targetDb =
        session.getDatabase(
          this.targetUpdateServerName,
          this.targetUpdateNsfFilePath
        );
      if (targetDb.isOpen()) {
        targetDb.sign();
        System.out.println(
          "Success sign database: " + this.targetUpdateNsfFilePath
        );
      } else {
        System.out.println(
          "Error: Failed to sign database: " + this.targetUpdateNsfFilePath
        );
      }
    } catch (Exception e) {
      System.out.println("Error: Failed to open the database.");
      e.printStackTrace();
    } finally {
      if (sourceDb != null) {
        sourceDb.recycle();
      }
      if (targetDb != null) {
        targetDb.recycle();
      }
      if(sourceDbOnServer!=null){
        
        sourceDbOnServer.recycle();
      }
    }
    // Perform your specific operations here
    // For example, copying design elements, updating documents, etc.

    // Return 0 if operations are successful, or an error code if there's a problem
    return 0;
  }
}
