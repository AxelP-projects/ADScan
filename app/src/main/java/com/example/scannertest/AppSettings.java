package com.example.scannertest;

import java.io.File;

public class AppSettings {

    //folder at the root of the device, in which all the image destination folders will be created
    public final static String APP_ROOT_FOLDER = "MonScanner";

    //Default image destination folder
    public final static String APP_DEFAULT_FOLDER_NAME = "Default";
    public final static String APP_DEFAULT_FOLDER = APP_DEFAULT_FOLDER_NAME + File.separator;

    //Default photo file extension
    public final static String APP_DEFAULT_PHOTO_EXTENSION = ".pdf";

    public final static String DATE_FORMAT = "ddMMyyHHmm";

    //Char used to separate words inside strings, for instance in the file names
    public final static String DEFAULT_SPACE_CHAR = "_";

    public static final String CANT_READ_DIR = " (inaccessible)";

    public static final String PNG_ICON_NAME = "pdf_default.png";
    public static final String FOLDER_ICON_NAME = "folder_default.png";

    //Server address for the php script to save files
    //public static final String SERVER_ADDRESS = "https://controle-levage.fr/Temp_upload_test/FileManager.php";
    //public static final String SERVER_ADDRESS = "http://192.168.1.11/Temp_upload_test/FileManager.php";
    public static final String SERVER_ADDRESS = "http://192.168.1.11/GitGestionAdministration/gestionAdministration/public/";

    public static final String FILE_MANAGER_API = "FileManager";
    public static final String CONNECT_API = "ConnectAPI";
}
