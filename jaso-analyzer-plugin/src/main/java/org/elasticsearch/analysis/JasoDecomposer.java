package org.elasticsearch.analysis;

public class JasoDecomposer {
    /**
     * 한글 유니코드
     * 시작값 : 0xAC00 가
     * 끝값   : 0xD79F 힣
     */
    public static final char START_KOREA_UNICODE = 0xAC00;
    public static final char END_KOREA_UNICODE = 0xD79F;



    /**
     * 종성을 위한 빈값 유니코드
     */
    public static final char UNICODE_JONG_SUNG_EMPTY = 0x0000;

    /**
     * 초성 (19자)
     * ㄱ ㄲ ㄴ ㄷ ㄸ ㄹ ㅁ ㅂ ㅃ ㅅ
     * ㅆ ㅇ ㅈ ㅉ ㅊ ㅋ ㅌ ㅍ ㅎ
     *
     */
    public static final char[] UNICODE_CHO_SUNG = {
            0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
            0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
    };


    /**
     * 중성 (21자)
     * ㅏ ㅐ ㅑ ㅒ ㅓ ㅔ ㅕ ㅖ ㅗ ㅘ
     * ㅙ ㅚ ㅛ ㅜ ㅝ ㅞ ㅟ ㅠ ㅡ ㅢ
     * ㅣ
     *
     */
    public static final char[] UNICODE_JUNG_SUNG = {
            0x314F, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158,
            0x3159, 0x315A, 0x315B, 0x315C, 0x315D, 0x315E, 0x315F, 0x3160, 0x3161, 0x3162,
            0x3163
    };


    /**
     * 종성 (28자)
     * 기본 27자와 "빈값"을 표현하는 1자를 합쳐서 총 28자
     *  빈값 ㄱ ㄲ ㄳ ㄴ ㄵ ㄶ ㄷ ㄹ ㄺ
     *  ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ ㅁ ㅂ ㅄ ㅅ
     *  ㅆ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ
     *
     */
    public static final char[] UNICODE_JONG_SUNG = {
            0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313A,
            0x313B, 0x313C, 0x313D, 0x313E, 0x313F, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145,
            0x3146, 0x3147, 0x3148, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
    };

    public String decompose(String token) {
        if (token.length() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        char[] arrCh = token.toCharArray();
        for(char ch : arrCh) {
            int codePoint = ch;
            if (codePoint >= START_KOREA_UNICODE && codePoint <= END_KOREA_UNICODE) {
                // 한글인 경우
                int startValue = codePoint - START_KOREA_UNICODE;
                int jong = startValue % 28;
                int jung = ((startValue - jong) / 28) % 21;
                int cho = (((startValue - jong) / 28) - jung) / 21;
                builder.append(UNICODE_CHO_SUNG[cho]);
                builder.append(UNICODE_JUNG_SUNG[jung]);
                if (UNICODE_JONG_SUNG[jong] != UNICODE_JONG_SUNG_EMPTY) {
                    builder.append(UNICODE_JONG_SUNG[jong]);
                }
            } else {
                // 그외 (e.g. 영어, 숫자, 특수문자)
                builder.append(ch);
            }
        }

        return builder.toString();
    }
}
