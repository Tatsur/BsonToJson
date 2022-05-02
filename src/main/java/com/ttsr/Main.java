package com.ttsr;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
//        test path
//        String path = "D:/Work/Practice/MyProjects/BsonToJson/src/main/resources/dump";
        String rawPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String pathWithFileName  = rawPath.replace("/", File.separator).substring(1);
        File file = new File(pathWithFileName);
        String path = file.getParent();
        String userLastConnectedDateFilename  = "users_last_connected_date.txt";
        String targetPath = path + File.separator + "bsonToJson" + File.separator;
        String searchFormat = ".bson";
        System.out.println(targetPath);

        BsonDumper bsonDump = new BsonDumper();
        bsonDump.saveToJson(path, targetPath, searchFormat);
        bsonDump.findAndSaveUsersLastConnectedDates(userLastConnectedDateFilename, targetPath);
    }
}
