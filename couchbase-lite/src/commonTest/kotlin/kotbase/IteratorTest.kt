/*
 * Copyright 2025 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class IteratorTest : BaseDbTest() {

    ///// Array Iterator Tests

    @Test
    fun testConcurrentModOfMutableArrayIterator() {
        val array = MutableArray()
        (0..5).forEach { array.addValue(it) }

        var n = 0
        val itr = array.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    array.addValue(10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfSavedArrayIterator() {
        val array = MutableArray()
        (0..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        var n = 0
        val itr = savedArray.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedArray.addValue(10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedArrayIterator() {
        val array = MutableArray()
        (0..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        savedArray.addValue(9)

        var n = 0
        val itr = savedArray.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedArray.addValue(10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfArrayIteratorArrayMember() {
        val subArray = MutableArray()
        val array = MutableArray().addValue(subArray)
        (1..5).forEach { array.addValue(it) }

        var n = 0
        val itr = array.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedArrayIteratorArrayMember1() {
        val array = MutableArray().addValue(MutableArray())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        val ssa = savedArray.getArray(0)
        assertNotNull(ssa)

        var n = 0
        val itr = savedArray.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedArrayIteratorArrayMember2() {
        val array = MutableArray().addValue(MutableArray())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)

        val ssa = sa.getArray(0)
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        var n = 0
        val itr = sa.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(1)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedArrayIteratorArrayMember1() {
        val array = MutableArray().addValue(MutableArray())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        val ssa = savedArray.getArray(0)
        assertNotNull(ssa)

        ssa.addValue(9)

        var n = 0
        val itr = savedArray.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedArrayIteratorArrayMember2() {
        val array = MutableArray().addValue(MutableArray())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)

        val ssa = sa.getArray(0)
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        savedSubArray.addValue(9)

        var n = 0
        val itr = sa.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfArrayIteratorDictMember() {
        val subDict = MutableDictionary()
        val array = MutableArray().addValue(subDict)
        (1..5).forEach { array.addValue(it) }

        var n = 0
        val itr = array.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedArrayIteratorDictMember1() {
        val array = MutableArray().addValue(MutableDictionary())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        val ssd = savedArray.getDictionary(0)
        assertNotNull(ssd)

        var n = 0
        val itr = savedArray.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedArrayIteratorDictMember2() {
        val array = MutableArray().addValue(MutableDictionary())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)

        val ssd = sa.getDictionary(0)
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        var n = 0
        val itr = sa.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedArrayIteratorDictMember1() {
        val array = MutableArray().addValue(MutableDictionary())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)
        val savedArray = sa.toMutable()

        val ssd = savedArray.getDictionary(0)
        assertNotNull(ssd)

        ssd.setValue("9", 9)

        var n = 0
        val itr = savedArray.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedArrayIteratorDictMember2() {
        val array = MutableArray().addValue(MutableDictionary())
        (1..5).forEach { array.addValue(it) }

        val sa = saveDocInCollection(MutableDocument().setValue("array", array)).getArray("array")
        assertNotNull(sa)

        val ssd = sa.getDictionary(0)
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        savedSubDict.setValue("9", 9)

        var n = 0
        val itr = sa.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    ///// Dictionary Iterator Tests

    @Test
    fun testConcurrentModOfMutableDictionaryIterator() {
        val dict = MutableDictionary()
        (0..5).forEach { dict.setValue("$it", it) }

        var n = 0
        val itr = dict.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    dict.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfSavedDictionaryIterator() {
        val dict = MutableDictionary()
        (0..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        var n = 0
        val itr = savedDict.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedDict.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDictionaryIterator() {
        val dict = MutableDictionary()
        (0..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        savedDict.setValue("9", 9)

        var n = 0
        val itr = savedDict.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedDict.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfDictionaryIteratorArrayMember() {
        val subArray = MutableArray()
        val dict = MutableDictionary().setValue("0", subArray)
        (1..5).forEach { dict.setValue("$it", it) }

        var n = 0
        val itr = dict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDictionaryIteratorArrayMember1() {
        val dict = MutableDictionary().setValue("0", MutableArray())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        val ssa = savedDict.getArray("0")
        assertNotNull(ssa)

        var n = 0
        val itr = savedDict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDictionaryIteratorArrayMember2() {
        val dict = MutableDictionary().setValue("0", MutableArray())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)

        val ssa = sd.getArray("0")
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        var n = 0
        val itr = sd.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDictionaryIteratorArrayMember1() {
        val dict = MutableDictionary().setValue("0", MutableArray())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        val ssa = savedDict.getArray("0")
        assertNotNull(ssa)

        ssa.addValue(9)

        var n = 0
        val itr = savedDict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDictionaryIteratorArrayMember2() {
        val dict = MutableDictionary().setValue("0", MutableArray())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)

        val ssa = sd.getArray("0")
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        savedSubArray.addValue(9)

        var n = 0
        val itr = sd.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfDictionaryIteratorDictMember() {
        val subDict = MutableDictionary()
        val dict = MutableDictionary().setValue("0", subDict)
        (1..5).forEach { dict.setValue("$it", it) }

        var n = 0
        val itr = dict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDictionaryIteratorDictMember1() {
        val dict = MutableDictionary().setValue("0", MutableDictionary())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        val ssd = savedDict.getDictionary("0")
        assertNotNull(ssd)

        var n = 0
        val itr = savedDict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDictionaryIteratorDictMember2() {
        val dict = MutableDictionary().setValue("0", MutableDictionary())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)

        val ssd = sd.getDictionary("0")
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        var n = 0
        val itr = sd.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDictionaryIteratorDictMember1() {
        val dict = MutableDictionary().setValue("0", MutableDictionary())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)
        val savedDict = sd.toMutable()

        val ssd = savedDict.getDictionary("0")
        assertNotNull(ssd)

        ssd.setValue("9", 9)

        var n = 0
        val itr = savedDict.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDictionaryIteratorDictMember2() {
        val dict = MutableDictionary().setValue("0", MutableDictionary())
        (1..5).forEach { dict.setValue("$it", it) }

        val sd = saveDocInCollection(MutableDocument().setValue("dict", dict)).getDictionary("dict")
        assertNotNull(sd)

        val ssd = sd.getDictionary("0")
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        savedSubDict.setValue("9", 9)

        var n = 0
        val itr = sd.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }


    ///// Document Iterator Tests

    @Test
    fun testConcurrentModOfMutableDocumentIterator() {
        val doc = MutableDocument()
        (0..5).forEach { doc.setValue("$it", it) }

        var n = 0
        val itr = doc.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    doc.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfSavedDocumentIterator() {
        val doc = MutableDocument()
        (0..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        var n = 0
        val itr = savedDoc.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedDoc.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDocumentIterator() {
        val doc = MutableDocument()
        (0..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        savedDoc.setValue("9", 9)

        var n = 0
        val itr = savedDoc.iterator()
        assertFailsWith<ConcurrentModificationException> {
            while (itr.hasNext()) {
                if (n++ == 3) {
                    savedDoc.setValue("10", 10)
                }
                itr.next()
            }
        }
    }

    @Test
    fun testConcurrentModOfDocumentIteratorArrayMember() {
        val subArray = MutableArray()
        val doc = MutableDocument().setValue("0", subArray)
        (1..5).forEach { doc.setValue("$it", it) }

        var n = 0
        val itr = doc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDocumentIteratorArrayMember1() {
        val doc = MutableDocument().setValue("0", MutableArray())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        val ssa = savedDoc.getArray("0")
        assertNotNull(ssa)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDocumentIteratorArrayMember2() {
        val doc = MutableDocument().setValue("0", MutableArray())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc)

        val ssa = savedDoc.getArray("0")
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDocumentIteratorArrayMember1() {
        val doc = MutableDocument().setValue("0", MutableArray())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        val ssa = savedDoc.getArray("0")
        assertNotNull(ssa)

        ssa.addValue(9)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssa.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDocumentIteratorArrayMember2() {
        val doc = MutableDocument().setValue("0", MutableArray())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc)

        val ssa = savedDoc.getArray("0")
        assertNotNull(ssa)
        val savedSubArray = ssa.toMutable()

        savedSubArray.addValue(9)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubArray.addValue(10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfDocumentIteratorDictMember() {
        val subDict = MutableDictionary()
        val doc = MutableDocument().setValue("0", subDict)
        (1..5).forEach { doc.setValue("$it", it) }

        var n = 0
        val itr = doc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                subDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDocumentIteratorDictMember1() {
        val doc = MutableDocument().setValue("0", MutableDictionary())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        val ssd = savedDoc.getDictionary("0")
        assertNotNull(ssd)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfSavedDocumentIteratorDictMember2() {
        val doc = MutableDocument().setValue("0", MutableDictionary())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc)

        val ssd = savedDoc.getDictionary("0")
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDocumentIteratorDictMember1() {
        val doc = MutableDocument().setValue("0", MutableDictionary())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc).toMutable()

        val ssd = savedDoc.getDictionary("0")
        assertNotNull(ssd)

        ssd.setValue("9", 9)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                ssd.setValue("10", 10)
            }
            itr.next()
        }
    }

    @Test
    fun testConcurrentModOfMutatedSavedDocumentIteratorDictMember2() {
        val doc = MutableDocument().setValue("0", MutableDictionary())
        (1..5).forEach { doc.setValue("$it", it) }

        val savedDoc = saveDocInCollection(doc)

        val ssd = savedDoc.getDictionary("0")
        assertNotNull(ssd)
        val savedSubDict = ssd.toMutable()

        savedSubDict.setValue("9", 9)

        var n = 0
        val itr = savedDoc.iterator()
        while (itr.hasNext()) {
            if (n++ == 3) {
                savedSubDict.setValue("10", 10)
            }
            itr.next()
        }
    }
}
