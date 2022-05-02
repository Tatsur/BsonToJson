package com.ttsr;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttsr.model.ServerOutput;
import com.ttsr.model.User;
import lombok.Getter;
import org.bson.*;
import org.eclipse.collections.impl.block.factory.HashingStrategies;
import org.eclipse.collections.impl.utility.ListIterate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BsonDumper {

    private static final List<User> users = new ArrayList<>();
    private static final List<ServerOutput> outputs = new ArrayList<>();

    public List<String> getJsonDump(String filename) {
        File file = new File(filename);

        BSONDecoder decoder = new BasicBSONDecoder();
        List<String> stringList = new ArrayList<>();
        int count = 0;

        try (InputStream inputStream = new FileInputStream(file)){
            while (inputStream.available() > 0) {
                BSONObject obj = decoder.readObject(inputStream);
                if(obj == null){
                    break;
                }
                Document bson =  new Document(obj.toMap());
                String json = bson.toJson();
                stringList.add(bson.toJson());
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                if(file.getName().equals("users.bson")){
                    users.add(objectMapper.readValue(json, User.class));
                }
                if (file.getName().equals("servers_output.bson")){
                    outputs.add(objectMapper.readValue(json, ServerOutput.class));
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.printf("file:%s, %s objects read%n", file.getName(), count);
        return stringList;
    }

    protected String findLastConnectDate(){
        List<User> uniqueUsers = ListIterate.distinct(users, HashingStrategies.fromFunction(User::getOid));
        List<User> uniqueNamedUsers = uniqueUsers.stream()
                .filter(s -> !s.getName().isEmpty() && !s.getName().equals("undefined"))
                .collect(Collectors.toList());
        Map<String, String> dateUserMap = new HashMap<>();
        outputs.stream()
                .map(ServerOutput::getOutput)
                .filter(output -> output.contains("User connected user_id"))
                .forEach(m -> outputDataRegex(dateUserMap, m));
        uniqueNamedUsers.forEach(u -> u.setLastConnectedDateRaw(dateUserMap.getOrDefault(u.getOid(), null)));

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

        StringBuilder prettyUsers = new StringBuilder();

        uniqueNamedUsers.stream()
                .filter(u -> u.getLastConnectedDateRaw() != null)
                .peek(r -> {
                    try {
                        r.setLastConnectedDate(format.parse(r.getLastConnectedDateRaw()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                })
                .sorted(Comparator.comparing(User::getLastConnectedDate))
                .forEach(u -> prettyUsers
                                    .append(u.getLastConnectedDateRaw()).append(", ")
                                    .append(u.getName())
                                    .append("\n")

                );
        System.out.println(prettyUsers);
        return prettyUsers.toString();
    }

    private void outputDataRegex(Map<String, String> dateUserMap, String m) {
        String matchName = "user_id=";
        int from = m.lastIndexOf(matchName);
        int idLength = 24;
        int startIndex = from + matchName.length();
        String key = m.substring(startIndex, startIndex + idLength);
        String regex = "] ([A-Za-z]{3} [A-Za-z]{3} [0-3]?[0-9] [0-2]?[0-9]:[0-5][0-9]:[0-5][0-9] [0-9]{4}) User";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(m);
        if(matcher.find()){
            dateUserMap.put(key, matcher.group(1));
        }
    }

    protected void collectToFile(Path path, String saveRoot)  {
        BsonDumper bsonDump = new BsonDumper();
        List<String> stringList = bsonDump.getJsonDump(path.toString());
        try {
            File fileOld = new File(saveRoot + path.getFileName().toString().replace("bson","json"));
            fileOld.delete();
            File file = new File(saveRoot + path.getFileName().toString().replace("bson","json"));
            file.createNewFile();
            saveDataToFile(stringList, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile(List<String> stringList, File file) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);
        stringList = stringList.stream().map(s -> s.concat(",\n")).collect(Collectors.toList());
        stringList.forEach(printWriter::write);
        printWriter.close();
    }

    protected List<Path> filesWalk(String bsonFilePath, String fileType){
        try (Stream<Path> paths = Files.walk(Paths.get(bsonFilePath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(fileType))
                    .collect(Collectors.toList());
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void saveToJson(String path, String targetPath, String searchFormat) throws IOException {
        List<Path> filesPath = filesWalk(path, searchFormat);
        Files.createDirectories(Paths.get(targetPath));
        Objects.requireNonNull(filesPath)
                .forEach(f -> collectToFile(f, targetPath));
    }

    protected void findAndSaveUsersLastConnectedDates(String userLastConnectedDateFilename, String targetPath) {
        String usersLastConnectedDates = findLastConnectDate();
        String path = targetPath + userLastConnectedDateFilename;
        try {
            File fileOld = new File(path);
            fileOld.delete();
            File file = new File(path);
            file.createNewFile();
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(usersLastConnectedDates);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
