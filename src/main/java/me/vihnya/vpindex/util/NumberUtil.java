package me.vihnya.vpindex.util;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.util.function.Consumer;

@UtilityClass
public class NumberUtil {

    public String round(double number, int places) {
        if (places < 0) throw new IllegalArgumentException();

        // Форматируем число до нужного количества знаков после запятой
        StringBuilder pattern = new StringBuilder("#.");
        for (int i = 0; i < places; i++) {
            pattern.append("#");
        }
        DecimalFormat df = new DecimalFormat(pattern.toString());
        return df.format(number);
    }

    public int getInt(String s, Consumer<String> consumer) {
        try {
            return Integer.parseInt(s);
        }catch (NumberFormatException e) {
            consumer.accept(s);
            return 0;
        }
    }
}
