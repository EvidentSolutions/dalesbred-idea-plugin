/*
 * Copyright (c) 2013 Evident Solutions Oy
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

import fi.evident.dalesbred.Database;

public class SimpleCases {

    Database db;

    public void instantiationByFields() {
        db.findAll(MyClassWithImplicitDefaultConstructor.class, "select bar from foo");
        db.findAll(MyClassWithExplicitDefaultConstructor.class, "select bar from foo");
    }

    public void instantiotionBySetters() {
        db.findAll(MyClassWithSetter.class, "select bar from foo");
        db.findAll(<warning>MyClassWithSetter.class</warning>, "select baz from foo");
    }

    public void instantiotionByConstructor() {
        db.findAll(MyClassWithTwoArgumentConstructor.class, "select x, y from foo");
        db.findAll(<warning>MyClassWithTwoArgumentConstructor.class</warning>, "select x from foo");
        db.findAll(<warning>MyClassWithTwoArgumentConstructor.class</warning>, "select x, y, z from foo");
    }

    public void instantiotionByConstructorAndFields() {
        db.findAll(MyClassWithConstructorFieldAndSetter.class, "select foo, bar, baz from foobar");
    }

    public class NonStaticInnerClass { }
}

class MyClassWithImplicitDefaultConstructor {
    public String bar;
}

class MyClassWithExplicitDefaultConstructor {
    public String bar;

    public MyClassWithExplicitDefaultConstructor() { }
}

class MyClassWithTwoArgumentConstructor {
    public MyClassWithTwoArgumentConstructor(int x, int y) { }
}

class MyClassWithSetter {
    public void setBar(String bar) { }
}

class MyClassWithConstructorFieldAndSetter {
    public String bar;
    public void setBaz(String baz) { }

    public MyClassWithConstructorFieldAndSetter(String foo) { }
}
