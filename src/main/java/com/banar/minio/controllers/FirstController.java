package com.banar.minio.controllers;

import com.banar.minio.models.Element;
import com.banar.minio.services.MinioUtils;
import io.minio.errors.*;

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
                             Model model) throws IOException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, XmlParserException, ServerException, InvalidKeyException {
        minioUtils.uploadFile(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getInputStream());
        return "message";
    }

    @GetMapping()
    public String mainPage(Model model) {
        List<Element> elements = minioUtils.listElements();

        List<Element> folders = new LinkedList<>();
        folders.add(new Element("ROOT", "folder0", true));

        model.addAttribute("folders", folders);
        model.addAttribute("directories", minioUtils.sortElements(elements, Element::isDir));
        model.addAttribute("files", minioUtils.sortElements(elements, x -> !x.isDir()));
        return "mainMinio";
    }

    @GetMapping("/folder")
    public String openFolder(@RequestParam("id") String name, Model model) {
        if (name.equals("folder0")) {
            return "redirect:/";
        }
        System.out.println(name);

        List<Element> elements = minioUtils.listElements(name);

        List<Element> folders = new LinkedList<>();
        folders.add(new Element("ROOT", "folder0", true));

        model.addAttribute("folders", folders);
        model.addAttribute("directories", minioUtils.sortElements(elements, Element::isDir));
        model.addAttribute("files", minioUtils.sortElements(elements, x -> !x.isDir()));

        return "mainMinio";
    }

    // folders - list of entered folders (folder.name + folder.id)
    // directories - list of folders in the current folder (folder.name + folder.id)
    // files - list of files in the current folder (file.name + file.id)






}
