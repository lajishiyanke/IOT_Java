package com.iot.platform.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据序列化工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSerializationUtil {

    /**
     * 保存数据到NumPy .npy文件
     */
    public void saveToNumpyFile(double[] data, Path file) throws IOException {
        INDArray array = Nd4j.create(data);
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(file))) {
            writeNumpyHeader(dos, data.length);
            for (double value : data) {
                dos.writeDouble(value);
            }
        }
    }

    /**
     * 从NumPy .npy文件加载数据
     */
    public double[] loadFromNumpyFile(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new FileNotFoundException("File not found: " + file);
        }

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
            skipNumpyHeader(dis);
            long dataSize = Files.size(file) - 128; // 128字节头部
            int length = (int) (dataSize / Double.BYTES);
            
            double[] data = new double[length];
            for (int i = 0; i < length; i++) {
                data[i] = dis.readDouble();
            }
            return data;
        }
    }

    private void writeNumpyHeader(DataOutputStream dos, int length) throws IOException {
        dos.write(new byte[]{ (byte)0x93, (byte)'N', (byte)'U', (byte)'M', (byte)'P', (byte)'Y' });
        dos.write((byte) 0x01);  // major version
        dos.write((byte) 0x00);  // minor version
        
        String header = String.format(
            "{'descr': '<f8', 'fortran_order': False, 'shape': (%d,), }", length
        );
        dos.writeShort(header.length());
        dos.writeBytes(header);
    }

    private void skipNumpyHeader(DataInputStream dis) throws IOException {
        dis.skipBytes(6);  // magic string
        dis.skipBytes(2);  // version
        int headerLen = dis.readShort();
        dis.skipBytes(headerLen);
    }
}