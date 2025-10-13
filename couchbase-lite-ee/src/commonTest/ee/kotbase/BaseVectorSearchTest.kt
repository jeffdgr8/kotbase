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

import co.touchlab.stately.collections.ConcurrentMutableList
import kotbase.internal.utils.FileUtils
import kotbase.internal.utils.PlatformUtils
import kotbase.logging.CustomLogSink
import kotbase.logging.LogSink
import kotbase.logging.LogSinks
import kotlinx.io.readByteArray
import kotlin.test.assertContains
import kotlin.test.assertTrue
import kotlin.test.fail

class WordEmbeddingModel(
    val db: Database
) : PredictiveModel {

    fun getWordVector(word: String, collection: String): Array? {
        val sql = "select vector from $collection where word = '$word'"
        val q = db.createQuery(sql)
        // don't autoclose this ResultSet, as its content is returned and used by the function caller
        val rs = q.execute()
        val results = rs.allResults()

        return results.firstOrNull()?.getArray("vector")
    }

    override fun predict(input: Dictionary): Dictionary? {
        val inputWord = input.getString("word") ?: fail("No word input !!!")

        val result = getWordVector(inputWord, "words")
            ?: getWordVector(inputWord, "extwords")
            ?: return null

        val output = MutableDictionary()
        output.setValue("vector", result)
        return output
    }
}

abstract class BaseVectorSearchTest : BaseDbTest() {

    val dinnerVector = listOf(0.03193166106939316, 0.032055653631687164, 0.07188114523887634, -0.09893740713596344, -0.07693558186292648, 0.07570040225982666, 0.42786234617233276, -0.11442682892084122, -0.7863243818283081, -0.47983086109161377, -0.10168658196926117, 0.10985997319221497, -0.15261511504650116, -0.08458329737186432, -0.16363860666751862, -0.20225222408771515, -0.2593214809894562, -0.032738097012043, -0.16649988293647766, -0.059701453894376755, 0.17472036182880402, -0.007310086861252785, -0.13918264210224152, -0.07260780036449432, -0.02461239881813526, -0.04195880889892578, -0.15714778006076813, 0.48038315773010254, 0.7536261677742004, 0.41809454560279846, -0.17144775390625, 0.18296195566654205, -0.10611499845981598, 0.11669538915157318, 0.07423929125070572, -0.3105475902557373, -0.045081984251737595, -0.18190748989582062, 0.22430984675884247, 0.05735112354159355, -0.017394868656992912, -0.148889422416687, -0.20618586242198944, -0.1446581482887268, 0.061972495168447495, 0.07787969708442688, 0.14225411415100098, 0.20560632646083832, 0.1786964386701584, -0.380594402551651, -0.18301603198051453, -0.19542981684207916, 0.3879885971546173, -0.2219538390636444, 0.11549852043390274, -0.0021717497147619724, -0.10556972026824951, 0.030264658853411674, 0.16252967715263367, 0.06010117009282112, -0.045007310807704926, 0.02435707487165928, 0.12623260915279388, -0.12688252329826355, -0.3306281864643097, 0.06452160328626633, 0.0707000121474266, -0.04959108680486679, -0.2567063570022583, -0.01878536120057106, -0.10857286304235458, -0.01754194125533104, -0.0713721290230751, 0.05946013703942299, -0.1821729987859726, -0.07293688505887985, -0.2778160572052002, 0.17880073189735413, -0.04669278487563133, 0.05351974070072174, -0.23292849957942963, 0.05746332183480263, 0.15462779998779297, -0.04772235080599785, -0.003306782804429531, 0.058290787041187286, 0.05908169597387314, 0.00504430802538991, -0.1262340396642685, 0.11612161248922348, 0.25303348898887634, 0.18580256402492523, 0.09704313427209854, -0.06087183952331543, 0.19697663187980652, -0.27528849244117737, -0.0837797075510025, -0.09988483041524887, -0.20565757155418396, 0.020984146744012833, 0.031014855951070786, 0.03521743416786194, -0.05171370506286621, 0.009112107567489147, -0.19296088814735413, -0.19363830983638763, 0.1591167151927948, -0.02629968523979187, -0.1695055067539215, -0.35807400941848755, -0.1935291737318039, -0.17090126872062683, -0.35123637318611145, -0.20035606622695923, -0.03487539291381836, 0.2650701701641083, -0.1588021069765091, 0.32268261909484863, -0.024521857500076294, -0.11985184997320175, 0.14826008677482605, 0.194917231798172, 0.07971998304128647, 0.07594677060842514, 0.007186363451182842, -0.14641280472278595, 0.053229596465826035, 0.0619836151599884, 0.003207010915502906, -0.12729716300964355, 0.13496214151382446, 0.107656329870224, -0.16516226530075073, -0.033881571143865585, -0.11175122112035751, -0.005806141998618841, -0.4765360355377197, 0.11495379358530045, 0.1472187340259552, 0.3781401813030243, 0.10045770555734634, -0.1352398842573166, -0.17544329166412354, -0.13191302120685577, -0.10440415143966675, 0.34598618745803833, 0.09728766977787018, -0.25583627820014954, 0.035236816853284836, 0.16205145418643951, -0.06128586828708649, 0.13735555112361908, 0.11582338809967041, -0.10182418674230576, 0.1370954066514969, 0.15048766136169434, 0.06671152263879776, -0.1884871870279312, -0.11004580557346344, 0.24694739282131195, -0.008159132674336433, -0.11668405681848526, -0.01214478351175785, 0.10379738360643387, -0.1626262664794922, 0.09377897530794144, 0.11594484746456146, -0.19621512293815613, 0.26271334290504456, 0.04888357222080231, -0.10103251039981842, 0.33250945806503296, 0.13565145432949066, -0.23888370394706726, -0.13335271179676056, -0.0076894499361515045, 0.18256276845932007, 0.3276212215423584, -0.06567271053791046, -0.1853761374950409, 0.08945729583501816, 0.13876311480998993, 0.09976287186145782, 0.07869105041027069, -0.1346970647573471, 0.29857659339904785, 0.1329529583454132, 0.11350086331367493, 0.09112624824047089, -0.12515446543693542, -0.07917925715446472, 0.2881546914577484, -1.4532661225530319e-05, -0.07712751626968384, 0.21063975989818573, 0.10858846455812454, -0.009552721865475178, 0.1629313975572586, -0.39703384041786194, 0.1904662847518921, 0.18924959003925323, -0.09611514210700989, 0.001136621693149209, -0.1293390840291977, -0.019481558352708817, 0.09661063551902771, -0.17659670114517212, 0.11671938002109528, 0.15038564801216125, -0.020016824826598167, -0.20642194151878357, 0.09050136059522629, -0.1768183410167694, -0.2891409397125244, 0.04596589505672455, -0.004407480824738741, 0.15323616564273834, 0.16503025591373444, 0.17370983958244324, 0.02883041836321354, 0.1463884711265564, 0.14786243438720703, -0.026439940556883812, -0.03113352134823799, 0.10978181660175323, 0.008928884752094746, 0.24813824892044067, -0.06918247044086456, 0.06958142668008804, 0.17475970089435577, 0.04911438003182411, 0.17614248394966125, 0.19236832857131958, -0.1425514668226242, -0.056531358510255814, -0.03680772706866264, -0.028677923604846, -0.11353116482496262, 0.012293893843889236, -0.05192646384239197, 0.20331953465938568, 0.09290937334299088, 0.15373043715953827, 0.21684466302394867, 0.40546831488609314, -0.23753701150417328, 0.27929359674453735, -0.07277711480855942, 0.046813879162073135, 0.06883064657449722, -0.1033223420381546, 0.15769273042678833, 0.21685580909252167, -0.00971329677850008, 0.17375953495502472, 0.027193285524845123, -0.09943609684705734, 0.05770351365208626, 0.0868956446647644, -0.02671697922050953, -0.02979189157485962, 0.024517420679330826, -0.03931192681193352, -0.35641804337501526, -0.10590721666812897, -0.2118944674730301, -0.22070199251174927, 0.0941486731171608, 0.19881175458431244, 0.1815279871225357, -0.1256905049085617, -0.0683583989739418, 0.19080783426761627, -0.009482398629188538, -0.04374842345714569, 0.08184348791837692, 0.20070189237594604, 0.039221834391355515, -0.12251003831624985, -0.04325549304485321, 0.03840530663728714, -0.19840988516807556, -0.13591833412647247, 0.03073180839419365, 0.1059495136141777, -0.10656466335058212, 0.048937033861875534, -0.1362423598766327, -0.04138947278261185, 0.10234509408473969, 0.09793911874294281, 0.1391254961490631, -0.0906999260187149, 0.146945983171463, 0.14941848814487457, 0.23930180072784424, 0.36049938201904297, 0.0239607822149992, 0.08884347230195999, 0.061145078390836716)

    val lunchVectorBase64 = "4OYevd8eyDxJGj69HCKOvoCJYTzQCJs9xhDbPp1Y6r2OTEm/ZKz1vtRbwL1Ik8I9+RQFPpyGBD69OEI9ul+evZD71L2nI4y8uTINPnVN+702+c4+8zToPEoGKj6xEqi93vPFvQDdK71Z6yC+yPT1PqXtQD99ENY+xnh+PpBEOD6aIUi+eVezvg24fj0YAJ++46c4vfVFOr57sWU+A+lqPdFq3T1ZJg6+Ok6yvs1/Cr5blju+ITa9vAFxlj1+8h4+c7UePe6fUL6OaDu+wR5IvnGmxj7eR2O+fYrsPf8kw73IOfq8YOJtvAxBMj0g99O8+toTPr0v8r2I4mK+Yxd1PTGxhbzu3aS9zeJEPqKy0Ty2cOy9YqgQPL7af703wFK9965hvOM0pz2VuAc+RIyTu4nxi73pigA9RCjpvVTOFj6zPIC+HTsrvrcpTz4vXzS6ArPxvM+VNL3hJgk+9pM7vtP1jL51sao8q4oJPonfBDxkAiC9XvJUPWiWTD1Kwbe+4KHOvUQmjjypsrS6i4MJPjRnWz0g8E4+Ad3IvVsKMT5O7Qw9X4tFPbpriT1TYme8uw5uvqBar72DLEa+vgAvvkHVs74kKk2+gNkOvZkV57zBfcC+/WM7PrKQQb4+adC9ftEXPmKYRz47RKM9+4mbPZZ76zs4LZq+0gIXPgNoxL26tT09rGFdvPdQqDwi/Y8939OLvYVTQr7J8hK+ljyeveMZsL5xeGi8sppcPfezjT11QuU9cvRpPSoby7yIZ3U9FUPXPd/y1z2xBhu9CfRyvbjXR72xLjk+9rkLvrdWJD2u+Iy9TtM/vlc0Ez4E1ju9XtcrPP+4Cr5ymDu+DfEAPswpP770tKm+3u07vsXxXb19zcC8MQ/APX507T2e7Ei+XYKGPiQ6SD0MORK+Lk4NP1zuHTzrAKW+Eu2WvSGPRj6fL7g9IdSgPkNyojxUSPi95uGqvJugrj0Bqbc9x1eVPk8qh74NlYk+07gZPVqt271XR2E+bMxmOyw0JD1Lg2Y+h+GDvRpuj70YCss890HtPdFwMz7oo7I+RpgXv4/lkz54b+Y8l6yOPdbWYj3H+4G+Q4wXvsXhyD0ayts9XIXBPndXLj34Q1I+0zfQu5pblj66UKa9dSWqvRl1xb04RQK9HsA6PrH2rD2r8wC+XQQPPlSirDwC3zU+K7Z4vUfVML4xHyY92TguPigvMj2emD8+q3AXPsSHWz4Cq5+9P/o7PveDcD095w++4fc9vvE81j17lt09AY7CvHD/Nz7FdCe+t7z4PDJPZD4Qsce9mdwZPtvzDj60sz6+ETvUPTLZ970Gauu83dW7PZZPCj51tCc+yMYtPYrmSjyUcpE+GCDgPf1tGr7aODg+ESYGPmu52T070vi9kW0vvaiwWj6JgQ6+hoehPVygk77JeOg8yCI+PtSnpD2I6w0+z3IFPRUoLD7boxM+XJYbviPzNrxBSBs+XO+WPpkuH74N9+m9tds9PiCinT6BaZ2+tGIfvhZSTj2ZP2k+cld+PHx1Kj4uOfK9bsXHPRx8Bz5OlMg96nYOPuLAub0CeRY+KQEZPogLdT5gk7g+Z0nEPJHztT1Dc3o9"

    val wordsDatabaseName = "words_db"

    val wordsCollectionName = "words"

    val extWordsCollectionName = "extwords"

    val wordsIndexName = "words_index"

    val wordPredictiveModelName = "WordEmbedding"

    lateinit var logger: TestCustomLogSink

    lateinit var wordDB: Database

    var modelDB: Database? = null

    lateinit var wordsCollection: Collection

    lateinit var extWordsCollection: Collection

    val directory = getScratchDirectoryPath(getUniqueName("CouchbaseLite-EE"))

    class TestCustomLogSink : LogSink {
        val lines = ConcurrentMutableList<String>()

        override fun writeLog(level: LogLevel, domain: LogDomain, message: String) {
            lines.add(message)
        }

        fun reset() {
            lines.clear()
        }

        fun containsString(string: String): Boolean {
            return lines.block {
                for (line in it) {
                    if (line.contains(string)) {
                        return@block true
                    }
                }
                false
            }
        }
    }

    override fun setUpFirst() {
        try {
            Database.delete(wordsDatabaseName, directory)
        } catch (ignore: Exception) { }

        super.setUpFirst()

        Extension.enableVectorSearch()

        val config = DatabaseConfiguration()
        config.directory = directory
        PlatformUtils.getAsset("vectorsearch/$wordsDatabaseName.cblite2/db.sqlite3")!!.use { input ->
            val bytes = input.readByteArray()
            val dir = "$directory/$wordsDatabaseName.cblite2"
            FileUtils.verifyDir(dir)
            FileUtils.write(bytes, "$dir/db.sqlite3")
        }

        wordDB = Database(wordsDatabaseName, config)
        wordsCollection = wordDB.getCollection(wordsCollectionName)!!
        extWordsCollection = wordDB.getCollection(extWordsCollectionName)!!

        logger = TestCustomLogSink()
        LogSinks.custom = CustomLogSink(LogLevel.INFO, logSink = logger)
    }

    override fun tearDown() {
        LogSinks.custom = null
        if (::wordDB.isInitialized) {
            wordDB.close()
        }

        modelDB?.let {
            it.close()
            unregisterPredictiveModel()
        }
        super.tearDown()
    }

    fun resetIndexWasTrainedLog() {
        logger.reset()
    }

    fun checkIndexWasTrained(): Boolean {
        return !logger.containsString("Untrained index; queries may be slow")
    }

    fun toDocIDWordMap(rs: ResultSet): Map<String, String> {
        val wordMap = mutableMapOf<String, String>()
        for (result in rs.allResults()) {
            result.getString(0)?.let { docID ->
                result.getString(1)?.let { word ->
                    wordMap[docID] = word
                }
            }
        }
        return wordMap
    }

    fun registerPredictiveModel() {
        if (modelDB == null) {
            val config = DatabaseConfiguration()
            config.directory = directory
            modelDB = Database(wordsDatabaseName, config)
        }

        val modelDB = modelDB ?: fail("Cannot open model DB")

        val model = WordEmbeddingModel(modelDB)
        Database.prediction.registerModel(wordPredictiveModelName, model)
    }

    fun unregisterPredictiveModel() {
        Database.prediction.unregisterModel(wordPredictiveModelName)
    }

    fun createVectorIndex(collection: Collection, name: String, config: VectorIndexConfiguration) {
        collection.createIndex(name, config)
    }

    fun createWordsIndex(config: VectorIndexConfiguration) {
        wordsCollection.createIndex(wordsIndexName, config)

        val names = wordsCollection.indexes
        assertTrue(names.contains(wordsIndexName))
    }

    fun deleteWordsIndex() {
        wordsCollection.deleteIndex(wordsIndexName)
    }

    open fun wordsQueryDefaultExpression(): String =
        "vector"

    fun wordsQueryString(
        limit: Int,
        metric: String? = null,
        vectorExpression: String? = null,
        whereExpression: String? = null
    ): String {
        return buildString {
            append("SELECT meta().id, word, catid ")

            append("FROM $wordsCollectionName ")

            if (whereExpression != null) {
                append("WHERE $whereExpression ")
            }

            val expr = vectorExpression ?: wordsQueryDefaultExpression()

            if (metric != null) {
                append("ORDER BY APPROX_VECTOR_DISTANCE($expr, \$vector, \"$metric\") ")
            } else {
                append("ORDER BY APPROX_VECTOR_DISTANCE($expr, \$vector) ")
            }

            append("LIMIT $limit")
        }
    }

    fun executeWordsQuery(
        limit: Int,
        metric: String? = null,
        vectorExpression: String? = null,
        whereExpression: String? = null,
        checkTraining: Boolean = true
    ): ResultSet {
        val sql = wordsQueryString(limit, metric, vectorExpression, whereExpression)
        val query = wordDB.createQuery(sql)

        val parameters = Parameters()
        parameters.setValue("vector", dinnerVector)
        query.parameters = parameters

        val explain = query.explain()
        assertContains(explain, "kv_.words:vector:words_index")

        val rs = query.execute()
        if (checkTraining) {
            assertTrue(checkIndexWasTrained())
        }
        return rs
    }
}
