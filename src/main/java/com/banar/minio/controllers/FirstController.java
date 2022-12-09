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

        //tb checked if correct full name
        String fileFullName = openedFolder + "/" + multipartFile.getOriginalFilename();


        minioUtils.uploadFile(
                fileFullName,
                multipartFile.getContentType(),
                multipartFile.getInputStream());
        return "message";
    }

    @GetMapping()
    public String mainPage(Model model) {
        List<Element> elements = minioUtils.listElements();
        List<Element> folders = new ArrayList<>();
        folders.add(new Element("ROOT", "", true));
        model.addAttribute("folders", folders);
        model.addAttribute("directories", minioUtils.sortElements(elements, Element::isDir));
        model.addAttribute("files", minioUtils.sortElements(elements, x -> !x.isDir()));
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
        List<Element> elements = minioUtils.listElements(requestedFolder);
        List<Element> folders = minioUtils.extractOpenFolders(requestedFolder);
        Element openedDir = folders.get(folders.size() - 1);

        // tb checked if slash is between name and id(path to folder)
        String openedFolder = openedDir.getId().concat(openedDir.getId());

        model.addAttribute("openedFolder", openedFolder);
        model.addAttribute("folders", folders);
        model.addAttribute("directories", minioUtils.sortElements(elements, Element::isDir));
        model.addAttribute("files", minioUtils.sortElements(elements, x -> !x.isDir()));
        model.addAttribute("currentPath", requestedFolder);

        return "mainMinio";
    }

    @PostMapping("/uploadRequest")
    public String uploadRequest(@RequestParam("openedFolder") String openedFolder,
                              Model model) {
        model.addAttribute("openedFolder", openedFolder);
        return "uploadPage";
    }




}