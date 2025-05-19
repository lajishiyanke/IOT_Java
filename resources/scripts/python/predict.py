import torch
import numpy as np
import json
import sys
import os
import traceback
import pywt
from model import EfficientNetB0  # 导入模型定义
from scipy.ndimage import zoom

def cwt_transform(signal_data, fs=1/1e-7):
    """
    使用连续小波变换生成时频图
    Args:
        signal_data: 1D信号数据
        fs: 采样频率
    Returns:
        cwt_matrix: 归一化的时频图矩阵
    """
    print(f"输入信号shape: {signal_data.shape}")
    
    # 时间参数设置
    dt = 1e-7
    total_time = 1e-3
    num_samples = int(total_time / dt) + 1
    t = np.linspace(0, total_time, num_samples)
    
    # 小波变换参数设置
    wavename = 'cmor1-1'
    totalscal = 256
    wcf = pywt.central_frequency(wavename)
    cparam = 10 * wcf * totalscal
    a = np.arange(totalscal, 0, -1)
    scal = cparam / a
    f = pywt.scale2frequency(wavename, scal) / fs  # 将尺度转换为频率
    
    # 执行连续小波变换
    coefs, _ = pywt.cwt(signal_data, scal, wavename)
    print(f"小波变换后shape: {coefs.shape}")
    coefs = np.abs(coefs)
    
    # 归一化
    if np.max(coefs) != np.min(coefs):
        coefs = (coefs - np.min(coefs)) / (np.max(coefs) - np.min(coefs))
    else:
        coefs = np.zeros_like(coefs)
    
    # 频率范围限制（50kHz - 400kHz）
    freq_mask = (f >= 50e3) & (f <= 400e3)
    if not np.any(freq_mask):  # 如果没有频率在范围内
        print(f"警告：当前频率范围 [{np.min(f):.2e}, {np.max(f):.2e}] Hz")
        # 返回原始系数而不是空数组
        return coefs
    
    filtered_coefs = coefs[freq_mask]
    print(f"频率限制后shape: {filtered_coefs.shape}")
    
    return filtered_coefs

def load_model(model_path, device):
    """加载已有模型"""
    try:
        # 创建模型实例
        model = EfficientNetB0(num_classes=3)
        
        # 加载本地保存的模型权重
        if os.path.exists(model_path):
            state_dict = torch.load(model_path, map_location=device)
            model.load_state_dict(state_dict)
            print(f"模型 {model_path} 加载成功")
        else:
            print(f"未找到模型文件 {model_path}，使用未训练的模型")
        
        model.to(device)
        model.eval()  # 设置为评估模式
        return model
        
    except Exception as e:
        print(f"加载模型时出错: {str(e)}")
        raise

def process_signal_batch(signal_batch, fs=1/1e-7):
    """
    处理一批信号（3个通道）并生成拼接的时频图
    Args:
        signal_batch: 包含3个通道信号的数组 [signal1, signal2, signal3]
        fs: 采样频率
    Returns:
        combined_image: 横向拼接的时频图
    """
    time_freq_images = []
    
    for i, signal in enumerate(signal_batch):
        print(f"处理第 {i+1} 个信号")
        # 对每个信号进行CWT变换
        coefs = cwt_transform(signal, fs)
        time_freq_images.append(coefs)
    
    # 确保所有图像具有相同的高度
    max_height = max(img.shape[0] for img in time_freq_images)
    print(f"最大高度: {max_height}")
    
    # 调整每个图像的大小
    normalized_images = []
    target_width = 224 // 3  # 每个图像的目标宽度
    target_height = 224  # 目标高度
    
    for i, img in enumerate(time_freq_images):
        print(f"调整第 {i+1} 个图像从 {img.shape} 到 ({target_height}, {target_width})")
        img_resized = zoom(img, (target_height/img.shape[0], target_width/img.shape[1]))
        normalized_images.append(img_resized)
    
    # 横向拼接图像
    combined_image = np.concatenate(normalized_images, axis=1)
    print(f"拼接后的图像shape: {combined_image.shape}")
    return combined_image

def predict_single(model, signal_data, device):
    """预测单个信号"""
    model.eval()
    with torch.no_grad():
        try:
            # 将信号数据分成3个通道
            signals = np.array_split(signal_data, 3)
            print(f"分割后的信号shape: {[s.shape for s in signals]}")
            
            # 生成拼接的时频图
            combined_image = process_signal_batch(signals)
            print(f"拼接后的图像shape: {combined_image.shape}")
            
            # 转换为3通道图像格式 [batch_size, channels, height, width]
            image = np.stack([combined_image] * 3, axis=0)
            image = np.expand_dims(image, axis=0)
            print(f"最终输入tensor shape: {image.shape}")
            
            # 转换为tensor
            input_tensor = torch.FloatTensor(image)
            input_tensor = input_tensor.to(device)
            
            # 模型推断
            output = model(input_tensor)
            
            # 将结果从GPU移到CPU
            prediction = output.cpu().numpy().squeeze()
            
            return prediction
            
        except Exception as e:
            print(f"预测过程中出错: {str(e)}")
            raise

if __name__ == "__main__":
    try:
        print(f"当前工作目录: {os.getcwd()}")
        print(f"Python版本: {sys.version}")
        print(f"PyTorch版本: {torch.__version__}")
        
        # 读取输入文件路径
        input_file = sys.argv[1]
        print(f"输入文件路径: {input_file}")
        
        # 读取参数和信号数据
        with open(input_file, 'r') as f:
            # 读取参数行
            params_line = f.readline().strip('# Parameters: ')
            params = json.loads(params_line)
            model_path = params.get('model_path', 'model.pth')
            print(f"模型路径: {model_path}")
            
            # 读取信号数据
            signal_data = np.loadtxt(f)
            print(f"信号数据shape: {signal_data.shape}")
        
        # 设备选择
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        print(f"使用设备: {device}")
        
        # 加载模型
        model = load_model(model_path, device)
        
        # 预测
        prediction = predict_single(model, signal_data, device)
        print(f"预测结果shape: {prediction.shape}")
        
        # 保存预测结果
        output_file = input_file + '.data'
        np.savetxt(output_file, prediction)
        print(f"预测结果已保存到: {output_file}")
        
    except Exception as e:
        print(f"错误类型: {type(e)}")
        print(f"错误信息: {str(e)}")
        print("详细错误栈:")
        traceback.print_exc()
        sys.exit(1)
