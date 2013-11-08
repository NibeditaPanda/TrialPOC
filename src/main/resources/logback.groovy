import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import com.tesco.core.Configuration

appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p [%c] - <%m>%n"
    }
}

appender("price_service", RollingFileAppender) {
    file = "logs/price_service.log"
    append = true
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "price_service.%i.log.zip"
        minIndex = 1
        maxIndex = 3
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = "20MB"
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p [%c] - <%m>%n"
    }
}

def logLevel = Configuration.get().getString("log.level")
root(Level.toLevel(logLevel), ["stdout", "price_service"])
