package com.edusoho.kuozhi.v3.factory;

import com.edusoho.kuozhi.v3.factory.json.Parser;
import com.edusoho.kuozhi.v3.factory.json.ParserImpl;

/**
 * Created by su on 2015/12/29.
 */
public class UtilFactory extends AbstractFactrory {

    private Parser mJsonParser;

    public UtilFactory() {
        this.mJsonParser = new ParserImpl();
    }

    public Parser getJsonParser() {
        return mJsonParser;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }
}
