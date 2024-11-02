enum class PythonMethod(private val strName: String) {

    LOAD_MODEL("load_model"),
    PREDICT_NUMBER("predict_number"),
    GET_MAXIMUM_FROM_DICT("get_maximum_from_dict"),
    I_AM_ALIVE("iamalive");

    fun getValue(): String {
        return strName
    }
}