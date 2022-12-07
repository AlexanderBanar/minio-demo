package com.banar.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinioDemoApplication {

	public static void main(String[] args) {

		String s = "/folder/folder2//folder3///";
		String[] split = s.split("/");
		for (int i = 0; i < split.length; i++) {
			System.out.println(split[i]);
			System.out.println("-----");
		}


		SpringApplication.run(MinioDemoApplication.class, args);
	}

}
