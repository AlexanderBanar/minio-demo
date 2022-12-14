package com.banar.minio.controllers;

import com.banar.minio.models.Element;
import com.banar.minio.services.MinioUtils;
import io.minio.errors.*;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
public class FirstController {
    private MinioUtils minioUtils;

    public FirstController(MinioUtils minioUtils) {
        this.minioUtils = minioUtils;
    }

    @GetMapping("/up")
    public String homePage() {
        return "uploadPage";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile,
                             @RequestParam("openedFolder") String openedFolder)
            throws IOException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, XmlParserException, ServerException, InvalidKeyException {
        String fileFullName = openedFolder + "/" + multipartFile.getOriginalFilename();
        minioUtils.uploadFile(
                fileFullName,
                multipartFile.getContentType(),
                multipartFile.getInputStream());
        return "redirect:/";
    }

    @GetMapping()
    public String mainPage(Model model) {
        List<Element> rawElements = minioUtils.listElements();
        List<Element> directories = minioUtils.sortElements(rawElements, Element::isDir);
        List<Element> files = minioUtils.sortElements(rawElements, x -> !x.isDir());
        boolean isEmpty = directories.isEmpty() && files.isEmpty();
        List<Element> folders = new ArrayList<>();
        folders.add(new Element("ROOT", "", true));
        model.addAttribute("isEmpty", isEmpty);
        model.addAttribute("folders", folders);
        model.addAttribute("directories", directories);
        model.addAttribute("files", files);
        model.addAttribute("currentPath", "");
        return "mainMinio";
    }

    @PostMapping("/openFolder")
    public String openFolder(@RequestParam("currentPath") String currentPath,
                             @RequestParam("name") String name,
                             Model model) {
        if (name.equals("ROOT")) {
            return "redirect:/";
        }
        String requestedFolder = currentPath + "/" + name;
        List<Element> rawElements = minioUtils.listElements(requestedFolder);
        List<Element> directories = minioUtils.sortElements(rawElements, Element::isDir);
        List<Element> files = minioUtils.sortElements(rawElements, x -> !x.isDir());
        boolean isEmpty = directories.isEmpty() && files.isEmpty();
        List<Element> folders = minioUtils.extractOpenFolders(requestedFolder);
        Element openedDir = folders.get(folders.size() - 1);
        String openedFolder = openedDir.getId().concat(name);
        model.addAttribute("isEmpty", isEmpty);
        model.addAttribute("openedFolder", openedFolder);
        model.addAttribute("folders", folders);
        model.addAttribute("directories", directories);
        model.addAttribute("files", files);
        model.addAttribute("currentPath", requestedFolder);
        return "mainMinio";
    }

    @PostMapping("/uploadRequest")
    public String uploadRequest(@RequestParam("openedFolder") String openedFolder,
                              Model model) {
        model.addAttribute("openedFolder", openedFolder);
        return "uploadPage";
    }

    @PostMapping("/deleteRequest")
    public String deleteElementRequest(@RequestParam("fullName") String fullName,
                                @RequestParam("shortName") String shortName,
                                @RequestParam("isDir") boolean isDir,
                                Model model) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("shortName", shortName);
        model.addAttribute("isDir", isDir);
        return "deletePage";
    }

    @PostMapping("/delete")
    public String deleteElement(@RequestParam("fullName") String fullName,
                                @RequestParam("isDir") boolean isDir) {
        minioUtils.deleteElement(fullName, isDir);
        return "redirect:/";
    }

    @PostMapping("/createRequest")
    public String createFolderRequest(@RequestParam("openedFolder") String openedFolder,
                                      Model model) {
        model.addAttribute("openedFolder", openedFolder);
        return "createFolder";
    }

    @PostMapping("/create")
    public String createFolder(@RequestParam("openedFolder") String openedFolder,
                               @RequestParam("newFolderName") String newFolderName) {
        minioUtils.createFolder(openedFolder, newFolderName);
        return "redirect:/";
    }




}