package haberdashery.utils

import java.util.Comparator
import java.util.function.Predicate


class ObservableArrayList<T> : ArrayList<T>() {
    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener): Listener {
        listeners.add(listener)
        return listener
    }

    fun removeListener(listener: Listener): Boolean {
        return listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach(Listener::onChange)
    }

    interface Listener {
        fun onChange()
    }

    override fun add(element: T): Boolean {
        return super.add(element).also {
            notifyListeners()
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        notifyListeners()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return super.addAll(elements).also {
            notifyListeners()
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).also {
            notifyListeners()
        }
    }

    override fun removeAt(index: Int): T {
        return super.removeAt(index).also {
            notifyListeners()
        }
    }

    override fun remove(element: T): Boolean {
        return super.remove(element).also {
            notifyListeners()
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements).also {
            notifyListeners()
        }
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        return super.removeIf(filter).also {
            notifyListeners()
        }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        notifyListeners()
    }

    override fun clear() {
        super.clear()
        notifyListeners()
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return super.retainAll(elements).also {
            notifyListeners()
        }
    }

    override fun sort(c: Comparator<in T>?) {
        super.sort(c)
        notifyListeners()
    }
}
