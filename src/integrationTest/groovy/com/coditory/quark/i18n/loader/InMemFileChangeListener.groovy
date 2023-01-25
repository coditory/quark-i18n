package com.coditory.quark.i18n.loader

import org.awaitility.core.ConditionTimeoutException

import java.util.function.Supplier

import static java.util.Collections.unmodifiableList
import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class InMemFileChangeListener implements FileWatcher.FileChangeListener {
    private final List<FileWatcher.FileChangedEvent> events = new ArrayList<>()

    @Override
    void onFileChange(FileWatcher.FileChangedEvent event) {
        events.add(event)
    }

    synchronized FileWatcher.FileChangedEvent getLastEvent() {
        return events.isEmpty() ? null : events.last()
    }

    synchronized List<FileWatcher.FileChangedEvent> getEvents() {
        return unmodifiableList(events)
    }

    void skipEvents(int count) {
        waitForEvents(count)
        synchronized (this) {
            events.subList(0, count).clear()
        }
    }

    void assertEvents(FileWatcher.FileChangedEvent... expected) {
        waitForEvents(expected.length)
        synchronized (this) {
            Set<FileWatcher.FileChangedEvent> actualSet = events.toSet()
            Set<FileWatcher.FileChangedEvent> expectedSet = expected.toList().toSet()
            assert actualSet == expectedSet
        }
    }

    void waitForEvents(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Expected event count > 0. Got " + count)
        }
        try {
            waitFor { events.size() >= count }
        } catch (ConditionTimeoutException ignored) {
            assert events.size() >= count:
                    "Received ${events.size()} of $count expected events.\nEvents received: " + events
        }
    }

    void waitFor(Supplier<Boolean> condition) {
        InMemFileChangeListener listener = this
        await().atMost(60, SECONDS).until {
            synchronized (listener) {
                condition.get()
            }
        }
    }
}
