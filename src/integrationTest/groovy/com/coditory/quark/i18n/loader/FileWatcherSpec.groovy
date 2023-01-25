package com.coditory.quark.i18n.loader

import com.coditory.quark.i18n.base.UsesInMemFs
import spock.lang.Specification

import static com.coditory.quark.i18n.loader.FileWatcher.FileChangeType.CREATE
import static com.coditory.quark.i18n.loader.FileWatcher.FileChangeType.DELETE
import static com.coditory.quark.i18n.loader.FileWatcher.FileChangeType.MODIFY
import static com.coditory.quark.i18n.loader.FileWatcher.FileChangedEvent

class FileWatcherSpec extends Specification implements UsesInMemFs {
    InMemFileChangeListener listener = new InMemFileChangeListener()

    def "should send event on created file when parent dir was created after watch"() {
        given:
            createInMemDir("a")
            watch("a/**")
        when:
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            writeInMemFsFile("a/bar.txt", "bar")
        then:
            listener.assertEvents(
                    event(CREATE, "a/b/c/foo.txt"),
                    event(CREATE, "a/bar.txt")
            )
    }

    def "should send event on created file when parent dir was created before watch"() {
        given:
            createInMemDir("a/b")
            watch("a/**")
        when:
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            writeInMemFsFile("a/bar.txt", "bar")
        then:
            listener.assertEvents(
                    event(CREATE, "a/b/c/foo.txt"),
                    event(CREATE, "a/bar.txt")
            )
    }

    def "should send event on deleted files when files were created before watch"() {
        given:
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            writeInMemFsFile("a/bar.txt", "bar")
            writeInMemFsFile("a/b/c/baz.txt", "baz")
            watch("a/**")
        when:
            deleteInMemFile("a/b/c/foo.txt")
            deleteInMemFile("a/bar.txt")
        then:
            listener.assertEvents(
                    event(DELETE, "a/b/c/foo.txt"),
                    event(DELETE, "a/bar.txt")
            )
    }

    def "should send event on deleted files when files were created after watch"() {
        given:
            createInMemDir("a")
            watch("a/**")
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            writeInMemFsFile("a/bar.txt", "bar")
            writeInMemFsFile("a/b/c/baz.txt", "baz")
            listener.skipEvents(3)
        when:
            deleteInMemFile("a/b/c/foo.txt")
            deleteInMemFile("a/bar.txt")
        then:
            listener.assertEvents(
                    event(DELETE, "a/b/c/foo.txt"),
                    event(DELETE, "a/bar.txt")
            )
    }

    def "should send events on deleted files when parent directory is removed"() {
        given:
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            writeInMemFsFile("a/b/bar.txt", "bar")
            writeInMemFsFile("a/baz.txt", "baz")
            watch("a/**")
        when:
            deleteInMemDirRecursively("a/b")
        then:
            listener.assertEvents(
                    event(DELETE, "a/b/c/foo.txt"),
                    event(DELETE, "a/b/bar.txt")
            )
    }

    def "should send event on file modification when file was created before watch"() {
        given:
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            watch("a/**")
        when:
            writeInMemFsFile("a/b/c/foo.txt", "foo2")
        then:
            listener.assertEvents(
                    event(MODIFY, "a/b/c/foo.txt")
            )
    }

    def "should send event on file modification when file was created after watch"() {
        given:
            createInMemDir("a")
            watch("a/**")
            writeInMemFsFile("a/b/c/foo.txt", "foo")
            listener.skipEvents(1)
        when:
            writeInMemFsFile("a/b/c/foo.txt", "foo2")
        then:
            listener.assertEvents(
                    event(MODIFY, "a/b/c/foo.txt")
            )
    }

    FileChangedEvent event(FileWatcher.FileChangeType changeType, String path) {
        return new FileChangedEvent(changeType, inMemAbsolutePath(path))
    }

    void watch(String pattern) {
        FileWatcher.builder()
                .addListener(listener)
                .fileSystem(inMemFs)
                .addPathPattern(pattern)
                .startWatchingThread()
    }
}
