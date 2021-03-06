/*
 * The MIT License
 *
 * Copyright (c) 2017 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jsfr.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.jsfr.json.provider.JsonProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class GsonParser implements JsonParserAdapter {

    public final static GsonParser INSTANCE = new GsonParser();

    private GsonParser(){}

    @Override
    public void parse(Reader reader, SurfingContext context) {
        try {
            final JsonReader jsonReader = new JsonReader(reader);
            final JsonProvider jsonProvider = context.getConfig().getJsonProvider();
            AbstractPrimitiveHolder stringHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() throws IOException {
                    return jsonProvider.primitive(jsonReader.nextString());
                }

                @Override
                public void doSkipValue() throws IOException {
                    jsonReader.skipValue();
                }
            };
            AbstractPrimitiveHolder numberHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() throws IOException {
                    return jsonProvider.primitive(jsonReader.nextDouble());
                }

                @Override
                public void doSkipValue() throws IOException {
                    jsonReader.skipValue();
                }
            };
            AbstractPrimitiveHolder booleanHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() throws IOException {
                    return jsonProvider.primitive(jsonReader.nextBoolean());
                }

                @Override
                public void doSkipValue() throws IOException {
                    jsonReader.skipValue();
                }
            };
            AbstractPrimitiveHolder nullHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() throws IOException {
                    jsonReader.nextNull();
                    return jsonProvider.primitiveNull();
                }

                @Override
                public void doSkipValue() throws IOException {
                    jsonReader.skipValue();
                }
            };
            context.startJSON();
            while (!context.isStopped()) {
                JsonToken token = jsonReader.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                        jsonReader.beginArray();
                        context.startArray();
                        break;
                    case END_ARRAY:
                        jsonReader.endArray();
                        context.endArray();
                        break;
                    case BEGIN_OBJECT:
                        jsonReader.beginObject();
                        context.startObject();
                        break;
                    case END_OBJECT:
                        jsonReader.endObject();
                        context.endObject();
                        break;
                    case NAME:
                        String name = jsonReader.nextName();
                        context.startObjectEntry(name);
                        break;
                    case STRING:
                        stringHolder.init();
                        context.primitive(stringHolder);
                        stringHolder.skipValue();
                        break;
                    case NUMBER:
                        numberHolder.init();
                        context.primitive(numberHolder);
                        numberHolder.skipValue();
                        break;
                    case BOOLEAN:
                        booleanHolder.init();
                        context.primitive(booleanHolder);
                        booleanHolder.skipValue();
                        break;
                    case NULL:
                        nullHolder.init();
                        context.primitive(nullHolder);
                        nullHolder.skipValue();
                        break;
                    case END_DOCUMENT:
                        context.endJSON();
                        return;
                }
            }
        } catch (Exception e) {
            context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
        }
    }

    @Override
    public void parse(String json, SurfingContext context) {
        parse(new StringReader(json), context);
    }

}
