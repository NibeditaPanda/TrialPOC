import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.DEBUG

appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p [%c] - <%m>%n"
    }
}

appender("test", RollingFileAppender) {
    file = "logs/test.log"
    append = true
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "logs/test.%i.log.zip"
        minIndex = 1
        maxIndex = 3
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = "5MB"
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p [%c] - <%m>%n"
    }
}

root(DEBUG, ["stdout", "test"])
