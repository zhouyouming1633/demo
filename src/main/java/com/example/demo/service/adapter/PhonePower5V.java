package com.example.demo.service.adapter;

/**
 * 目标接口，定义手机可直接使用的低压输出能力。
 */
public interface PhonePower5V {

    /**
     * 获取手机可直接使用的输出电压。
     *
     * @return 转换后的 5V 电压值
     */
    int outputVoltage();
}
