package me.vihnya.vpindex.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtil {
    private final Pattern HEX_PATTERN = Pattern.compile("(&#[0-9a-fA-F]{6})");

    public String colorize(String text) {
        text = text.replace("§", "&");
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1).substring(1);
            matcher.appendReplacement(sb, "" + ChatColor.of(hex));
        }
        matcher.appendTail(sb);

        String hexColored = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', hexColored);
    }
    public String concat(String[] args, int number) {
        String text = "";
        for (int i = number; i < args.length; i++) {
            if (text.equals("")) text = args[i];
            else text += " " + args[i];
        }
        return text;
    }

}
