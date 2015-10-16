/*
 * Copyright (c) 2015 Evident Solutions Oy
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
import org.dalesbred.annotation.DalesbredInstantiator;

import java.lang.String;

public class InstantiatorAnnotation {

    Database db;

    public void multipleInstantiators() {
        db.findUnique(<warning>ClassWithMultipleInstantiators.class</warning>, "select foo from bar");
    }

    public void matchingInstantiator() {
        db.findUnique(SingleArgumentInstantiator.class, "select foo from bar");
    }

    public void nonMatchingInstantiator() {
        db.findUnique(<warning>SingleArgumentInstantiator.class</warning>, "select foo, bar from bar");
    }
}

class ClassWithMultipleInstantiators {

    @DalesbredInstantiator
    public ClassWithMultipleInstantiators(String s) { }

    @DalesbredInstantiator
    public ClassWithMultipleInstantiators(int i) { }
}

class SingleArgumentInstantiator {

    @DalesbredInstantiator
    public SingleArgumentInstantiator(String s) { }

    public SingleArgumentInstantiator(String s, String s2) { }
}
