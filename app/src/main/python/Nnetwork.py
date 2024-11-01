import numpy as np
import os
from matplotlib import image as img
from tensorflow import keras
from tensorflow.keras import Sequential

image_to_recognize: np.ndarray
model: Sequential
save_model_path: str = '../../../NeuroNet/'
ext: str = '.keras'


def iamalive():
    return 'hello from python'


def load_model(neuro_arc: str = "dense") -> None:
    if os.path.exists(save_model_path) and os.listdir(save_model_path).__len__() != 0:
        list_models = os.listdir(save_model_path)
        if list_models.count(neuro_arc) != 0:
            val_to_load = save_model_path + neuro_arc + ext
            with open(val_to_load, 'r'):
                model = keras.models.load_model(val_to_load)


def predict_number(image_to_recognize, batch_size=1) -> None:
    weights_before = model.get_weights()  # get weights before recognize
    print('freeze weights')
    try:
        if image_to_recognize is not None:
            image_to_recognize = img.imread(image_to_recognize)
            image_to_recognize = image_to_recognize.sum(axis=2)
            image_to_recognize = image_to_recognize.reshape((1, 28 * 28)).astype(
                np.float32) / 255

            if model is not None and image_to_recognize is not None:
                value = model.predict(x=image_to_recognize, batch_size=batch_size)[0]
                key = 0
                results = dict()
                for _ in value:
                    results[key] = _
                    key += 1
                get_maximum_from_dict(results)

                # print(
                #     'please enter "yes" or "y" if prediction is right else "no" or "n" if not: '
                #     ,
                #     end='')
                # user_prompt = input().lower()
                # if user_prompt == 'yes' or user_prompt == 'y':
                #     model.set_weights(weights_before)  # set frozen weights after recognize
                #     print('weights returned')
                # else:
                #     print('weights changed')
            else:
                print('Please, load model')
        else:
            print('Please specify the image')
    except FileNotFoundError:
        print('please, make sure that file with image exists')


def get_maximum_from_dict(dictionary: dict) -> None:
    value_tmp: float = 0.0
    key_tmp: int = 0
    for key, val in dictionary.items():
        if value_tmp < val:
            value_tmp = val
            key_tmp = key
    print(f'Neuro net thinks that number from the image is {key_tmp}.')
