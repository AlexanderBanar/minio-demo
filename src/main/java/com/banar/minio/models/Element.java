package com.banar.minio.models;

import java.util.Objects;

public class Element {
    private String name;
    private String id;
    private boolean isDir;

    public Element(String name, String id, boolean isDir) {
        this.name = name;
        this.id = id;
        this.isDir = isDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return isDir == element.isDir && name.equals(element.name) && id.equals(element.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, isDir);
    }
}
