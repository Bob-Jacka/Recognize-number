import os

from keras import Sequential
import numpy as np
from matplotlib import image as img
import keras
import tensorflow

ext_keras: str = '.keras'
ext_pb: str = '.pb'

save_model_path: str = '/data/data/com.example.recognizenumber/files/Neuro/'


def iamalive():
    return "hello from python"


# def load_model(neuro_arc: str = "dense") -> Sequential:
#     if os.path.exists(save_model_path) and os.listdir(save_model_path).__len__() != 0:
#         list_models = os.listdir(save_model_path)
#         if list_models.count(neuro_arc + ext_pb) != 0:
#             val_to_load = save_model_path + neuro_arc + ext_pb
#             model = tensorflow.saved_model.load(val_to_load)
#             return model

def load_models(neuro_arc: str = "dense") -> Sequential:
    if os.path.exists(save_model_path) and os.listdir(save_model_path).__len__() != 0:
        val_to_load = save_model_path + neuro_arc + ext_keras
        model = keras.models.load_model(val_to_load)
        return model


def predict_number(path_to_data: str, batch_size=1) -> int:
    model = load_models()
    weights_before = model.get_weights()  # get weights before recognize
    print('freeze weights')
    try:
        if path_to_data is not None:
            image_to_recognize = img.imread(path_to_data)
            image_to_recognize = image_to_recognize.sum(axis=2)
            image_to_recognize = image_to_recognize.reshape((1, 28 * 28)).astype(
                np.float32) / 255

            if model is not None:
                value = model.predict(x=image_to_recognize, batch_size=batch_size)[0]
                key = 0
                results = dict()
                for _ in value:
                    results[key] = _
                    key += 1
                model.set_weights(weights_before)
                return get_maximum_from_dict(results)
            else:
                print('Please, load model')
        else:
            print('Please specify the image')
    except FileNotFoundError:
        print('please, make sure that file with image exists')


def get_maximum_from_dict(dictionary: dict) -> int:
    value_tmp: float = 0.0
    key_tmp: int = 0
    for key, val in dictionary.items():
        if value_tmp < val:
            value_tmp = val
            key_tmp = key
    return key_tmp
