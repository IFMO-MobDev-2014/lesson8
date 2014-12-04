package com.example.home.superwheather;

/**
 * Created by Home on 04.12.2014.
 */
public final class Transliterator {

    private static String transliterationTable(char c) {
        switch (c) {
            case 'а': return "a";
            case 'б': return "b";
            case 'в': return "v";
            case 'г': return "g";
            case 'д': return "d";
            case 'е': return "e";
            case 'ё': return "e";
            case 'ж': return "zh";
            case 'з': return "z";
            case 'и': return "i";
            case 'й': return "i";
            case 'к': return "k";
            case 'л': return "l";
            case 'м': return "m";
            case 'н': return "n";
            case 'о': return "o";
            case 'п': return "p";
            case 'р': return "r";
            case 'с': return "s";
            case 'т': return "t";
            case 'у': return "u";
            case 'ф': return "f";
            case 'х': return "h";
            case 'ц': return "c";
            case 'ч': return "ch";
            case 'ш': return "sh";
            case 'щ': return "sh";
            case 'ъ': return "";
            case 'ы': return "i";
            case 'ь': return "'";
            case 'э': return "e";
            case 'ю': return "yu";
            case 'я': return "ya";
            case 'А': return "A";
            case 'Б': return "B";
            case 'В': return "V";
            case 'Г': return "G";
            case 'Д': return "D";
            case 'Е': return "E";
            case 'Ё': return "E";
            case 'Ж': return "Zh";
            case 'З': return "Z";
            case 'И': return "I";
            case 'Й': return "I";
            case 'К': return "K";
            case 'Л': return "L";
            case 'М': return "M";
            case 'Н': return "N";
            case 'О': return "O";
            case 'П': return "P";
            case 'Р': return "R";
            case 'С': return "S";
            case 'Т': return "T";
            case 'У': return "U";
            case 'Ф': return "F";
            case 'Х': return "H";
            case 'Ц': return "C";
            case 'Ч': return "Ch";
            case 'Ш': return "Sh";
            case 'Щ': return "Sh";
            case 'Ъ': return "";
            case 'Ы': return "I";
            case 'Ь': return "'";
            case 'Э': return "E";
            case 'Ю': return "Yu";
            case 'Я': return "Ya";
            default: return "" + c;
        }
    }

    public static String transliterate(String source) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            stringBuilder.append(transliterationTable(source.charAt(i)));
        }
        return stringBuilder.toString();
    }

}
