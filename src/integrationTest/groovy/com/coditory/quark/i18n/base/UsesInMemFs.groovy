package com.coditory.quark.i18n.base

import com.coditory.quark.i18n.I18nMessagePack
import com.coditory.quark.i18n.I18nMessagePackFactory
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
trait UsesInMemFs {
    private FileSystem inMemFs = Jimfs.newFileSystem(Configuration.unix())

    FileSystem getInMemFs() {
        return inMemFs
    }

    void writeInMemFsFile(String fileName, String content) {
        setupInMemFs()
        Path path = inMemFs.getPath(fileName)
        if (!Files.exists(path.parent)) {
            Files.createDirectories(path.parent)
        }
        String trimmed = content.stripIndent().trim()
        Files.writeString(path, trimmed)
        println "Wrote in-mem file: " + path.toAbsolutePath()
    }

    void deleteInMemFile(String fileName) {
        Path path = inMemFs.getPath(fileName)
        Files.delete(path)
        println "Deleted in-mem file: " + path.toAbsolutePath()
    }

    void deleteInMemDirRecursively(String dirName) {
        Path path = inMemFs.getPath(dirName)
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
        println "Deleted in-mem dir: " + path.toAbsolutePath()
    }

    void createInMemDir(String dirName) {
        Path path = inMemFs.getPath(dirName)
        Files.createDirectories(path)
        println "Created in-mem dir: " + path.toAbsolutePath()
    }

    I18nMessagePack scanInMemFs(String firstPattern, String... others) {
        setupInMemFs()
        return I18nMessagePackFactory.scanFileSystem(inMemFs, firstPattern, others)
    }

    Path inMemAbsolutePath(String path) {
        return inMemPath(path).toAbsolutePath()
    }

    Path inMemPath(String path) {
        return inMemFs.getPath(path)
    }

    private void setupInMemFs() {
        if (inMemFs == null) {
            inMemFs = Jimfs.newFileSystem(Configuration.unix())
        }
    }
}