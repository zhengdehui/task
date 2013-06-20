package cn.dehui.zbj2334776;

import com.memetix.mst.language.Language;

public enum LanguageTransformEnum {

    EN("英语", Language.ENGLISH), FR("法语", Language.FRENCH), GE("德语", Language.GERMAN), PT("葡萄牙语", Language.PORTUGUESE), IT(
            "意大利语", Language.ITALIAN), ES("西班牙语", Language.SPANISH), DA("丹麦语", Language.DANISH), AR("阿拉伯语",
            Language.ARABIC);

    String   title;

    Language from = Language.CHINESE_SIMPLIFIED;

    Language to;

    LanguageTransformEnum(String title, Language from, Language to) {
        this.title = title;
        this.from = from;
        this.to = to;
    }

    LanguageTransformEnum(String title, Language to) {
        this.title = title;
        this.to = to;
    }

    public String getTitle() {
        return title;
    }

    public Language getFrom() {
        return from;
    }

    public Language getTo() {
        return to;
    }

    @Override
    public String toString() {
        return title;
    }
}
