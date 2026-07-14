package me.vihnya.vpindex.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

@UtilityClass
public class TimeUtil {
    private final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,5}(?=y))?(\\d{1,5}(?=w))?(\\d{1,5}(?=d))?(\\d{1,5}(?=h))?(\\d{1,5}(?=m))?(\\d{1,5}(?=s))?", Pattern.CASE_INSENSITIVE);

    public long parseTime(String time) throws IllegalArgumentException {
        if (time.startsWith("e")) {
            return -1L;
        } else {
            boolean found = false;
            long[] periods = new long[6];
            Matcher matcher = TIME_PATTERN.matcher(time);

            while(true) {
                while(matcher.find()) {
                    for(int i = 0; i < periods.length; ++i) {
                        String period = matcher.group(i + 1);
                        if (period != null && !period.isEmpty()) {
                            periods[i] = Integer.parseInt(period);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    throw new IllegalArgumentException("Can't parse time");
                }

                long seconds = periods[5];
                seconds += periods[4] * 60L;
                seconds += periods[3] * 3600L;
                seconds += periods[2] * 86400L;
                seconds += periods[1] * 604800L;
                seconds += periods[0] * 31104000L;
                return TimeUnit.SECONDS.toMillis(seconds);
            }
        }
    }
    public String getTime(long sec) {
        if (sec < 1L) {
            return "1 секунда";
        } else {
            long m = sec / 60L;
            sec %= 60L;
            long h = m / 60L;
            m %= 60L;
            long d = h / 24L;
            h %= 24L;
            long y = d / 365L;
            d %= 365L;
            String time = "";
            if (y > 0L) {
                time = time + y + " " + formatTime(y, "год", "года", "лет");
                if (d > 0L || h > 0L || m > 0L || sec > 0L) {
                    time = time + " ";
                }
            }

            if (d > 0L) {
                time = time + d + " " + formatTime(d, "день", "дня", "дней");
                if (h > 0L || m > 0L || sec > 0L) {
                    time = time + " ";
                }
            }

            if (h > 0L) {
                time = time + h + " " + formatTime(h, "час", "часа", "часов");
                if (m > 0L || sec > 0L) {
                    time = time + " ";
                }
            }

            if (m > 0L) {
                time = time + m + " " + formatTime(m, "минута", "минуты", "минут");
                if (sec > 0L) {
                    time = time + " ";
                }
            }

            if (sec > 0L) {
                time = time + sec + " " + formatTime(sec, "секунда", "секунды", "секунд");
            }

            return time;
        }
    }

    public String formatTime(long number, String caseOne, String caseTwo, String caseFive) {
        String str;
        number = abs(number);

        if (number % 10 == 1 && number % 100 != 11) {
            str = caseOne;
        } else if (number % 10 >= 2 && number % 10 <= 4 && (number % 100 < 10 || number % 100 >= 20)) {
            str = caseTwo;
        } else {
            str = caseFive;
        }

        return str;
    }
}
