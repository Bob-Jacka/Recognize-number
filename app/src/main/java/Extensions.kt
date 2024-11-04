import android.content.Context
import android.util.Log
import android.widget.Toast
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object Extensions {

    private lateinit var py: Python
    private lateinit var pyObject: PyObject

    fun makeShortText(context: Context, text: String) {
        try {
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.d("extensions", "error in make short text")
        }
    }

    /**
     * initialization of python
     */
    fun initPython(context: Context) {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
                py = Python.getInstance()
                pyObject = py.getModule("Nnetwork")
            }
        } catch (e: Exception) {
            Log.d("extensions", "error in python initialization")
        }
    }

    fun pythonAction(methodToEval: PythonMethod, path: String): PyObject? {
        return pyObject.callAttr(methodToEval.getValue(), path)
    }

    fun pythonAction(methodToEval: PythonMethod): PyObject? {
        return pyObject.callAttr(methodToEval.getValue())
    }
}