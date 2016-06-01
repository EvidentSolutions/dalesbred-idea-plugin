/*
 * Copyright (c) 2016 Evident Solutions Oy
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

import org.dalesbred.Database;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLXML;
import java.time.*;
import java.util.TimeZone;

public class DefaultInstantiable {

    Database db;

    public void defaultConversions() {
        db.findAll(Short.class, "select x from foo");
        db.findAll(Integer.class, "select x from foo");
        db.findAll(Long.class, "select x from foo");
        db.findAll(Float.class, "select x from foo");
        db.findAll(Double.class, "select x from foo");
        db.findAll(BigInteger.class, "select x from foo");
        db.findAll(BigDecimal.class, "select x from foo");

        db.findAll(InputStream.class, "select x from foo");
        db.findAll(Reader.class, "select x from foo");
        db.findAll(byte[].class, "select x from foo");
        db.findAll(Document.class, "select x from foo");

        db.findAll(URL.class, "select x from foo");
        db.findAll(URI.class, "select x from foo");
        db.findAll(TimeZone.class, "select x from foo");
        db.findAll(Clob.class, "select x from foo");
        db.findAll(Blob.class, "select x from foo");
        db.findAll(SQLXML.class, "select x from foo");

        db.findAll(Instant.class, "select x from foo");
        db.findAll(LocalDateTime.class, "select x from foo");
        db.findAll(LocalTime.class, "select x from foo");
        db.findAll(ZoneId.class, "select x from foo");
        db.findAll(LocalDate.class, "select x from foo");
    }
}
