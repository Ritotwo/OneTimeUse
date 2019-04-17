package com.app.awqsome.onetimeuse.Otp;

public class TokenCode {

    private final String code;
    private final long startTime;
    private final long untilTime;
    private TokenCode nextCode;

    public TokenCode(String code, long startTime, long untilTime) {
        this.code = code;
        this.startTime = startTime;
        this.untilTime = untilTime;
    }

    public TokenCode(String code, long startTime, long untilTime, TokenCode next) {
        this(code, startTime, untilTime);
        this.nextCode = next;
    }

    public String getCurrentCode() {
        TokenCode active = getActive(System.currentTimeMillis());
        if(active == null) return null;
        return active.code;
    }

    public TokenCode getActive(long currTime) {
        if(currTime >= startTime && currTime <= untilTime) return this;
        if(nextCode == null) return null;
        return this.nextCode.getActive(currTime);
    }
}
