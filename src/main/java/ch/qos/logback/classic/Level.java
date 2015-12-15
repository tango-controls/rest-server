package ch.qos.logback.classic;

/**
 * Work around JTangoServer compilation failure when no logback classes are provided
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
public enum Level {
    OFF,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}
