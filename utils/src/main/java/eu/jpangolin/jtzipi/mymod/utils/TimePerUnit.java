package eu.jpangolin.jtzipi.mymod.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public record TimePerUnit(long time, TimeUnit timeUnit, Range<Long> range ) {

    public TimePerUnit( long time, TimeUnit timeUnit, Range<Long> range) {
        this.time = ModUtils.clamp(time, range.min(), range.max());
        this.timeUnit = null == timeUnit ? TimeUnit.SECONDS : timeUnit;
        this.range = range;
    }
    /**
     * Convert time  and time unit to a java.time.Duration.
     * @return Duration of time and unit
     */
    public Duration toDuration() {
        return Duration.of(time, timeUnit.toChronoUnit());
    }
}
