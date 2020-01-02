package com.folioreader.ui.base;

import android.content.Context;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.dictionary.U8Parser;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    private static int MaxPhraseSearchSize = 6;
    private static int CharactersToCheckBeforeTurningOnAnnotation = 200;

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {

        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");

        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jquery-3.1.1.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-core.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-highlighter.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-classapplier.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-serializer.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/Bridge.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangefix.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/readium-cfi.umd.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag_method_call),
                "setMediaOverlayStyleColors('#C0ED72','#C0ED72')") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        switch (config.getFont()) {
            case Constants.FONT_ANDADA:
                classes = "andada";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_RALEWAY:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        htmlContent = htmlContent.replace("<html", "<html class=\"" + classes + "\"" +
                " onclick=\"onClickHtml()\"");

        String[] htmlSplit = htmlContent.split("(?i)<body");

        AnnotationDictionaryTable dictionaryTable = new AnnotationDictionaryTable(context);

        Map<String, List<AnnotationDictionaryTable.AnnotationDefinition>> defs = null;

        if (htmlSplit.length >= 2)
        {
            String head = htmlSplit[0];
            String body = htmlSplit[1];

            boolean needsAnnotation = false;
            for (int k = 0; k < CharactersToCheckBeforeTurningOnAnnotation && k < body.length(); k++)
            {
                if (Character.UnicodeScript.of(body.codePointAt(k)) == Character.UnicodeScript.HAN)
                {
                    needsAnnotation = true;
                    break;
                }
            }

            if (needsAnnotation) {

                head = head.replaceAll("(?s)<style.*?</style>", "");

                head = head.replace("</head>",
                        "<style>body { font-size: 30px } "
                        + ".w { display: inline-block !important; margin-left: 5px; margin-right: 0px; vertical-align: top; text-align: center; }"
                                + ".d { display: block; text-indent: 0px !important; font-size: 10px; max-width: 80px; overflow-wrap: break-word !important; word-wrap: break-word !important; word-break: break-all !important; overflow: hidden; text-overflow: ellipsis; max-height: 26px; }"
            + " </style></head>");

                StringBuilder annotatedBody = new StringBuilder();

                boolean insideAngleBrackets = false;
                for (int i = 0; i < body.length(); i++) {
                    int cur = body.codePointAt(i);

                    if (cur == (int) '<') {
                        insideAngleBrackets = true;
                        annotatedBody.appendCodePoint(cur);
                        continue;
                    } else if (cur == (int) '>') {
                        insideAngleBrackets = false;
                        annotatedBody.appendCodePoint(cur);
                        continue;
                    } else if (Character.UnicodeScript.of(cur) == Character.UnicodeScript.HAN) {
                        if (insideAngleBrackets) {
                            annotatedBody.appendCodePoint(cur);
                        } else {
                            if (defs == null) {
                                if (!dictionaryTable.hasWords()) {
                                    U8Parser.Init(context);
                                }

                                defs = dictionaryTable.GetAllWords();
                            }

                            boolean found = false;
                            for (int j = MaxPhraseSearchSize; j >= 1; j--) {
                                String sub = body.substring(i, i + j);
                                if (defs.containsKey(sub)) {
                                    AnnotationDictionaryTable.AnnotationDefinition mainDef = defs.get(sub).get(0);

                                    if (mainDef.Learned == 0) {
                                        annotatedBody.append("<div class='w " + mainDef.WordId + "' onclick='onClickWord(\"" + sub + "\")'>" + sub
                                                + "<div class='d'>[" + mainDef.Romanization + "] " + mainDef.Definition.replace("/", "/ ") + "</div></div>");
                                    } else {
                                        annotatedBody.append(sub);
                                    }

                                    found = true;

                                    // -1 because the loop already does ++
                                    i += (j - 1);
                                    break;
                                }
                            }

                            if (!found)
                            {
                                annotatedBody.appendCodePoint(cur);
                            }
                        }
                    } else {
                        annotatedBody.appendCodePoint(cur);
                    }

                }

                return head + "<body" + annotatedBody.toString();
            }
        }

        return htmlContent;
    }
}
