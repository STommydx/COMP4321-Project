package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException

/**
 * The `PhraseQuery` class represent phrase query with an input phrase.
 *
 *
 *
 * @see Query
 *
 * @see WordQuery
 */
class PhraseQuery(private val phrase: String) : WordQuery() {
    private val listOfWords: List<String> = phrase.split(" ")

    /**
     * Return a set with ID of documents which contains the phrase consecutively
     *
     * @param invertedIndex to get the document ID from a word
     * @return Set of docID, empty set if the phrase does not exist in any doc
     * @throws RocksDBException if there is an error from RocksDB
     * @throws IOException if there is an I/O problem
     * @throws ClassNotFoundException if the deserialization fails
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    override fun getRootSet(invertedIndex: InvertedIndex): Set<Int> {

        // take intersection of all sets containing docID
        var intersectionSet = listOfWords
                .flatMap { invertedIndex.getDocumentsFromWord(it) }
                .toSet()

        var oldPosting: Map<Int, ArrayList<Int>>? = null
        for (word in listOfWords) {
            // return if one of the words is not in invertedIndex
            val postingList = invertedIndex[word]
                    ?: return HashSet()

            // run this if it is the 0th iteration
            if (oldPosting == null) {
                oldPosting = postingList
                continue
            }
            val newIntersectionSet: MutableSet<Int> = HashSet()
            val nextPostingList: MutableMap<Int, ArrayList<Int>> = HashMap()
            for (docID in intersectionSet) {
                val oldPosList = oldPosting[docID]!!  // the document must contain the word
                val posList = postingList[docID]!!
                val nextPosList = ArrayList<Int>()
                for (pos in oldPosList) {
                    if (pos >= 0) {
                        // last word is in body, and grow in +ve direction
                        if (posList.contains(pos + 1)) {
                            nextPosList.add(pos + 1)
                        }
                    } else {
                        // the last word is in title, and grow in -ve direction
                        if (posList.contains(pos - 1)) {
                            nextPosList.add(pos - 1)
                        }
                    }
                }
                if (nextPosList.isNotEmpty()) {
                    newIntersectionSet.add(docID) // this docID contains the sequence
                    nextPostingList[docID] = nextPosList
                }
            }

            // Update set and map for next (word) iteration
            intersectionSet = newIntersectionSet
            oldPosting = nextPostingList
        }
        return intersectionSet
    }

    /**
     * {@inheritDoc}
     */
    override val queryVector: Map<String, Double>
        get() {
            return listOfWords.associateWith { 1.0 }
        }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return "\"" + phrase + "\""
    }

}
