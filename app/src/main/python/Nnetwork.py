import os
import torch
from PIL import Image
from torchvision import transforms

ext_keras: str = '.keras'
ext_pb: str = '.pb'
ext_torch: str = '.pth'

save_model_path: str = '/data/data/com.example.recognizenumber/files/Neuro/'

transform_func3 = transforms.Compose([
    transforms.Grayscale(),
    transforms.Resize((28, 28)),
    transforms.ToTensor(),
    transforms.Normalize((0.5,), (0.5,))
])


def iamalive():
    return "hello from python"


def load_model(load_path: str):
    if len(os.listdir(load_path)) != 0:
        state_dict = torch.load(load_path + 'model' + ext_torch, weights_only=True)
        loaded_model = torch.nn.Module()
        if 'module.' in next(iter(state_dict.keys())):
            state_dict = {k.replace('module.', ''):
                              v for k, v in state_dict.items()}
        loaded_model.load_state_dict(state_dict, strict=False)
        loaded_model.eval()
        print("model loaded and ready")
        return loaded_model
    else:
        print("Model is not exitst")
        return None


def predict_number(image_path: str):
    model = load_model(save_model_path)
    if model is not None:
        print("Net thinks that ")
        with torch.no_grad():
            image = Image.open(image_path)
            image = transform_func3(image).unsqueeze(0)
            output = model(image)
            _, predicted = torch.max(output.data, 1)
            return predicted.item()
    else:
        return "model is not exist"
