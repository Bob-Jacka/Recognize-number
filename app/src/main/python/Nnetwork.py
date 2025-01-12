import os
import torch
from PIL import Image
from torch import nn
from torchvision import transforms

save_model_path: str = '/data/data/com.example.recognizenumber/files/Neuro/'
pic_path: str = '/mnt/sdcard/Pictures/'

ext_torch: str = '.pth'
static_pic_ext: str = '.jpg'
static_model_name: str = 'cnn.pth'

transform_func3 = transforms.Compose([
    transforms.Grayscale(),
    transforms.Resize((28, 28)),
    transforms.ToTensor(),
    transforms.Normalize((0.5,), (0.5,))
])

class inner_cnn(nn.Module):

    def __init__(self):
        super(inner_cnn, self).__init__()
        self.conv1 = nn.Conv2d(1, 32, kernel_size=3, padding=1)
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)
        self.pool = nn.MaxPool2d(kernel_size=2, stride=2)
        self.fc1 = nn.Linear(64 * 7 * 7, 128)
        self.fc2 = nn.Linear(128, 10)

    def forward(self, x):
        x = self.pool(nn.ReLU()(self.conv1(x)))
        x = self.pool(nn.ReLU()(self.conv2(x)))
        x = x.view(-1, 64 * 7 * 7)
        x = nn.ReLU()(self.fc1(x))
        x = self.fc2(x)
        return x

def iamalive():
    return 'Hello from python file'


def load_model(load_path: str):
    if len(os.listdir(load_path)) != 0:
        state_dict = torch.load(load_path + static_model_name)
        loaded_model = inner_cnn()
        if 'module.' in next(iter(state_dict.keys())):
            state_dict = {k.replace('module.', ''):
                              v for k, v in state_dict.items()}
        loaded_model.load_state_dict(state_dict, strict=False)
        loaded_model.eval()
        return loaded_model
    else:
        return None


def predict_number(file_name: str):
    model = load_model(save_model_path)
    if model is not None:
        with torch.no_grad():
            image = Image.open(pic_path + file_name + static_pic_ext, mode='r')
#             '/data/data/com.example.recognizenumber/files/chaquopy/AssetFinder/app'
            image = transform_func3(image).unsqueeze(0)
            output = model(image)
            _, predicted = torch.max(output.data, 1)
            return predicted.item()
    else:
        return 'model is not exist'
