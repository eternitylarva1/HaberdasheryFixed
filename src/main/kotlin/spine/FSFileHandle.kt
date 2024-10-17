package haberdashery.spine

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.StreamUtils
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

internal class FSFileHandle(private val path: Path) : FileHandle() {
    override fun equals(other: Any?): Boolean {
        if (other !is FSFileHandle) {
            return false
        }
        return path == other.path
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = hash * 37 + path.hashCode()
        return hash
    }

    override fun toString(): String {
        return path()
    }

    override fun path(): String {
        return path.toString().replace('\\', '/')
    }

    override fun name(): String {
        return path.fileName?.toString() ?: ""
    }

    override fun extension(): String {
        return path.fileName?.toString()?.substringAfterLast(".") ?: ""
    }

    override fun nameWithoutExtension(): String {
        return path.fileName?.toString()?.substringBeforeLast(".") ?: ""
    }

    override fun pathWithoutExtension(): String {
        return path().substringBeforeLast(".")
    }

    override fun read(): InputStream {
        return path.inputStream()
    }

    override fun child(name: String): FileHandle {
        return FSFileHandle(path.resolve(name))
    }

    override fun parent(): FileHandle {
        val parent = path.parent
        if (parent == null) {
            return FSFileHandle(path.fileSystem.getPath(""))
        }
        return FSFileHandle(parent)
    }

    override fun exists(): Boolean {
        return path.exists()
    }

    override fun length(): Long {
        val input = read()
        try {
            return input.available().toLong()
        } catch (ignore: Exception) {
        } finally {
            StreamUtils.closeQuietly(input)
        }
        return 0L
    }
}
