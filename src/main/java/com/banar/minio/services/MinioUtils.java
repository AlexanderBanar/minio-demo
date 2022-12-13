package com.banar.minio.services;

import com.banar.minio.models.Element;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

@Service
public class MinioUtils {
    private final String BUCKET = "bucket";
    private final MinioClient minioClient;

    public MinioUtils() {
        minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000/")
                .credentials("user", "password")
                .build();
    }

    public void uploadFile(String fileName, String fileContentType, InputStream inputStream)
            throws IOException, ServerException, InsufficientDataException, InternalException,
            InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException,
            XmlParserException, ErrorResponseException {


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .contentType(fileContentType)
                        .stream(inputStream, inputStream.available(), -1)
                        .build()
        );

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET)
                        .object("banar/кон.txt")
                        .build());

//        Iterable<Result<Item>> results = minioClient.listObjects(
//                ListObjectsArgs.builder()
//                        .bucket("banar")
//                        .build());
//
//        for (Result<Item> result : results) {
//            System.out.println(result.get().objectName());
//        }

        // чтобы удалить папку - удаляем ее содержимое - далее пустая папка удалится сама автоматически

//        перечисление содержимого папки folder в бакете banar
//        Iterable<Result<Item>> results = minioClient.listObjects(
//                ListObjectsArgs.builder()
//                        .bucket("banar")
//                        .prefix("folder/")
//                        .build());

    }

    public List<Element> listElements() {
        Iterable<Result<Item>> iterable = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET)
                        .build());
        return convert(iterable);
    }

    public List<Element> listElements(String folder) {
        String fullPath = getFullPath(folder);
        Iterable<Result<Item>> iterable = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET)
                        .prefix(fullPath)
                        .build());
        return convert(iterable);
    }

    private String getFullPath(String folder) {
        if (folder.contains("ROOT/")) {
            folder = folder.replace("ROOT/", "");
        }
        while(folder.contains("//")) {
            folder = folder.replace("//", "/");
        }
        return folder + "/";
    }

    private List<Element> convert(Iterable<Result<Item>> iterable) {
        List<Element> list = new ArrayList<>();
        try {
            for (Result<Item> result : iterable) {
                String id = result.get().objectName();
                String[] split = id.split("/");
                String name = split[split.length - 1];
                list.add(new Element(
                        name,
                        id,
                        result.get().isDir()
                ));
            }
        } catch (ErrorResponseException | InvalidKeyException | NoSuchAlgorithmException
                | InvalidResponseException | ServerException | InsufficientDataException
                | XmlParserException | InternalException | IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Element> sortElements(List<Element> rawItems, Predicate<Element> predicate) {
        List<Element> folders = new LinkedList<>();
        for (Element element : rawItems) {
            if (predicate.test(element)) {
                folders.add(element);
            }
        }
        folders.sort(Comparator.comparing(Element::getName));
        return folders;
    }


    public List<Element> extractOpenFolders(String currentPath) {
        String[] split = currentPath.split("/");
        Map<Integer, String> map = getFoldersMap(split);
        return getOpenFolders(map);
    }

    private List<Element> getOpenFolders(Map<Integer, String> map) {
        StringBuilder sb = new StringBuilder();
        List<Element> result = new ArrayList<>();
        result.add(new Element("ROOT", "", true));
        String pathToFolder;
        for (int i = 0; i < map.size(); i++) {
            if (i == 0) {
                pathToFolder = "ROOT";
            } else {
                sb.append(map.get(i - 1));
                sb.append("/");
                pathToFolder = sb.toString();
            }
            result.add(new Element(map.get(i), pathToFolder, true));
        }
        return result;
    }

    private Map<Integer, String> getFoldersMap(String[] split) {
        Map<Integer, String> map = new HashMap<>();
        int count = 0;
        for (String folderName : split) {
            if (folderName.equals("ROOT")
                    || folderName.isEmpty()
                    || folderName.isBlank()) {
                continue;
            }
            map.put(count++, folderName);
        }
        return map;
    }

    public void deleteElement(String fullName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fullName)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException
                | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
        }
    }
}
