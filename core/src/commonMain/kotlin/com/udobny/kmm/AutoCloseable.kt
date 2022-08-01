// from java.lang.AutoCloseable for use in Kotlin/Common
package com.udobny.kmm

/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * An object that may hold resources (such as file or socket handles)
 * until it is closed. The [close] method of an `AutoCloseable`
 * object is called automatically when exiting a
 * `try`-with-resources block for which the object has been declared in
 * the resource specification header. This construction ensures prompt
 * release, avoiding resource exhaustion exceptions and errors that
 * may otherwise occur.
 *
 * API Note:
 * It is possible, and in fact common, for a base class to
 * implement AutoCloseable even though not all of its subclasses or
 * instances will hold releasable resources.  For code that must operate
 * in complete generality, or when it is known that the `AutoCloseable`
 * instance requires resource release, it is recommended to use
 * `try`-with-resources constructions. However, when using facilities such as
 * `Stream` that support both I/O-based and
 * non-I/O-based forms, `try`-with-resources blocks are in
 * general unnecessary when using non-I/O-based forms.
 */
public expect interface AutoCloseable {

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     *
     * While this interface method is declared to throw
     * `Exception`, implementers are _strongly_ encouraged to
     * declare concrete implementations of the `close` method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally _mark_ the
     * resource as closed, prior to throwing the exception. The
     * `close` method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * _Implementers of this interface are also strongly advised
     * to not have the `close` method throw
     * [InterruptedException]._
     *
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an
     * `InterruptedException` is {@linkplain Throwable#addSuppressed
     * suppressed}.
     *
     * More generally, if it would cause problems for an
     * exception to be suppressed, the `AutoCloseable.close`
     * method should not throw it.
     *
     * Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this `close` method
     * is _not_ required to be idempotent.  In other words,
     * calling this `close` method more than once may have some
     * visible side effect, unlike `Closeable.close` which is
     * required to have no effect if called more than once.
     *
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    public fun close()
}
