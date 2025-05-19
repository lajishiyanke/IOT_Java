import torch
import torch.nn as nn
from torchvision import models

class EfficientNetB0(nn.Module):
    def __init__(self, num_classes=3, pretrained=False):
        super(EfficientNetB0, self).__init__()
        # 不使用预训练模型
        self.model = models.efficientnet_b0(weights=None)
        
        # 添加输入层的归一化
        self.normalize = nn.BatchNorm2d(3)
        
        # 修改最后的分类层
        num_features = self.model.classifier[1].in_features
        self.model.classifier = nn.Sequential(
            nn.Dropout(p=0.2, inplace=True),
            nn.Linear(num_features, num_classes),
        )

    def forward(self, x):
        # 添加归一化
        x = self.normalize(x)
        return self.model(x)

