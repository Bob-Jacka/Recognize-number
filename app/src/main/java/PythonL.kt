enum class PythonL(private val strName: String) {

    LOAD_MODEL("load_model"),
    PREDICT_NUMBER("predict_number"),
    I_AM_ALIVE("iamalive"),

    MODULE_NAME("Nnetwork");

    fun getValue(): String {
        return strName
    }
}