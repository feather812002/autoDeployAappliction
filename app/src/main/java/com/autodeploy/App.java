package com.autodeploy;

import lotus.domino.*;
import com.autodeploy.utils.DominoOperations;

public class App extends NotesThread {
    private String adminPassword;
    private String sourceNtfFileName;
    private String targetUpdateNsfFilePath;
    private String targetUpdateServerName;
    private static Session session = null;
    private int errorCode = 0;

    public App(String adminPassword, String sourceNtfFileName, String targetUpdateNsfFilePath,String targetServerName) {
        this.adminPassword = adminPassword;
        this.sourceNtfFileName = sourceNtfFileName;
        this.targetUpdateNsfFilePath = targetUpdateNsfFilePath;
        this.targetUpdateServerName = targetServerName;
    }

    @Override
    public void runNotes() {
        try {
            System.out.println("Initializing Notes thread...");
            System.out.println(Thread.currentThread().getName());

            if (adminPassword.isEmpty()) {
                session = NotesFactory.createSessionWithFullAccess();
            } else {
                session = NotesFactory.createSessionWithFullAccess(adminPassword);
            }

            System.out.println("Connected to Domino server on platform: " + session.getPlatform());

            DominoOperations operations = new DominoOperations(session, sourceNtfFileName, targetUpdateNsfFilePath,targetUpdateServerName);
            errorCode = operations.performOperations();

            System.out.println("Operations completed with error code: " + errorCode);
        } catch (NotesException e) {
            System.err.println("Notes authentication error. Please check your password and try again.");
            e.printStackTrace();
            errorCode = 2;
        } catch (Exception e) {
            System.err.println("An error occurred while initializing the Notes thread.");
            e.printStackTrace();
            errorCode = 2;
        } finally {
            try {
                if (session != null) {
                    session.recycle();
                }
            } catch (NotesException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java -jar YourJarFile.jar <adminPassword> <sourceNtfFileName> <targetUpdateNsfFilePath> <targetUpdateServerName>");
            System.exit(1);
        }

        String adminPassword_parameter = args[0];
        String sourceNtfFileName_parameter = args[1];
        String targetUpdateNsfFilePath_parameter = args[2];
        String targetUpdateServerName_parameter = args[3];

        //check sourceNtfFileName_parameter and targetUpdateNsfFilePath_parameter must different , otherwise it will cause error  
        if(sourceNtfFileName_parameter.equals(targetUpdateNsfFilePath_parameter)){
            System.out.println("Error: sourceNtfFileName and targetUpdateNsfFilePath must different , otherwise it will cause error ");
            System.exit(1);
        }


        try {
            App app = new App(adminPassword_parameter, sourceNtfFileName_parameter, targetUpdateNsfFilePath_parameter,targetUpdateServerName_parameter);
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    System.err.println("Uncaught exception in thread: " + t.getName());
                    e.printStackTrace();
                }
            });
            app.start();

            if (app.isAlive()) {
                System.out.println("Notes Thread is alive");
            } else {
                System.out.println("Notes Thread is dead");
            }

            app.join();

            System.out.println("Final error code: " + app.errorCode);
            System.exit(app.errorCode);
        } catch (Exception e) {
            System.err.println("An error occurred in the main thread: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}