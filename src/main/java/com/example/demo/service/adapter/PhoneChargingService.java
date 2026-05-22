package com.example.demo.service.adapter;

/**
 * 客户端，依赖目标接口完成充电流程。
 */
public class PhoneChargingService {

    /**
     * 使用适配后的电压为手机充电，并返回充电结果描述。
     *
     * @param power 手机可用电源接口
     * @return 充电结果描述
     */
    public String charge(PhonePower5V power) {
        int voltage = power.outputVoltage();
        return "Phone is charging with " + voltage + "V";
    }
}
